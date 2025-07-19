package com.embabel.template.code_agent.tools;

/**
 * Options for build
 *
 * @param buildCommand command to run such as "mvn test"
 * @param streamOutput if true, the output will be streamed to the console
 */
public final class BuildOptions {

    private final String buildCommand;
    private final boolean streamOutput;

    public BuildOptions(String buildCommand) {
        this(buildCommand, false);
    }

    public BuildOptions(String buildCommand, boolean streamOutput) {
        this.buildCommand = buildCommand;
        this.streamOutput = streamOutput;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public boolean getStreamOutput() {
        return streamOutput;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BuildOptions that = (BuildOptions) obj;
        return streamOutput == that.streamOutput && buildCommand.equals(that.buildCommand);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(buildCommand, streamOutput);
    }

    @Override
    public String toString() {
        return "BuildOptions{" +
                "buildCommand='" + buildCommand + '\'' +
                ", streamOutput=" + streamOutput +
                '}';
    }
}
