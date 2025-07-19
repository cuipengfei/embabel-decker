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
package com.embabel.template.code_agent.tools;

import com.embabel.agent.tools.DirectoryBased;
import com.embabel.common.util.GetLoggerKt;
import com.embabel.common.util.TimeKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * CI support with pluggable build systems
 */
public class Ci implements DirectoryBased {

    private final Logger logger = LoggerFactory.getLogger(Ci.class);
    private final String root;
    private final List<BuildSystemIntegration> buildSystemIntegrations;

    public Ci(String root) {
        this(root, Arrays.asList(new MavenBuildSystemIntegration()));
    }

    public Ci(String root, List<BuildSystemIntegration> buildSystemIntegrations) {
        this.root = root;
        this.buildSystemIntegrations = buildSystemIntegrations;
    }

    @Override
    public String getRoot() {
        return root;
    }

    /**
     * Build the project with the given command and parse the output
     * Parse status if known
     */
    public BuildResult buildAndParse(BuildOptions buildOptions) {
        var timeResult = TimeKt.time(() -> build(buildOptions));
        String rawOutput = timeResult.getFirst();
        Long ms = timeResult.getSecond();

        BuildResult buildResult = new BuildResult(
                null,
                rawOutput,
                Duration.ofMillis(ms)
        );

        BuildStatus buildStatus = parseOutput(buildResult.getRawOutput());
        return new BuildResult(buildStatus, rawOutput, buildResult.getTimestamp(), buildResult.getRunningTime());
    }

    public BuildResult parseBuildOutput(String rawOutput, Duration runningTime) {
        BuildResult buildResult = new BuildResult(
                null,
                rawOutput,
                runningTime
        );

        BuildStatus buildStatus = parseOutput(buildResult.getRawOutput());
        return new BuildResult(buildStatus, rawOutput, buildResult.getTimestamp(), buildResult.getRunningTime());
    }

    private BuildStatus parseOutput(String rawOutput) {
        for (BuildSystemIntegration integration : buildSystemIntegrations) {
            BuildStatus status = integration.parseBuildOutput(root, rawOutput);
            if (status != null) {
                return status;
            }
        }
        logger.warn("No build system understands this output");
        return null;
    }

    /**
     * Build the project using the given command
     */
    public String build(BuildOptions buildOptions) {
        logger.info("Running build command <{}> in root directory {}", buildOptions.getBuildCommand(), root);

        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the working directory to the root
        processBuilder.directory(Paths.get(root).toFile());

        // Configure the command
        String[] commandParts = buildOptions.getBuildCommand().split("\\s+");
        processBuilder.command(commandParts);

        // Redirect error stream to output stream
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            StringBuilder outputBuilder = new StringBuilder();

            // Handle the output differently based on streamOutput flag
            if (buildOptions.getStreamOutput()) {
                // Stream the output to the console while also capturing it
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        outputBuilder.append(line).append("\n");
                    }
                }
            } else {
                // Original behavior - just capture the output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                    }
                }
            }

            int exitCode = process.waitFor();
            String output = outputBuilder.toString();

            if (exitCode == 0) {
                return "Command executed successfully:\n" + output;
            } else {
                return "Command failed with exit code " + exitCode + ":\n" + output;
            }
        } catch (Exception e) {
            GetLoggerKt.logger().error("Error executing command: " + buildOptions.getBuildCommand(), e);
            return "Error executing command: " + e.getMessage();
        }
    }
}

