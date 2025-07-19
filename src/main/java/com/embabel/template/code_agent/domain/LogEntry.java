package com.embabel.template.code_agent.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CodeModificationRequest.class),
        @JsonSubTypes.Type(value = SuccessfulCodeModification.class)
})
public interface LogEntry {
}
