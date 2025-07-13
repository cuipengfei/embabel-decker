package com.embabel.template.decker_agent;

import com.embabel.agent.config.models.OpenAiModels;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "embabel.presentation-maker")
class PresentationMakerProperties {
    private String researchLlm = OpenAiModels.GPT_41;
    private String creationLlm = OpenAiModels.GPT_41;

    public String getResearchLlm() {
        return researchLlm;
    }

    public String getCreationLlm() {
        return creationLlm;
    }

}
