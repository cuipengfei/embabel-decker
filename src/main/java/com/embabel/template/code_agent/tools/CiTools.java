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

import com.embabel.agent.api.common.support.SelfToolCallbackPublisher;
import com.embabel.agent.tools.DirectoryBased;
import org.springframework.ai.tool.annotation.Tool;

/**
 * Interface for Continuous Integration tools that enable building and testing projects.
 * <p>
 * The CiTools interface provides functionality for executing build commands within a project's
 * root directory. It serves as a bridge between the agent and the underlying build system,
 * allowing for seamless integration with various build tools like Maven, Gradle, npm, etc.
 * <p>
 * This interface extends:
 * - SelfToolCallbackPublisher: Automatically publishes methods annotated with @Tool to be
 * available as callable tools by the agent system. This enables the methods to be discovered
 * and invoked through the tool callback mechanism.
 * - DirectoryBased: Provides access to the root directory on the host machine where the
 * project is located. This allows build commands to be executed in the correct context.
 * <p>
 * Implementations of this interface are expected to handle the execution of build commands
 * and process their output appropriately.
 *
 * @see SelfToolCallbackPublisher
 * @see DirectoryBased
 * @see Ci
 * @see BuildOptions
 */
public interface CiTools extends SelfToolCallbackPublisher, DirectoryBased {

    /**
     * Builds the project using the specified command.
     * <p>
     * This method executes the provided build command in the project's root directory.
     * It leverages the Ci implementation to handle the actual command execution and
     * returns the output (stdout/stderr) from the build process.
     * <p>
     * The method is annotated with @Tool to make it available as a callable tool
     * within the agent system.
     *
     * @param command The build command to execute in the project root (e.g., "mvn clean install",
     *                "gradle build", "npm run build", etc.)
     * @return The output of the build process as a string, containing both stdout and stderr
     * @throws RuntimeException if the build process fails or cannot be executed
     */
    @Tool(description = "build the project using the given command in the root")
    default String buildProject(String command) {
        return new Ci(getRoot()).build(new BuildOptions(command, true));
    }
}
