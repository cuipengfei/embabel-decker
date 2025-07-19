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

import com.embabel.template.code_agent.domain.SoftwareProject;
import com.embabel.template.code_agent.domain.TaskFocus;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class CodingCommands {

    private final TaskFocus taskFocus;

    public CodingCommands(TaskFocus taskFocus) {
        this.taskFocus = taskFocus;
    }

    @ShellMethod("Get current task focus")
    public String focus() {
        SoftwareProject project = taskFocus.getSoftwareProject();
        return project != null ? project.getRoot() : "No project is currently focused.";
    }

    @ShellMethod("Set current task focus")
    public String setFocus(@ShellOption String name) {
        SoftwareProject project = taskFocus.setFocus(name);
        return project != null ? project.getRoot() : "No project found with name containing '" + name + "'.";
    }
}
