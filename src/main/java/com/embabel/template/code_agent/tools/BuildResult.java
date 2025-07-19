package com.embabel.template.code_agent.tools;

import com.embabel.common.ai.prompt.PromptContribution;
import com.embabel.common.ai.prompt.PromptContributionLocation;
import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.ai.prompt.PromptElement;
import com.embabel.common.core.types.Timed;
import com.embabel.common.core.types.Timestamped;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public final class BuildResult implements Timestamped, Timed, PromptContributor {

    private final BuildStatus status;
    private final String rawOutput;
    private final Instant timestamp;
    private final Duration runningTime;

    public BuildResult(BuildStatus status, String rawOutput, Duration runningTime) {
        this(status, rawOutput, Instant.now(), runningTime);
    }

    public BuildResult(BuildStatus status, String rawOutput, Instant timestamp, Duration runningTime) {
        this.status = status;
        this.rawOutput = rawOutput;
        this.timestamp = timestamp;
        this.runningTime = runningTime;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public Duration getRunningTime() {
        return runningTime;
    }

    @Override
    public String contribution() {
        StringBuilder sb = new StringBuilder();
        sb.append("Build result: success=").append(status != null ? status.getSuccess() : "unknown").append("\n");
        sb.append("Relevant output:\n");
        sb.append(relevantOutput());
        return sb.toString();
    }

    public String relevantOutput() {
        return status != null ? status.getRelevantOutput() : rawOutput;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BuildResult that = (BuildResult) obj;
        return java.util.Objects.equals(status, that.status) &&
                rawOutput.equals(that.rawOutput) &&
                runningTime.equals(that.runningTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(status, rawOutput, runningTime);
    }

    @Override
    public String toString() {
        return "BuildResult{" +
                "status=" + status +
                ", rawOutput='" + rawOutput + '\'' +
                ", runningTime=" + runningTime +
                '}';
    }

    //next

    @NotNull
    @Override
    public PromptContribution promptContribution() {
        return DefaultImpls.promptContribution(this);
    }

    @Nullable
    @Override
    public String getRole() {
        return PromptElement.DefaultImpls.getRole(this);
    }

    @NotNull
    @Override
    public PromptContributionLocation getPromptContributionLocation() {
        return PromptElement.DefaultImpls.getPromptContributionLocation(this);
    }
}
