package com.embabel.template.code_agent.domain;

import com.embabel.common.core.NameGeneratorKt;
import com.embabel.common.core.types.Timestamped;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.time.Instant;

public final class CodeModificationRequest implements Timestamped, LogEntry {

    @JsonPropertyDescription("Request to modify code")
    private final String request;
    private final String id;
    private final Instant timestamp;

    public CodeModificationRequest(String request) {

        this(request, NameGeneratorKt.getMobyNameGenerator().generateName());
    }

    public CodeModificationRequest(String request, String id) {
        this.request = request;
        this.id = id;
        this.timestamp = Instant.now();
    }

    public String getRequest() {
        return request;
    }

    public String getId() {
        return id;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CodeModificationRequest that = (CodeModificationRequest) obj;
        return request.equals(that.request) && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(request, id);
    }

    @Override
    public String toString() {
        return "CodeModificationRequest{" +
                "request='" + request + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

