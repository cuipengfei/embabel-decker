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
import com.embabel.agent.api.annotation.ActionMethodPromptRunnerKt;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.template.code_agent.domain.SoftwareProject;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.List;

@Agent(
        description = "Explain code in a software project or directory structure"
)
@Profile("!test")
public class CodeExplainer {

    private final CoderProperties coderProperties;

    public CodeExplainer(CoderProperties coderProperties) {
        this.coderProperties = coderProperties;
    }

    @Action
    @AchievesGoal(description = "Code has been explained to the user")
    public CodeExplanation explainCode(UserInput userInput, SoftwareProject project) {
        // Note: This uses Kotlin interop for the `using` function and `create` extension
        // The equivalent Java code would use the Spring AI framework directly

        return ActionMethodPromptRunnerKt.using(
                coderProperties.getPrimaryCodingLlm(),
                Collections.emptySet(),
                Collections.emptyList(),
                List.of(project), Collections.emptyList(), null
        ).createObject(
                "Execute the following user request to explain something about the given project.\n" +
                        "Use the file tools to read code and directories.\n" +
                        "Use the project information to help you understand the code.\n" +
                        "List any resources from the internet that will help the user\n" +
                        "understand any complex concepts and provide useful background reading.\n" +
                        "For example, provide links for a potentially unfamiliar algorithm.\n\n" +
                        "User request:\n" +
                        "\"" + userInput.getContent() + "\"\n" +
                        "}",
                CodeExplanation.class
        );
    }
}
