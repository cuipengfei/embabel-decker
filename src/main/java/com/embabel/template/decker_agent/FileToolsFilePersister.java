package com.embabel.template.decker_agent;

import com.embabel.agent.tools.file.FileTools;
import org.springframework.stereotype.Service;

import static kotlin.collections.CollectionsKt.emptyList;

@Service
class FileToolsFilePersister implements FilePersister {

    @Override
    public void saveFile(String directory, String fileName, String content) {
        FileTools.Companion.readWrite(directory, emptyList()).createFile(fileName, content, true);
    }

    @Override
    public String loadFile(String directory, String fileName) {
        try {
            return FileTools.Companion.readOnly(directory, emptyList()).readFile(fileName);
        } catch (IllegalArgumentException e) {
            // File does not exist
            return null;
        }
    }
}
