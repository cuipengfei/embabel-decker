package com.embabel.template.decker_agent;

import com.embabel.agent.domain.library.ContentAsset;
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

    @Override
    public String getContent() {
        return deck;
    }

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
                .map(p -> p.trim())
                .filter(p -> !p.isBlank())
                .collect(Collectors.toList());

        if (trimmedParts.isEmpty()) {
            return List.of();
        }
        if (trimmedParts.size() == 1 && deck.trim().startsWith("---")) {
            return List.of();
        }

        return trimmedParts.stream().skip(1).map(content -> new Slide(trimmedParts.indexOf(content), content)).collect(Collectors.toList());
    }

    public String header() {
        if (deck == null || deck.isBlank()) {
            return "";
        }
        String[] parts = deck.split("(?m)^\\s*---+\\s*$");
        List<String> trimmedParts = Arrays.stream(parts)
                .map(p -> p.trim())
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
            String slideContents = slides.stream().map(Slide::getContent).collect(Collectors.joining("\n---\n"));
            return new SlideDeck("---\n" + trimmedHeader + "\n---\n" + slideContents + "\n");
        }
    }

    public SlideDeck replaceSlide(Slide slide, String newContent) {
        List<Slide> slides = slides();
        if (slides.isEmpty() || slide.getNumber() < 1 || slide.getNumber() > slides.size()) {
            return this;
        }

        List<Slide> updatedSlides = slides.stream().map(s -> {
            if (s.getNumber() == slide.getNumber()) {
                return new Slide(s.getNumber(), newContent);
            } else {
                return s;
            }
        }).collect(Collectors.toList());

        String currentHeader = header();
        String slideContents = updatedSlides.stream().map(Slide::getContent).collect(Collectors.joining("\n---\n"));

        return new SlideDeck("---\n" + currentHeader + "\n---\n" + slideContents + "\n");
    }

    public SlideDeck expandDigraphs(DigraphExpander digraphExpander) {
        String result = getContent();
        Pattern dotBlockRegex = Pattern.compile("(```)?dot\\s*digraph\\s+(\\w+)\\s+(\\{[\\s\\S;]*?\\})\\s*(```)?", Pattern.DOTALL);
        Matcher matches = dotBlockRegex.matcher(getContent());

        int replacedDiagrams = 0;
        while (matches.find()) {
            String dotString = matches.group(3);
            String diagramName = matches.group(2);

            String diagramFile = digraphExpander.expandDiagram("digraph " + dotString, diagramName);
            String imageReference = "\n![Diagram](./" + diagramFile + ")\n";
            result = result.replace(matches.group(0), imageReference);
            replacedDiagrams++;
            logger.info("Replaced dot diagram {} with\n{}", diagramFile, matches.group(0));
        }
        logger.info("Replaced {} dot diagrams", replacedDiagrams);
        return new SlideDeck(result);
    }
}

