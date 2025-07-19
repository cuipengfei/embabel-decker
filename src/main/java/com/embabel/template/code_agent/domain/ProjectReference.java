package com.embabel.template.code_agent.domain;

import com.embabel.agent.tools.file.FileReadTools;
import com.embabel.agent.tools.file.WellKnownFileContentTransformers;
import com.embabel.common.util.StringTransformer;

import java.util.Arrays;
import java.util.List;

/**
 * Reference to another project
 */
abstract class ProjectReference implements Reference, FileReadTools {

    private final String name;
    private final String description;
    private final String root;

    public ProjectReference(String name, String description, String root) {
        this.name = name;
        this.description = description;
        this.root = root;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getRoot() {
        return root;
    }

    @Override
    public List<StringTransformer> getFileContentTransformers() {
        return Arrays.asList(WellKnownFileContentTransformers.INSTANCE.getRemoveApacheLicenseHeader());
    }

    @Override
    public String contribution() {
        return "Software project: " + name + "\n" +
                "Description: " + description + "\n" +
                "Root: " + root + "\n" +
                "Use file tools to read files in the project.";
    }
}
