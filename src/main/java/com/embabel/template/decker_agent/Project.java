package com.embabel.template.decker_agent;

import com.embabel.agent.tools.file.FileReadTools;
import com.embabel.agent.tools.file.WellKnownFileContentTransformers;
import com.embabel.common.util.StringTransformer;
import kotlin.text.Regex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.tool.ToolCallback;

import java.nio.file.Path;
import java.util.List;

class Project implements FileReadTools, SymbolSearch {

    private final String root;
    private final List<StringTransformer> fileContentTransformers = List.of(WellKnownFileContentTransformers.INSTANCE.getRemoveApacheLicenseHeader());

    public Project(String root) {
        this.root = root;
    }

    @Override
    @NotNull
    public String getRoot() {
        return root;
    }

    @Override
    @NotNull
    public List<StringTransformer> getFileContentTransformers() {
        return fileContentTransformers;
    }

    @Override
    public boolean exists() {
        return FileReadTools.DefaultImpls.exists(this);
    }

    @NotNull
    @Override
    public List<String> findFiles(@NotNull String glob) {
        return FileReadTools.DefaultImpls.findFiles(this, glob);
    }

    @NotNull
    @Override
    public List<String> findFiles(@NotNull String glob, boolean findHighest) {
        return FileReadTools.DefaultImpls.findFiles(this, glob, findHighest);
    }

    @Nullable
    @Override
    public String safeReadFile(@NotNull String path) {
        return FileReadTools.DefaultImpls.safeReadFile(this, path);
    }

    @NotNull
    @Override
    public String readFile(@NotNull String path) {
        return FileReadTools.DefaultImpls.readFile(this, path);
    }

    @NotNull
    @Override
    public List<String> listFiles(@NotNull String path) {
        return FileReadTools.DefaultImpls.listFiles(this, path);
    }

    @NotNull
    @Override
    public Path resolvePath(@NotNull String path) {
        return FileReadTools.DefaultImpls.resolvePath(this, path);
    }

    @NotNull
    @Override
    public Path resolveAndValidateFile(@NotNull String path) {
        return FileReadTools.DefaultImpls.resolveAndValidateFile(this, path);
    }

    @NotNull
    @Override
    public List<ToolCallback> getToolCallbacks() {
        return FileReadTools.DefaultImpls.getToolCallbacks(this);
    }

    @NotNull
    @Override
    public String findPatternInProject(@NotNull String pattern, @NotNull String globPattern) {
        return SymbolSearch.DefaultImpls.findPatternInProject(this, pattern, globPattern);
    }

    @NotNull
    @Override
    public List<PatternMatch> findPatternInProject(@NotNull Regex pattern, @NotNull String globPattern, boolean useParallelSearch) {
        return SymbolSearch.DefaultImpls.findPatternInProject(this, pattern, globPattern, useParallelSearch);
    }

    @Override
    public boolean matchesGlob(@NotNull String path, @NotNull String globPattern) {
        return SymbolSearch.DefaultImpls.matchesGlob(this, path, globPattern);
    }
}