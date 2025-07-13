package com.embabel.template.decker_agent;

public interface DigraphExpander {

    /**
     * Expands a DOT diagram into a more detailed representation.
     *
     * @param fileBase The base name of the file to save the expanded diagram.
     * @param dot      The DOT diagram as a string.
     * @return name of the file, without a path
     */
    String expandDiagram(String fileBase, String dot);
}

