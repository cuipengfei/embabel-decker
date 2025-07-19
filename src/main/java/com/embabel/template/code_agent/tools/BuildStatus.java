package com.embabel.template.code_agent.tools;

public final class BuildStatus {

    private final boolean success;
    private final String relevantOutput;

    public BuildStatus(boolean success, String relevantOutput) {
        this.success = success;
        this.relevantOutput = relevantOutput;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getRelevantOutput() {
        return relevantOutput;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BuildStatus that = (BuildStatus) obj;
        return success == that.success && relevantOutput.equals(that.relevantOutput);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(success, relevantOutput);
    }

    @Override
    public String toString() {
        return "BuildStatus{" +
                "success=" + success +
                ", relevantOutput='" + relevantOutput + '\'' +
                '}';
    }
}
