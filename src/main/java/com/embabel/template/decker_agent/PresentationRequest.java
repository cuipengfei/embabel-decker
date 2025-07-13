package com.embabel.template.decker_agent;

import com.embabel.agent.prompt.persona.CoStar;
import com.embabel.common.ai.prompt.PromptContribution;
import com.embabel.common.ai.prompt.PromptContributionLocation;
import com.embabel.common.ai.prompt.PromptContributor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

class PresentationRequest implements PromptContributor {
    private int slideCount;
    private String presenterBio;
    private String brief;
    private String softwareProject;
    private String outputDirectory = "/Users/rjohnson/Documents";
    private String outputFile = "presentation.md";
    private String header;
    private Map<String, ImageInfo> images = Map.of();
    private boolean autoIllustrate;
    private CoStar coStar;

    @JsonIgnore
    private Project project;

    public PresentationRequest(int slideCount, String presenterBio,
                               String brief, String softwareProject,
                               String outputDirectory, String outputFile,
                               String header, Map<String, ImageInfo> images,
                               boolean autoIllustrate, CoStar coStar) {
        this.slideCount = slideCount;
        this.presenterBio = presenterBio;
        this.brief = brief;
        this.softwareProject = softwareProject;
        this.outputDirectory = outputDirectory;
        this.outputFile = outputFile;
        this.header = header;
        this.images = images;
        this.autoIllustrate = autoIllustrate;
        this.coStar = coStar;
        this.project = softwareProject != null ? new Project(softwareProject) : null;
    }

    public PresentationRequest() {
    }

    public int getSlideCount() {
        return slideCount;
    }

    public String getPresenterBio() {
        return presenterBio;
    }

    public String getBrief() {
        return brief;
    }

    public String getSoftwareProject() {
        return softwareProject;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getHeader() {
        return header;
    }

    public Map<String, ImageInfo> getImages() {
        return images;
    }

    public boolean isAutoIllustrate() {
        return autoIllustrate;
    }

    public CoStar getCoStar() {
        return coStar;
    }

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public String rawOutputFile() {
        return outputFile.replace(".md", ".raw.md");
    }

    public String withDiagramsOutputFile() {
        return outputFile.replace(".md", ".withDiagrams.md");
    }

    @NotNull
    @Override
    public PromptContribution promptContribution() {
        return coStar.promptContribution();
    }

    @NotNull
    @Override
    public String contribution() {
        return coStar.contribution();
    }

    @Nullable
    @Override
    public String getRole() {
        return coStar.getRole();
    }

    @NotNull
    @Override
    public PromptContributionLocation getPromptContributionLocation() {
        return coStar.getPromptContributionLocation();
    }
}
