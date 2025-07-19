package com.embabel.template.code_agent.agent.support;

import com.embabel.template.code_agent.agent.LogWriter;
import com.embabel.template.code_agent.domain.CodeModificationRequest;
import com.embabel.template.code_agent.domain.SoftwareProject;
import com.embabel.template.code_agent.domain.SuccessfulCodeModification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * Write to the /.embabel/log.jsonl file in the focus project
 */
@Service
public class InProjectLogWriter implements LogWriter {

    private String path;
    private ObjectMapper objectMapper;

    public InProjectLogWriter() {
    }

    public InProjectLogWriter(ObjectMapper objectMapper) {
        this(".embabel/log.jsonl", objectMapper);
    }

    public InProjectLogWriter(String path, ObjectMapper objectMapper) {
        this.path = path;
        this.objectMapper = objectMapper;
    }

    @Override
    public void logRequest(CodeModificationRequest request, SoftwareProject softwareProject) {
        try {
            String jsonLine = objectMapper.writeValueAsString(request) + "\n";
            softwareProject.appendToFile(path, jsonLine, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to log request", e);
        }
    }

    @Override
    public void logResponse(SuccessfulCodeModification request, SoftwareProject softwareProject) {
        try {
            String jsonLine = objectMapper.writeValueAsString(request) + "\n";
            softwareProject.appendToFile(path, jsonLine, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to log response", e);
        }
    }
}
