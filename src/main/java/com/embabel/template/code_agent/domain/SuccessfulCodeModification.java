package com.embabel.template.code_agent.domain;

import com.embabel.agent.domain.library.HasContent;
import com.embabel.common.core.types.Timed;
import com.embabel.common.core.types.Timestamped;

import java.time.Duration;
import java.time.Instant;

/**
 * Will be logged.
 */
public final class SuccessfulCodeModification implements Timestamped, Timed, HasContent, LogEntry {

    private final CodeModificationRequest request;
    private final CodeModificationReport report;
    private final String suggestedCommitMessage;
    private final Instant timestamp;

    public SuccessfulCodeModification(
            CodeModificationRequest request,
            CodeModificationReport report,
            String suggestedCommitMessage) {
        this.request = request;
        this.report = report;
        this.suggestedCommitMessage = suggestedCommitMessage;
        this.timestamp = Instant.now();
    }

    public CodeModificationRequest getRequest() {
        return request;
    }

    public CodeModificationReport getReport() {
        return report;
    }

    public String getSuggestedCommitMessage() {
        return suggestedCommitMessage;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public Duration getRunningTime() {
        return Duration.between(request.getTimestamp(), timestamp);
    }

    @Override
    public String getContent() {
        return "Code modification completed in " + getRunningTime().getSeconds() + " seconds\n" + report.getContent();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SuccessfulCodeModification that = (SuccessfulCodeModification) obj;
        return request.equals(that.request) &&
                report.equals(that.report) &&
                suggestedCommitMessage.equals(that.suggestedCommitMessage);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(request, report, suggestedCommitMessage);
    }

    @Override
    public String toString() {
        return "SuccessfulCodeModification{" +
                "request=" + request +
                ", report=" + report +
                ", suggestedCommitMessage='" + suggestedCommitMessage + '\'' +
                '}';
    }
}
