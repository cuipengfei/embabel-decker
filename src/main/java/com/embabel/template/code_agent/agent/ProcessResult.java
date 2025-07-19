package com.embabel.template.code_agent.agent;

/**
 * Data class to hold the result of process execution
 */
record ProcessResult(int exitCode, String stdout, String stderr) {

    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessResult that = (ProcessResult) obj;
        return exitCode == that.exitCode &&
                stdout.equals(that.stdout) &&
                stderr.equals(that.stderr);
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
                "exitCode=" + exitCode +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                '}';
    }
}
