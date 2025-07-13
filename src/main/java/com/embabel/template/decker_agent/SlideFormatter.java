package com.embabel.template.decker_agent;

@FunctionalInterface
public interface SlideFormatter {

    String createHtmlSlides(String directory, String markdownFilename);
}

