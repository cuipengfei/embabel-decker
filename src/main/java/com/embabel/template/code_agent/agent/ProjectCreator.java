package com.embabel.template.code_agent.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class ProjectCreator {

    /**
     * Invokes the project-creator tool with additional arguments
     *
     * @param workingDirectory The directory where the command should be executed
     * @param additionalArgs   Additional arguments to pass to project-creator
     * @param timeoutSeconds   Maximum time to wait for command completion
     * @return ProcessResult containing exit code, stdout, and stderr
     */
    public ProcessResult invokeProjectCreatorWithArgs(
            File workingDirectory,
            List<String> additionalArgs,
            long timeoutSeconds) {

        if (!workingDirectory.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + workingDirectory.getAbsolutePath());
        }
        if (!workingDirectory.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + workingDirectory.getAbsolutePath());
        }

        List<String> command = new ArrayList<>();
        command.add("uvx");
        command.add("--from");
        command.add("git+https://github.com/embabel/project-creator.git");
        command.add("project-creator");
        command.addAll(additionalArgs);

        return executeCommand(command, workingDirectory, timeoutSeconds);
    }

    public ProcessResult invokeProjectCreatorWithArgs(File workingDirectory, List<String> additionalArgs) {
        return invokeProjectCreatorWithArgs(workingDirectory, additionalArgs, 300);
    }

    private ProcessResult executeCommand(List<String> command, File workingDirectory, long timeoutSeconds) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(workingDirectory)
                    .redirectErrorStream(false);

            Process process = processBuilder.start();

            // Capture stdout and stderr
            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());

            // Wait for process completion with timeout
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new IOException("Command timed out after " + timeoutSeconds + " seconds");
            }

            return new ProcessResult(
                    process.exitValue(),
                    stdout,
                    stderr
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute command: " + String.join(" ", command), e);
        }
    }
}
