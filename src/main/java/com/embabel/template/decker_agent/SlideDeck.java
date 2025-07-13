package com.embabel.template.decker_agent;

import com.embabel.agent.domain.library.ContentAsset;
import kotlin.collections.CollectionsKt;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SlideDeck implements ContentAsset {

    private static final Logger logger = LoggerFactory.getLogger(SlideDeck.class);

    private final String deck;
    private final Instant timestamp = Instant.now();

    public SlideDeck(String deck) {
        this.deck = deck;
    }

    public String getDeck() {
        return deck;
    }

    @NotNull
    @Override
    public String getContent() {
        return deck;
    }

    @NotNull
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public int slideCount() {
        return slides().size();
    }

    public List<Slide> slides() {
        if (deck == null || deck.isBlank()) {
            return List.of();
        }
        String[] parts = deck.split("(?m)^\\s*---+\\s*$");
        List<String> trimmedParts = Arrays.stream(parts)
                // 使用Kotlin的精确trim方法
                .map(p -> StringsKt.trim(p, new char[]{'\r', '\n', ' ', '\t'}))
                .filter(p -> !p.isBlank())
                .toList();

        if (trimmedParts.isEmpty()) {
            return List.of();
        }
        if (trimmedParts.size() == 1 && deck.trim().startsWith("---")) {
            return List.of();
        }

        // 使用Kotlin标准库的drop和mapIndexed方法
        List<String> slideContents = CollectionsKt.drop(trimmedParts, 1);
        return CollectionsKt.mapIndexed(slideContents, (index, content) -> new Slide(index + 1, content));
    }

    public String header() {
        if (deck == null || deck.isBlank()) {
            return "";
        }
        String[] parts = deck.split("(?m)^\\s*---+\\s*$");
        List<String> trimmedParts = Arrays.stream(parts)
                .map(p -> StringsKt.trim(p, new char[]{'\r', '\n', ' ', '\t'}))
                .filter(p -> !p.isBlank())
                .collect(Collectors.toList());

        if (!trimmedParts.isEmpty() && deck.trim().startsWith("---")) {
            return trimmedParts.get(0);
        }
        return "";
    }

    public SlideDeck withHeader(String header) {
        String trimmedHeader = header.trim();
        List<Slide> slides = slides();

        if (slides.isEmpty()) {
            return new SlideDeck("---\n" + trimmedHeader + "\n");
        } else {
            String slideContents = slides.stream().map(Slide::content).collect(Collectors.joining("\n---\n"));
            return new SlideDeck("---\n" + trimmedHeader + "\n---\n" + slideContents + "\n");
        }
    }

    public SlideDeck replaceSlide(Slide slide, String newContent) {
        List<Slide> slides = slides();
        if (slides.isEmpty() || slide.number() < 1 || slide.number() > slides.size()) {
            return this;
        }

        List<Slide> updatedSlides = slides.stream().map(s -> {
            if (s.number() == slide.number()) {
                return new Slide(s.number(), newContent);
            } else {
                return s;
            }
        }).collect(Collectors.toList());

        String currentHeader = header();
        String slideContents = updatedSlides.stream().map(Slide::content).collect(Collectors.joining("\n---\n"));

        return new SlideDeck("---\n" + currentHeader + "\n---\n" + slideContents + "\n");
    }

    public SlideDeck expandDigraphs(DigraphExpander digraphExpander) {
        String result = getContent();
        Pattern dotBlockRegex = Pattern.compile("(```)?dot\\s*digraph\\s+(\\w+)\\s+(\\{[\\s\\S;]*?\\})\\s*(```)?", Pattern.DOTALL);
        Matcher matches = dotBlockRegex.matcher(getContent());

        logger.info("Found {} regex matches",
                dotBlockRegex.matcher(getContent()).results().count());

        int replacedDiagrams = 0;
        while (matches.find()) {
            String dotString = matches.group(3);
            String diagramName = matches.group(2);

            // Fixed: Correct parameter order to match Kotlin version
            String diagramFile = digraphExpander.expandDiagram(diagramName, "digraph " + dotString);
            String imageReference = "\n![Diagram](./" + diagramFile + ")\n";
            result = result.replace(matches.group(0), imageReference);
            replacedDiagrams++;
            logger.info("Replaced dot diagram {} with\n{}", diagramFile, matches.group(0));
        }
        logger.info("Replaced {} dot diagrams", replacedDiagrams);
        return new SlideDeck(result);
    }
}
