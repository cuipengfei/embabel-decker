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
import com.embabel.agent.api.annotation.WaitKt;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.tools.file.FileWriteTools;
import com.embabel.template.code_agent.domain.SoftwareProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

@AgentCapabilities(scan = false)
@Profile("!test")
public class SpringCoder {

    private final Logger logger = LoggerFactory.getLogger(SpringCoder.class);

    @Action
    public SpringRecipe askUserForSpringRecipe() {
        // Note: This uses Kotlin interop for the `fromForm` function
        return WaitKt.fromForm("Spring recipe", SpringRecipe.class);
    }

    @Action(post = {SpringCoderConditions.SPRING_PROJECT_CREATED})
    public SoftwareProject createSpringInitialzrProject(SpringRecipe springRecipe, ActionContext context) {
        logger.info("Creating Spring Initialzr project");

        File tempDir = FileWriteTools.Companion.createTempDir("spring-initializr");

        // Create RestClient to call Spring Initialzr
        RestClient restClient = RestClient.builder()
                .baseUrl("https://start.spring.io")
                .build();

        // Make the request to Spring Initialzr and save the response to a zip file
        File zipFile = new File(tempDir, springRecipe.getArtifactId() + ".zip");

        byte[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/starter.zip")
                        .queryParam("name", springRecipe.getProjectName())
                        .queryParam("groupId", springRecipe.getGroupId())
                        .queryParam("artifactId", springRecipe.getArtifactId())
                        .queryParam("version", springRecipe.getVersion())
                        .queryParam("bootVersion", springRecipe.getBootVersion())
                        .queryParam("language", springRecipe.getLanguage())
                        .queryParam("packaging", springRecipe.getPackaging())
                        .queryParam("javaVersion", springRecipe.getJavaVersion())
                        .queryParam("dependencies", springRecipe.getDependencies())
                        .build())
                .retrieve()
                .toEntity(byte[].class)
                .getBody();

        if (response == null) {
            throw new RuntimeException("Failed to download Spring Initialzr project");
        }

        // Save the response to a zip file
        try {
            Files.write(zipFile.toPath(), response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write zip file", e);
        }

        logger.info("Downloaded Spring Initialzr project to {}", zipFile.getAbsolutePath());

        File projectDir = FileWriteTools.Companion.extractZipFile(zipFile, tempDir, true);
        logger.info("Extracted Spring Initialzr project to {}", projectDir.getAbsolutePath());

        // Return the project coordinates
        context.setCondition(SpringCoderConditions.SPRING_PROJECT_CREATED, true);
        context.addObject(springRecipe);

        return new SoftwareProject(
                projectDir.getAbsolutePath(),
                null, // url
                "Kotlin, Spring Boot, Maven, Spring Web, Spring Actuator, Spring DevTools",
                "Modern Kotlin with Spring Boot conventions. Clean architecture with separation of concerns.",
                "mvn test",
                true // wasCreated
        );
    }

    @Action(
            pre = {SpringCoderConditions.SPRING_PROJECT_CREATED, CoderConditions.BUILD_SUCCEEDED}
    )
    @AchievesGoal(description = "Create a new Spring project")
    public CodeExplanation describeShinyNewSpringProject(
            SoftwareProject softwareProject,
            SpringRecipe springRecipe) {

        String text = "Project root: " + softwareProject.getRoot() + "\n" +
                "Technologies used: " + softwareProject.getTech() + "\n" +
                "Coding style: " + softwareProject.getCodingStyle();

        return new CodeExplanation(text, Collections.emptyList());
    }
}
