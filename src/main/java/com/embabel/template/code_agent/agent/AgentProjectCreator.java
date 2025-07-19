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

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.AgentCapabilities;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.template.code_agent.domain.SoftwareProject;
import com.embabel.template.code_agent.domain.TaskFocus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.embabel.agent.api.annotation.WaitKt.fromForm;

@AgentCapabilities(scan = true)
@Profile("!test")
public class AgentProjectCreator {

    private final Logger logger = LoggerFactory.getLogger(AgentProjectCreator.class);
    private final CoderProperties coderProperties;
    private final TaskFocus taskFocus;

    public AgentProjectCreator(CoderProperties coderProperties, TaskFocus taskFocus, CoderProperties properties) {
        this.coderProperties = coderProperties;
        this.taskFocus = taskFocus;
    }

    @Action
    public AgentRequirements askUserForAgentRequirements() {

        // Note: This uses Kotlin interop for the `fromForm` function
        return fromForm("Agent requirements", AgentRequirements.class);
    }

    @Action(post = {CoderConditions.BUILD_NEEDED})
    public SoftwareProject createAgentProject(AgentRequirements requirements, OperationContext context) {
        logger.info("Creating Agent project named {}", requirements.getProjectName());

        File workDir = coderProperties.getRoot();
        SoftwareProject newAgentProject = createProject(workDir, requirements);
        taskFocus.saveAndSwitch(newAgentProject);
        return newAgentProject;
    }

    @Action(pre = {CoderConditions.BUILD_SUCCEEDED})
    @AchievesGoal(description = "Create a new Embabel agent project")
    public CodeExplanation describeShinyNewAgentProject(
            SoftwareProject softwareProject,
            AgentRequirements agentRequirements) {
        String text = "Project root: " + softwareProject.getRoot() + "\n" +
                "Technologies used: " + softwareProject.getTech() + "\n" +
                "Coding style: " + softwareProject.getCodingStyle();

        return new CodeExplanation(text, Collections.emptyList());
    }

    /**
     * Create a project under the working directory with the given requirements.
     */
    private SoftwareProject createProject(File workingDir, AgentRequirements requirements) {
        logger.info("Creating project under {}", workingDir.getAbsolutePath());

        ProjectCreator projectCreator = new ProjectCreator();
        List<String> additionalArgs = Arrays.asList(
                "--repo", "kotlin".equals(requirements.getLanguage()) ? "1" : "2",
                "--project-name", requirements.getProjectName(),
                "--package", requirements.getPackageName()
        );

        projectCreator.invokeProjectCreatorWithArgs(workingDir, additionalArgs);

        File projectDir = new File(workingDir, requirements.getProjectName());

        return new SoftwareProject(
                projectDir.getAbsolutePath(),
                null, // url
                "Kotlin, Spring Boot, Maven, Spring Web, Spring Actuator, Spring DevTools",
                "Modern Kotlin with Spring Boot conventions. Clean architecture with separation of concerns.",
                "mvn test",
                true // wasCreated
        );
    }
}

