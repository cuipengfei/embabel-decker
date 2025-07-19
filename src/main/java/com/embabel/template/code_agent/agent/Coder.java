/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.template.code_agent.agent;

import com.embabel.agent.api.annotation.*;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.tools.file.FileWriteTools;
import com.embabel.chat.Conversation;
import com.embabel.chat.SimpleMessageFormatter;
import com.embabel.chat.WindowingConversationFormatter;
import com.embabel.template.code_agent.domain.CodeModificationReport;
import com.embabel.template.code_agent.domain.CodeModificationRequest;
import com.embabel.template.code_agent.domain.SoftwareProject;
import com.embabel.template.code_agent.domain.SuccessfulCodeModification;
import com.embabel.template.code_agent.domain.TaskFocus;
import com.embabel.template.code_agent.tools.BuildResult;
import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.util.TimeKt;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static kotlin.collections.CollectionsKt.listOfNotNull;

/**
 * Embabel coding agent.
 * <p>
 * The Coder agent is responsible for modifying code in a software project based on user requests.
 * <p>
 * The agent uses conditions to control the flow:
 * - BuildNeeded: Triggered after code modification to determine if a build is required
 * - BuildSucceeded/BuildFailed: Tracks build status
 * - BuildWasLastAction: Helps determine the next step in the flow
 */
@Agent(description = "Perform changes to a software project or directory structure")
@Profile("!test")
public class Coder {

    private final Logger logger = LoggerFactory.getLogger(Coder.class);
    private final TaskFocus taskFocus;
    private final CoderProperties coderProperties;
    private final LogWriter logWriter;

    public Coder(TaskFocus taskFocus, CoderProperties coderProperties, LogWriter logWriter) {
        this.taskFocus = taskFocus;
        this.coderProperties = coderProperties;
        this.logWriter = logWriter;
    }

    @Action
    public SoftwareProject loadExistingProject() {
        SoftwareProject project = taskFocus.getSoftwareProject();
        logger.info("Working on project {}", project != null ? project.getRoot() : "None");
        return project;
    }

    /**
     * Converts raw user input into a structured code modification request
     * Uses GitHub tools to search for issues if the user references them
     */
    @Action(toolGroups = {CoreToolGroups.GITHUB})
    public CodeModificationRequest codeModificationRequestFromUserInput(
            SoftwareProject project,
            UserInput userInput,
            Conversation conversation) {

        // Note: This uses Kotlin interop for the `using` function and `create` extension

        return ActionMethodPromptRunnerKt
                .using(coderProperties.getPrimaryCodingLlm())
                .withPromptContributors(listOfNotNull(conversation.promptContributor(new WindowingConversationFormatter(SimpleMessageFormatter.INSTANCE, 100))))
                .createObject("Create a CodeModification request based on this user input: " + userInput.getContent() + "\n" +
                                "If the user wants you to pick up an issue from GitHub, search for it at " + project.getUrl() + ".\n" +
                                "Search for the milestone the user suggests.\n" +
                                "Use the GitHub tools.\n" +
                                "Create a CodeModificationRequest from the issue.",
                        CodeModificationRequest.class
                );
    }

    /**
     * The LLM will determine the command to use to build the project.
     * Only use as a last resort, so we mark it as expensive.
     * <p>
     * This is a fallback build method when the standard build method isn't sufficient
     * Triggered by the BuildNeeded condition after code modifications
     */
    @Action(
            cost = 10000.0,
            canRerun = true,
            pre = {CoderConditions.BUILD_NEEDED},
            post = {CoderConditions.BUILD_SUCCEEDED}
    )
    public BuildResult buildWithCommand(SoftwareProject project, OperationContext context) {
        Pair<String, Long> timeResult = TimeKt.time(() -> context.promptRunner(
                coderProperties.getPrimaryCodingLlm(),
                emptySet(),
                emptyList(),
                Arrays.asList(project),
                emptyList(),
                false
        ).generateText("Build the project"));

        String rawOutput = timeResult.getFirst();
        Long ms = timeResult.getSecond();

        return project.getCi().parseBuildOutput(rawOutput, Duration.ofMillis(ms));
    }

    /**
     * Standard build method with lower cost than buildWithCommand
     * Triggered by the BuildNeeded condition after code modifications
     */
    @Action(
            cost = 500.0,
            canRerun = true,
            pre = {CoderConditions.BUILD_NEEDED},
            post = {CoderConditions.BUILD_SUCCEEDED}
    )
    public BuildResult build(SoftwareProject project) {
        return project.build();
    }

    /**
     * Condition that determines if a build is needed
     * Triggered when the last action was a code modification
     */
    @Condition(name = CoderConditions.BUILD_NEEDED)
    public boolean buildNeeded(OperationContext context) {
        Object last = context.lastResult();
        return last instanceof CodeModificationReport ||
                (last instanceof SoftwareProject && ((SoftwareProject) last).getWasCreated());
    }

    /**
     * Condition that checks if the last action was a build
     * Used to determine the next step in the flow
     */
    @Condition(name = CoderConditions.BUILD_WAS_LAST_ACTION)
    public boolean buildWasLastAction(OperationContext context) {
        return context.lastResult() instanceof BuildResult;
    }

    /**
     * Condition that checks if the build was successful
     * Used to determine if the agent should proceed to sharing the report
     */
    @Condition(name = CoderConditions.BUILD_SUCCEEDED)
    public boolean buildSucceeded(BuildResult buildResult) {
        return buildResult.getStatus() != null && buildResult.getStatus().getSuccess();
    }

    /**
     * Condition that checks if the build failed
     * Used to determine if the agent should attempt to fix the build
     */
    @Condition(name = CoderConditions.BUILD_FAILED)
    public boolean buildFailed(BuildResult buildResult) {
        return buildResult.getStatus() != null && !buildResult.getStatus().getSuccess();
    }

    /**
     * Core action that modifies code based on the user request
     * Sets the BuildNeeded condition after completion
     */
    @Action(
            canRerun = true,
            post = {CoderConditions.BUILD_NEEDED},
            toolGroups = {CoreToolGroups.WEB}
    )
    public CodeModificationReport modifyProject(
            CodeModificationRequest codeModificationRequest,
            SoftwareProject project,
            OperationContext context,
            Conversation conversation) {

        logger.info("✎ Modifying code according to request: {}", codeModificationRequest.getRequest());

        boolean isFirstModification = context.count(CodeModificationRequest.class) == 1;
        if (isFirstModification) {
            logWriter.logRequest(codeModificationRequest, project);
        }

        project.flushChanges();

        // SoftwareProject tools are automatically added because it's a parameter to this function
        List<PromptContributor> promptContributors = listOfNotNull(
                project,
                coderProperties.codeModificationDirections(),
                conversation.promptContributor(new WindowingConversationFormatter(SimpleMessageFormatter.INSTANCE, 100))
        );

        String report = context.promptRunner(
                coderProperties.getPrimaryCodingLlm(),
                emptySet(), emptyList(),
                promptContributors.stream()
                        .filter(java.util.Objects::nonNull)
                        .toList(), emptyList(), false
        ).createObject(
                "Execute the following user request to modify code in the given project.\n" +
                        "Use the project information to help you understand the code.\n" +
                        "The project will be in git so you can safely modify content without worrying about backups.\n" +
                        "Return an explanation of what you did and why.\n\n" +
                        "DO NOT ASK FOR USER INPUT: DO WHAT YOU THINK IS NEEDED TO MODIFY THE PROJECT.\n\n" +
                        "DO NOT BUILD THE PROJECT UNLESS THE USER HAS REQUESTED IT\n" +
                        "AND IT IS NECESSARY TO DECIDE WHAT TO MODIFY.\n" +
                        "IF BUILDING IS NEEDED, BE SURE TO RUN UNIT TESTS.\n" +
                        "DO NOT BUILD *AFTER* MODIFYING CODE.\n\n" +
                        "User request:\n" +
                        "\"" + codeModificationRequest.getRequest() + "\"\n" +
                        "}",
                String.class
        );

        List<String> filesChanged = project.getChanges().stream()
                .map(FileWriteTools.FileModification::getPath)
                .collect(Collectors.toList());

        return new CodeModificationReport(report, filesChanged);
    }

    /**
     * Action to fix a broken build
     * Triggered when the build fails after code modifications
     * Uses a specialized LLM (fixCodingLlm) to address build failures
     */
    @Action(
            canRerun = true,
            pre = {CoderConditions.BUILD_FAILED, CoderConditions.BUILD_WAS_LAST_ACTION},
            post = {CoderConditions.BUILD_SUCCEEDED},
            toolGroups = {CoreToolGroups.WEB}
    )
    public CodeModificationReport fixBrokenBuild(
            CodeModificationRequest codeModificationRequest,
            SoftwareProject project,
            BuildResult buildFailure,
            Conversation conversation,
            OperationContext context) {

        project.flushChanges();

        List<PromptContributor> promptContributors = listOfNotNull(
                project,
                buildFailure,
                coderProperties.codeModificationDirections()
        );


        String report = context.promptRunner(
                coderProperties.getFixCodingLlm(), emptySet(), emptyList(),
                promptContributors.stream()
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList()), emptyList(), false
        ).createObject(
                "Modify code in the given project to fix the broken build.\n\n" +
                        "Use the project information to help you understand the code.\n" +
                        "The project will be in git so you can safely modify content without worrying about backups.\n" +
                        "Return an explanation of what you did and why.\n" +
                        "Consider the build failure report.\n\n" +
                        "DO NOT BUILD THE PROJECT. JUST MODIFY CODE.\n" +
                        "Consider the following user request for the necessary functionality:\n" +
                        "\"" + codeModificationRequest.getRequest() + "\"",
                String.class
        );

        List<String> filesChanged = project.getChanges().stream()
                .map(FileWriteTools.FileModification::getPath)
                .collect(Collectors.toList());

        return new CodeModificationReport(report, filesChanged);
    }

    /**
     * Final step in the agent flow
     * Returns the code modification completion report to the user
     * Only triggered when the build is successful (or not needed)
     */
    @Action(pre = {CoderConditions.BUILD_SUCCEEDED})
    @AchievesGoal(description = "Modify project code as per code modification request")
    public SuccessfulCodeModification shareCodeModificationReport(
            CodeModificationReport codeModificationReport,
            SoftwareProject softwareProject,
            OperationContext operationContext) {

        String suggestedCommitMessage = operationContext.promptRunner(
                coderProperties.getPrimaryCodingLlm(), emptySet(), emptyList(), emptyList(), emptyList(), false
        ).generateText(
                "Generate a concise git commit message for the following code modification report:\n" +
                        codeModificationReport.getText()
        );

        logger.info("Sharing code modification report: {}", codeModificationReport.getText());

        SuccessfulCodeModification success = new SuccessfulCodeModification(
                new CodeModificationRequest(codeModificationReport.getText()),
                codeModificationReport,
                suggestedCommitMessage
        );

        logWriter.logResponse(success, softwareProject);
        return success;
    }
}
