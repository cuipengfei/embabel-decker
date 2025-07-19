package com.embabel.template.code_agent.agent;

import com.embabel.agent.domain.library.HasContent;
import com.embabel.agent.domain.library.InternetResource;
import com.embabel.agent.domain.library.InternetResources;
import com.embabel.common.ai.prompt.PromptContribution;
import com.embabel.common.ai.prompt.PromptContributionLocation;
import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.ai.prompt.PromptElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class CodeExplanation implements HasContent, InternetResources {

    private final String text;
    private final List<InternetResource> links;

    public CodeExplanation(String text, List<InternetResource> links) {
        this.text = text;
        this.links = links;
    }

    public String getText() {
        return text;
    }

    @Override
    public List<InternetResource> getLinks() {
        return links;
    }

    @Override
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        sb.append("\n\n");
        sb.append(links.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")));
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CodeExplanation that = (CodeExplanation) obj;
        return text.equals(that.text) && links.equals(that.links);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(text, links);
    }

    @Override
    public String toString() {
        return "CodeExplanation{" +
                "text='" + text + '\'' +
                ", links=" + links +
                '}';
    }

    @NotNull
    @Override
    public String contribution() {
        return DefaultImpls.contribution(this);
    }

    @NotNull
    @Override
    public PromptContribution promptContribution() {
        return PromptContributor.DefaultImpls.promptContribution(this);
    }

    @Nullable
    @Override
    public String getRole() {
        return PromptElement.DefaultImpls.getRole(this);
    }

    @NotNull
    @Override
    public PromptContributionLocation getPromptContributionLocation() {
        return PromptElement.DefaultImpls.getPromptContributionLocation(this);
    }
}
