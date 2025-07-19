package com.embabel.template.code_agent.domain;

import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.core.types.Described;
import com.embabel.common.core.types.Named;

/**
 * Reference
 * Must be added as tools and prompt contributor
 */
public interface Reference extends PromptContributor, Named, Described {
}

