package com.embabel.template.decker_agent;

import com.embabel.agent.api.common.autonomy.AgentProcessExecution;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.event.logging.personality.severance.LumonColorPalette;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.ExtensionsKt;
import kotlin.collections.CollectionsKt;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.embabel.agent.shell.FormatProcessOutputKt.formatProcessOutput;

@ShellComponent("Presentation maker commands")
public class PresentationMakerShell {

    private final AgentPlatform agentPlatform;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public PresentationMakerShell(AgentPlatform agentPlatform, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.agentPlatform = agentPlatform;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @ShellMethod
    public String makePresentation(
            @ShellOption(defaultValue = "file:/Users/rjohnson/dev/embabel.com/embabel-agent/embabel-agent-api/src/main/kotlin/com/embabel/examples/dogfood/presentation/kotlinconf_presentation.yml")
            String file
    ) throws IOException {
        ObjectMapper yamlReader = ExtensionsKt.registerKotlinModule(new ObjectMapper(new YAMLFactory()));

        PresentationRequest presentationRequest = yamlReader.readValue(
                resourceLoader.getResource(file).getContentAsString(Charset.defaultCharset()),
                PresentationRequest.class
        );

        var targetAgent = CollectionsKt.single(
                agentPlatform.agents(),
                agent -> agent.getName().equals("PresentationMaker")
        );

        var agentProcess = agentPlatform.runAgentWithInput(
                targetAgent,
                ProcessOptions.Companion.getDEFAULT(),
                presentationRequest
        );

        return formatProcessOutput(
                AgentProcessExecution.Companion.fromProcessStatus(
                        presentationRequest,
                        agentProcess
                ),
                LumonColorPalette.INSTANCE,
                objectMapper,
                140
        ) + "\ndeck is at " + presentationRequest.getOutputDirectory() + "/" + presentationRequest.getOutputFile();
    }
}
