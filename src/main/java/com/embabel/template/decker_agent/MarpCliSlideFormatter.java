package com.embabel.template.decker_agent;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
class MarpCliSlideFormatter implements SlideFormatter {

    @Override
    public String createHtmlSlides(String directory, String markdownFileName) {
        runMarpCli(directory, markdownFileName);
        return markdownFileName.replace(".md", ".html");
    }

    public void runMarpCli(String directory, String markdownFileName) {
        String command = "npx @marp-team/marp-cli@latest " + markdownFileName + " --no-stdin";
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.directory(new File(directory));

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

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
                System.out.println("Marp CLI completed with exit code: " + process.exitValue());
            } else {
                System.out.println("Marp CLI process timed out");
                process.destroyForcibly();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing Marp CLI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
