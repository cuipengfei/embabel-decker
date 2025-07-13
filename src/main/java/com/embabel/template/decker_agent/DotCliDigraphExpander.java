package com.embabel.template.decker_agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

class DotCliDigraphExpander implements DigraphExpander {

    private final String directory;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DotCliDigraphExpander(String directory) {
        this.directory = directory;
    }

    @Override
    public String expandDiagram(String fileBase, String dot) {
        String outputFile = fileBase + ".svg";
        logger.info("Expanding diagram to {}:\n{}", outputFile, dot);

        String command = "echo '" + dot + "' | dot -Tsvg -o " + outputFile;
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.directory(new File(directory));

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

        logger.info("Running command {}", processBuilder.command());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            boolean exitCode = process.waitFor(60, TimeUnit.SECONDS);

            if (exitCode) {
                System.out.println("dot CLI completed with exit code: " + process.exitValue());
            } else {
                System.out.println("dot CLI process timed out");
                process.destroyForcibly();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return fileBase + ".svg";
    }
}
