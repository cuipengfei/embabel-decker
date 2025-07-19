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

import com.embabel.agent.config.models.OpenAiModels;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.ModelSelectionCriteria;
import com.embabel.common.ai.prompt.PromptContributor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties(prefix = "embabel.coder")
public class CoderProperties {

    private final String primaryCodingModel;
    private final String fixCodingModel;
    private final String projectRoot;
    private final boolean findNestedProjects;
    private final String defaultProject;
    private final String codeModificationDirections;
    private final File root;
    private final LlmOptions primaryCodingLlm;
    private final LlmOptions fixCodingLlm;

    public CoderProperties() {
        this(OpenAiModels.GPT_41,
                OpenAiModels.GPT_41,
                null,
                false,
                null,
                getDefaultCodeModificationDirections());
    }

    public CoderProperties(String primaryCodingModel,
                           String fixCodingModel,
                           String projectRoot,
                           boolean findNestedProjects,
                           String defaultProject) {
        this(primaryCodingModel, fixCodingModel, projectRoot, findNestedProjects, defaultProject, getDefaultCodeModificationDirections());
    }

    public CoderProperties(String primaryCodingModel,
                           String fixCodingModel,
                           String projectRoot,
                           boolean findNestedProjects,
                           String defaultProject,
                           String codeModificationDirections) {
        this.primaryCodingModel = primaryCodingModel;
        this.fixCodingModel = fixCodingModel;
        this.projectRoot = projectRoot;
        this.findNestedProjects = findNestedProjects;
        this.defaultProject = defaultProject;
        this.codeModificationDirections = codeModificationDirections;

        this.root = new File(projectRoot != null ? projectRoot : System.getProperty("user.dir")).getParentFile();

        this.primaryCodingLlm = LlmOptions.Companion.invoke(
                ModelSelectionCriteria.Companion.byName(primaryCodingModel)
        );

        this.fixCodingLlm = LlmOptions.Companion.invoke(
                ModelSelectionCriteria.Companion.byName(fixCodingModel)
        );
    }

    private static String getDefaultCodeModificationDirections() {
        return "Use the file tools to read code and directories.\n" +
                "Use the web tools if you are asked to use a technology you don't know about.\n" +
                "ALWAYS LOOK FOR THE FILES IN THE PROJECT LOCALLY USING FILE TOOLS, NOT THE WEB OR GITHUB.\n" +
                "Make multiple small, focused edits using the editFile tool.";
    }

    public String getPrimaryCodingModel() {
        return primaryCodingModel;
    }

    public String getFixCodingModel() {
        return fixCodingModel;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public boolean getFindNestedProjects() {
        return findNestedProjects;
    }

    public String getDefaultProject() {
        return defaultProject;
    }

    public File getRoot() {
        return root;
    }

    public PromptContributor codeModificationDirections() {
        return PromptContributor.fixed(codeModificationDirections);
    }

    /**
     * Primary coding Llm
     */
    public LlmOptions getPrimaryCodingLlm() {
        return primaryCodingLlm;
    }

    public LlmOptions getFixCodingLlm() {
        return fixCodingLlm;
    }
}
