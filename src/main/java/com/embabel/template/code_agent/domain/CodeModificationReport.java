package com.embabel.template.code_agent.domain;

import com.embabel.agent.domain.library.HasContent;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * What the agent did to modify the code.
 * Note that this might not be the final report,
 * as the agent might need to build the project
 * and fix any issues that arise.
 */
public final class CodeModificationReport implements HasContent {

    @JsonPropertyDescription("Report of the modifications made to code")
    private final String text;
    private final List<String> filesChanged;

    public CodeModificationReport(String text, List<String> filesChanged) {
        this.text = text;
        this.filesChanged = filesChanged;
    }

    public String getText() {
        return text;
    }

    public List<String> getFilesChanged() {
        return filesChanged;
    }

    @Override
    public String getContent() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CodeModificationReport that = (CodeModificationReport) obj;
        return text.equals(that.text) && filesChanged.equals(that.filesChanged);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(text, filesChanged);
    }

    @Override
    public String toString() {
        return "CodeModificationReport{" +
                "text='" + text + '\'' +
                ", filesChanged=" + filesChanged +
                '}';
    }
}
