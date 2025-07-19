package com.embabel.template.code_agent.tools;

interface BuildSystemIntegration {

    /**
     * If possible, parse the build result
     */
    BuildStatus parseBuildOutput(String root, String rawOutput);
}
