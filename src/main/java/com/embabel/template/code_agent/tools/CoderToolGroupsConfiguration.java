package com.embabel.template.code_agent.tools;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Configuration
public class CoderToolGroupsConfiguration {

    private final List<McpSyncClient> mcpSyncClients;

    public CoderToolGroupsConfiguration(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
    }

    @Bean
    public ToolGroup gitToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.create("git tools", "git"),
                "docker-git",
                "Docker",
                new HashSet<>(Arrays.asList(
                        ToolGroupPermission.INTERNET_ACCESS,
                        ToolGroupPermission.HOST_ACCESS
                )),
                mcpSyncClients,
                toolCallback -> toolCallback.getToolDefinition().name().contains("git_")
        );
    }
}
