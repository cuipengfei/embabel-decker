package com.embabel.template.decker_agent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.ActionMethodPromptRunnerKt;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.RequireNameMatch;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.dsl.MapperKt;
import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.domain.io.FileArtifact;
import com.embabel.agent.domain.library.CompletedResearch;
import com.embabel.agent.domain.library.ResearchReport;
import com.embabel.agent.domain.library.ResearchResult;
import com.embabel.agent.domain.library.ResearchTopics;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.ModelSelectionCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static kotlin.collections.CollectionsKt.emptyList;
import static kotlin.collections.SetsKt.emptySet;

@Agent(description = "Presentation maker. Build a presentation on a topic")
class PresentationMaker {

    private final SlideFormatter slideFormatter;
    private final FilePersister filePersister;
    private final PresentationMakerProperties properties;
    private final Logger logger = LoggerFactory.getLogger(PresentationMaker.class);

    public PresentationMaker(SlideFormatter slideFormatter, FilePersister filePersister, PresentationMakerProperties properties) {
        this.slideFormatter = slideFormatter;
        this.filePersister = filePersister;
        this.properties = properties;
    }

    @Action
    public ResearchTopics identifyResearchTopics(PresentationRequest presentationRequest) {
        String prompt = """
                Create a list of research topics for a presentation,
                based on the given input:
                %s
                About the presenter: %s
                """.formatted(presentationRequest.getBrief(), presentationRequest.getPresenterBio());

        return ActionMethodPromptRunnerKt.usingModel(properties.getCreationLlm())
                .createObject(prompt.trim(), ResearchTopics.class);
    }

    @Action
    public ResearchResult researchTopics(ResearchTopics researchTopics, PresentationRequest presentationRequest, OperationContext context) {
        List<ResearchReport> researchReports = MapperKt.parallelMap(
                researchTopics.getTopics(),
                context,
                10, // concurrencyLevel
                kotlinx.coroutines.Dispatchers.getIO(), // dispatcher
                (it, continuation) -> context.promptRunner(
                                LlmOptions.fromModel(properties.getResearchLlm()),
                                emptySet(), // 使用Kotlin的emptySet
                                emptyList(), // 使用Kotlin的emptyList
                                emptyList(),
                                emptyList(),
                                false)
                        .withToolGroup(CoreToolGroups.WEB)
                        .withToolObject(presentationRequest.getProject())
                        .withPromptContributor(presentationRequest)
                        .createObject(
                                "Given the following topic and the goal to create a presentation\n" +
                                        "for this audience, create a research report.\n" +
                                        "Use web tools to research and the findPatternInProject tool to look\n" +
                                        "within the given software project.\n" +
                                        "Always look for code examples in the project before using the web.\n" +
                                        "Topic: " + it.getTopic() + "\n" +
                                        "Questions:\n" +
                                        String.join("\n", it.getQuestions()),
                                ResearchReport.class
                        )
        );

        return new ResearchResult(
                researchTopics.getTopics().stream().map(topic ->
                        new CompletedResearch(
                                topic,
                                researchReports.get(researchTopics.getTopics().indexOf(topic))
                        )
                ).collect(Collectors.toList())
        );
    }

    @Action
    public SlideDeck createDeck(PresentationRequest presentationRequest, ResearchResult researchComplete, OperationContext context) {
        List<ResearchReport> reports = researchComplete.getTopicResearches().stream().map(CompletedResearch::getResearchReport).toList();
        SlideDeck slideDeck = context.promptRunner(
                        LlmOptions.fromCriteria(ModelSelectionCriteria.byName(properties.getCreationLlm())),
                        emptySet(), // 使用Kotlin的emptySet
                        emptyList(), // 使用Kotlin的emptyList
                        emptyList(),
                        emptyList(),
                        false)
                .withPromptContributor(presentationRequest)
                .withToolGroup(CoreToolGroups.WEB)
                .withToolObject(presentationRequest.getProject())
                .createObject(
                        "Create content for an impactful slide deck based on the given research.\n" +
                                "Use the following input to guide the presentation:\n\n" +
                                "# About the presenter\n" +
                                presentationRequest.getPresenterBio() + "\n\n" +
                                "# Presentation narrative\n" +
                                presentationRequest.getBrief() + "\n\n" +
                                "Support your points using the following research:\n" +
                                reports + "\n\n" +
                                "The presentation should be " + presentationRequest.getSlideCount() + " slides long.\n" +
                                "It should have a compelling narrative and call to action.\n" +
                                "It should end with a list of reference links.\n" +
                                "Use the findPatternInProject tool and other file tools to find relevant content within the given software project\n" +
                                "if required and format code on slides.\n\n" +
                                "Use Marp format, creating Markdown that can be rendered as slides.\n" +
                                "If you need to look it up, see https://github.com/marp-team/marp/blob/main/website/docs/guide/directives.md\n\n" +
                                "If you include GraphViz dot diagrams, do NOT enclose them in ```\n" +
                                "DO start with dot e.g. \"dot digraph...\"\n\n" +
                                "Use the following images as suggested:\n" +
                                presentationRequest.getImages().entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue().url() + " - use when: " + entry.getValue().useWhen()).collect(Collectors.joining("\n")) + "\n\n" +
                                "Use the following header elements to start the deck.\n" +
                                "Add further header elements if you wish.\n\n" +
                                "```\n" +
                                presentationRequest.getHeader() + "\n" +
                                "```",
                        SlideDeck.class
                );
        filePersister.saveFile(
                presentationRequest.getOutputDirectory(),
                presentationRequest.rawOutputFile(),
                slideDeck.getDeck()
        );
        return slideDeck;
    }

    @Action(outputBinding = "withDiagrams", cost = 1.0)
    public SlideDeck expandDigraphs(SlideDeck slideDeck, PresentationRequest presentationRequest) {
        DigraphExpander diagramExpander = new DotCliDigraphExpander(presentationRequest.getOutputDirectory());
        SlideDeck withDigraphs = slideDeck.expandDigraphs(diagramExpander);
        filePersister.saveFile(
                presentationRequest.getOutputDirectory(),
                presentationRequest.withDiagramsOutputFile(),
                withDigraphs.getDeck()
        );
        return slideDeck;
    }

    @Action(outputBinding = "withDiagrams")
    public SlideDeck loadWithDigraphs(PresentationRequest presentationRequest) {
        String content = filePersister.loadFile(
                presentationRequest.getOutputDirectory(),
                presentationRequest.withDiagramsOutputFile()
        );
        return content != null ? new SlideDeck(content) : null;
    }

    @Action(outputBinding = "withIllustrations")
    public SlideDeck addIllustrations(@RequireNameMatch SlideDeck withDiagrams, PresentationRequest presentationRequest, OperationContext context) {
        SlideDeck deckWithIllustrations;
        if (!presentationRequest.isAutoIllustrate()) {
            logger.info("Not auto illustrating");
            deckWithIllustrations = withDiagrams;
        } else {
            logger.info("Asking LLM to add illustrations to this resource");

            var illustrator = context.promptRunner(
                    LlmOptions.fromCriteria(ModelSelectionCriteria.byName(properties.getResearchLlm())).withTemperature(.3),
                    emptySet(), // 使用Kotlin的emptySet
                    emptyList(), // 使用Kotlin的emptyList
                    emptyList(),
                    emptyList(),
                    false).withToolGroup(CoreToolGroups.WEB);
            List<Slide> newSlides = withDiagrams.slides().stream().map(slide -> {
                String newContent = illustrator.generateText(
                        "Take the following slide in MARP format.\n" +
                                "Overall objective: " + presentationRequest.getBrief() + "\n\n" +
                                "If the slide contains an important point, try to add an image to it\n" +
                                "Check that the image is available.\n" +
                                "Don't make the image too big.\n" +
                                "Put the image on the right.\n" +
                                "Make no other changes.\n" +
                                "Do not perform any web research besides seeking images.\n" +
                                "Return nothing but the amended slide content (the content between <slide></slide>).\n" +
                                "Do not ask any questions.\n" +
                                "If you don't think an image is needed, return the slide unchanged.\n\n" +
                                "<slide>\n" +
                                slide.getContent() + "\n" +
                                "</slide>"
                );
                return new Slide(slide.getNumber(), newContent);
            }).collect(Collectors.toList());

            SlideDeck dwi = withDiagrams;
            for (Slide slide : newSlides) {
                dwi = dwi.replaceSlide(slide, slide.getContent());
            }
            deckWithIllustrations = dwi;
        }

        logger.info(
                "Saving final MARP markdown to {}/{}",
                presentationRequest.getOutputDirectory(),
                presentationRequest.getOutputFile()
        );
        filePersister.saveFile(
                presentationRequest.getOutputDirectory(),
                presentationRequest.getOutputFile(),
                deckWithIllustrations.getDeck()
        );
        return withDiagrams;
    }

    @Action
    public FileArtifact convertToSlides(PresentationRequest presentationRequest, @RequireNameMatch SlideDeck withIllustrations) {
        String htmlFile = slideFormatter.createHtmlSlides(
                presentationRequest.getOutputDirectory(),
                presentationRequest.getOutputFile()
        );
        return new FileArtifact(
                presentationRequest.getOutputDirectory(),
                htmlFile
        );
    }
}
