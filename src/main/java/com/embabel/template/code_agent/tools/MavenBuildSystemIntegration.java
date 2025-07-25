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

import java.util.stream.Collectors;

public class MavenBuildSystemIntegration implements BuildSystemIntegration {

    @Override
    public BuildStatus parseBuildOutput(String root, String rawOutput) {
        // TODO messy test
        if (!rawOutput.contains("[INFO]")) {
            // Not a Maven build
            return null;
        }

        boolean success = rawOutput.contains("BUILD SUCCESS");

        String warnings = rawOutput.lines()
                .filter(line -> line.contains("[WARNING]"))
                .collect(Collectors.joining("\n"));

        String errors = rawOutput.lines()
                .filter(line -> line.contains("[ERROR]"))
                .collect(Collectors.joining("\n"));

        String relevantOutput = warnings + "\n" + errors;

        return new BuildStatus(success, relevantOutput);
    }
}
