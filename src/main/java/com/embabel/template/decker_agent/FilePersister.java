package com.embabel.template.decker_agent;

public interface FilePersister {

    void saveFile(String directory, String fileName, String content);

    /**
     * Return file content
     */
    String loadFile(String directory, String fileName);
}

