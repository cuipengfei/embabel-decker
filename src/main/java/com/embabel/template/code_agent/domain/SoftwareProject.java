/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.template.code_agent.domain;

import com.embabel.agent.tools.file.*;
import com.embabel.common.ai.prompt.PromptContribution;
import com.embabel.common.ai.prompt.PromptContributionLocation;
import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.util.GetLoggerKt;
import com.embabel.common.util.StringTransformer;
import com.embabel.template.code_agent.tools.BuildOptions;
import com.embabel.template.code_agent.tools.BuildResult;
import com.embabel.template.code_agent.tools.Ci;
import com.embabel.template.code_agent.tools.SymbolSearch;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import kotlin.text.Regex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Open to allow extension
 */
@JsonClassDescription("Analysis of a technology project")
public class SoftwareProject implements PromptContributor, FileTools, SymbolSearch, FileChangeLog {

    public static final String DEFAULT_CODING_STYLE_GUIDE = ".embabel/coding-style.md";

    private final String root;
    private final String url;
    private final String tech;
    private final String defaultCodingStyle;
    private final String buildCommand;
    private final boolean wasCreated;
    private final Ci ci;
    private final FileChangeLog fileChangeLog;
    private final Logger logger;
    private DefaultFileChangeLog defaultFileChangeLog = new DefaultFileChangeLog();

    public SoftwareProject(String root) {
        this(root, null, "", getDefaultCodingStyleContent(root), "", false);
    }

    public SoftwareProject(String root, String url, String tech, String buildCommand) {
        this(root, url, tech, getDefaultCodingStyleContent(root), buildCommand, false);
    }

    public SoftwareProject(String root, String url, String tech, String defaultCodingStyle, String buildCommand, boolean wasCreated) {
        this.root = root;
        this.url = url;
        this.tech = tech;
        this.defaultCodingStyle = defaultCodingStyle;
        this.buildCommand = buildCommand;
        this.wasCreated = wasCreated;
        this.ci = new Ci(root);
        this.fileChangeLog = new DefaultFileChangeLog();
        this.logger = GetLoggerKt.logger();

        if (!exists()) {
            throw new IllegalArgumentException("Directory does not exist");
        }

        logger.info("Software project tools: {}",
                getToolCallbacks().stream()
                        .map(callback -> callback.getToolDefinition().name())
                        .sorted()
                        .collect(Collectors.toList())
        );
    }

    private static String getDefaultCodingStyleContent(String root) {
        return "No coding style guide found at " + DEFAULT_CODING_STYLE_GUIDE + ".\n" +
                "Try to follow the conventions of files you read in the project.";
    }

    @Override
    public String getRoot() {
        return root;
    }

    public String getUrl() {
        return url;
    }

    @JsonPropertyDescription("The technologies used in the project. List, comma separated. Include 10")
    public String getTech() {
        return tech;
    }

    @JsonPropertyDescription("Build command, such as 'mvn clean test'")
    public String getBuildCommand() {
        return buildCommand;
    }

    public boolean getWasCreated() {
        return wasCreated;
    }

    public String getCodingStyle() {
        String location = root + "/" + DEFAULT_CODING_STYLE_GUIDE;
        logger.info("Looking for coding style guide at '{}'", location);
        String content = safeReadFile(location);
        if (content != null) {
            logger.info("Found coding style guide at {}", location);
            return content;
        }
        return defaultCodingStyle;
    }

    @Override
    public List<StringTransformer> getFileContentTransformers() {
        return Arrays.asList(WellKnownFileContentTransformers.INSTANCE.getRemoveApacheLicenseHeader());
    }

    public Ci getCi() {
        return ci;
    }

    @Tool(description = "Returns the file containing a class with the given name")
    public String findClass(@ToolParam(description = "class name") String name) {
        List<PatternMatch> matches = findClassInProject(name, "**/*.{java,kt}");
        if (!matches.isEmpty()) {
            return matches.stream()
                    .map(PatternMatch::getRelativePath)
                    .collect(Collectors.joining("\n"));
        } else {
            return "No class found with name " + name;
        }
    }

    @Tool(description = "Returns the file containing a class with the given name")
    public String findPattern(
            @ToolParam(description = "regex pattern") String regex,
            @ToolParam(description = "glob pattern for file to search") String globPattern) {
        List<PatternMatch> matches = findPatternInProject(new Regex(regex), globPattern, true);
        if (!matches.isEmpty()) {
            return matches.stream()
                    .map(PatternMatch::getRelativePath)
                    .collect(Collectors.joining("\n"));
        } else {
            return "No matches for pattern '" + regex + "' in " + globPattern;
        }
    }

    @Tool(description = "Build the project using the given command in the root")
    public String build(String command) {
        BuildResult br = ci.buildAndParse(new BuildOptions(command, true));
        return br.relevantOutput();
    }

    public BuildResult build() {
        return ci.buildAndParse(new BuildOptions(buildCommand, true));
    }

    @Override
    public String toString() {
        return "SoftwareProject(" + root + ")";
    }

    @NotNull
    @Override
    public String contribution() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project:\n");
        sb.append(url != null ? url : "No URL").append("\n");
        sb.append(tech).append("\n\n");
        sb.append("Coding style:\n");
        sb.append(getCodingStyle());
        return sb.toString();
    }

    @NotNull
    @Override
    public List<FileModification> getChanges() {
        return fileChangeLog.getChanges();
    }

    @Override
    public void flushChanges() {
        fileChangeLog.flushChanges();
    }

    @Override
    public void recordChange(@NotNull FileModification c) {
        defaultFileChangeLog.recordChange(c);
    }

    @Override
    public boolean exists() {
        return FileTools.DefaultImpls.exists(this);
    }

    @NotNull
    @Override
    @Tool(description = "Find files using glob patterns. Return absolute paths")
    public List<String> findFiles(@NotNull String glob) {
        return FileTools.DefaultImpls.findFiles(this, glob);
    }

    @NotNull
    @Override
    public List<String> findFiles(@NotNull String glob, boolean findHighest) {
        return FileTools.DefaultImpls.findFiles(this, glob, findHighest);
    }

    @Nullable
    @Override
    public String safeReadFile(@NotNull String path) {
        return FileTools.DefaultImpls.safeReadFile(this, path);
    }

    @NotNull
    @Override
    @Tool(description = "Read a file at the relative path")
    public String readFile(@NotNull String path) {
        return FileTools.DefaultImpls.readFile(this, path);
    }

    @NotNull
    @Override
    @Tool(description = "List files and directories at a given path. Prefix is f: for file or d: for directory")
    public List<String> listFiles(@NotNull String path) {
        return FileTools.DefaultImpls.listFiles(this, path);
    }

    @NotNull
    @Override
    public Path resolvePath(@NotNull String path) {
        return FileTools.DefaultImpls.resolvePath(this, path);
    }

    @NotNull
    @Override
    public Path resolveAndValidateFile(@NotNull String path) {
        return FileTools.DefaultImpls.resolveAndValidateFile(this, path);
    }

    @NotNull
    @Override
    @Tool(description = "Create a file with the given content")
    public String createFile(@NotNull String path, @NotNull String content) {
        return FileTools.DefaultImpls.createFile(this, path, content);
    }

    @Override
    public void createFile(@NotNull String path, @NotNull String content, boolean overwrite) {
        FileTools.DefaultImpls.createFile(this, path, content, overwrite);
    }

    @NotNull
    @Override
    @Tool(description = "Edit the file at the given location. Replace oldContent with newContent. oldContent is typically just a part of the file. e.g. use it to replace a particular method to add another method")
    public String editFile(@NotNull String path,
                           @NotNull @ToolParam(description = "content to replace") String oldContent,
                           @NotNull @ToolParam(description = "replacement content") String newContent) {
        return FileTools.DefaultImpls.editFile(this, path, oldContent, newContent);
    }

    @NotNull
    @Override
    @Tool(description = "Create a directory at the given path")
    public String createDirectory(@NotNull String path) {
        return FileTools.DefaultImpls.createDirectory(this, path);
    }

    @NotNull
    @Override
    @Tool(description = "Append content to an existing file. The file must already exist.")
    public String appendFile(@NotNull String path, @NotNull String content) {
        return FileTools.DefaultImpls.appendFile(this, path, content);
    }

    @Override
    public void appendToFile(@NotNull String path, @NotNull String content, boolean createIfNotExists) {
        FileTools.DefaultImpls.appendToFile(this, path, content, createIfNotExists);
    }

    @NotNull
    @Override
    @Tool(description = "Delete a file at the given path")
    public String delete(@NotNull String path) {
        return FileTools.DefaultImpls.delete(this, path);
    }

    @NotNull
    @Override
    public List<ToolCallback> getToolCallbacks() {
        return FileTools.DefaultImpls.getToolCallbacks(this);
    }

    @NotNull
    @Override
    @Tool(description = "search for a regex in the project")
    public String findPatternInProject(@NotNull String pattern, @NotNull String globPattern) {
        return PatternSearch.DefaultImpls.findPatternInProject(this, pattern, globPattern);
    }

    @NotNull
    @Override
    public List<PatternMatch> findPatternInProject(@NotNull Regex pattern, @NotNull String globPattern, boolean useParallelSearch) {
        return PatternSearch.DefaultImpls.findPatternInProject(this, pattern, globPattern, useParallelSearch);
    }

    @Override
    public boolean matchesGlob(@NotNull String path, @NotNull String globPattern) {
        return PatternSearch.DefaultImpls.matchesGlob(this, path, globPattern);
    }

    @NotNull
    @Override
    public PromptContribution promptContribution() {
        return PromptContributor.DefaultImpls.promptContribution(this);
    }

    @Nullable
    @Override
    public String getRole() {
        return PromptContributor.DefaultImpls.getRole(this);
    }

    @NotNull
    @Override
    public PromptContributionLocation getPromptContributionLocation() {
        return PromptContributor.DefaultImpls.getPromptContributionLocation(this);
    }
}
