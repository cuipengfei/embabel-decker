package com.embabel.template.code_agent.agent;

/**
 * Don't be stringly typed.
 */
public final class CoderConditions {
    public static final String BUILD_NEEDED = "buildNeeded";
    public static final String BUILD_FAILED = "buildFailed";
    public static final String BUILD_SUCCEEDED = "buildSucceeded";
    public static final String BUILD_WAS_LAST_ACTION = "buildWasLastAction";

    private CoderConditions() {
        // Utility class
    }
}
