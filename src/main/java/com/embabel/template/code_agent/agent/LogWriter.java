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

import com.embabel.template.code_agent.domain.CodeModificationRequest;
import com.embabel.template.code_agent.domain.SoftwareProject;
import com.embabel.template.code_agent.domain.SuccessfulCodeModification;

/**
 * Log the changes we've made to the codebase.
 */
public interface LogWriter {

    /**
     * Write a log entry for a code modification request.
     */
    void logRequest(
            CodeModificationRequest request,
            SoftwareProject softwareProject
    );

    /**
     * Write a log entry for a successful code modification response.
     */
    void logResponse(
            SuccessfulCodeModification request,
            SoftwareProject softwareProject
    );
}
