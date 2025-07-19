package com.embabel.template.code_agent.agent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.template.code_agent.domain.SoftwareProject;
import com.embabel.template.code_agent.domain.SoftwareProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

//@Agent(
//    description = "Explain code or perform changes to a software project or directory structure",
//)
@Profile("!test")
public class SoftwareProjectLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SoftwareProjectRepository softwareProjectRepository;
    private final CoderProperties coderProperties;

    public SoftwareProjectLoader(
            SoftwareProjectRepository softwareProjectRepository,
            CoderProperties coderProperties) {
        this.softwareProjectRepository = softwareProjectRepository;
        this.coderProperties = coderProperties;
    }

    @Action
    public SoftwareProject loadExistingProject() {
//        Optional<SoftwareProject> found = softwareProjectRepository.findById(coderProperties.getDefaultLocation());
//        if (found.isPresent()) {
//            logger.info("Found existing project at {}", coderProperties.getDefaultLocation());
//        }
//        return found.orElse(null);
        throw new UnsupportedOperationException("TODO: Implementation needed");
    }

    /**
     * Use an LLM to analyze the project.
     * This is expensive so we set cost high
     */
    @Action(cost = 10000.0)
    public SoftwareProject analyzeProject() {
        // Note: This uses Kotlin interop for the `using` function and `create` extension
        // The equivalent Java code would use the Spring AI framework directly
        throw new UnsupportedOperationException("TODO: Implementation needed - requires file tools integration");
        
        /*
        SoftwareProject project = com.embabel.agent.api.annotation.AnnotationUtilsKt.using(
            coderProperties.getPrimaryCodingLlm()
        ).create(
            "Analyze the project at " + "TODO()" + "\n" +
            "Use the file tools to read code and directories before analyzing it",
            SoftwareProject.class
        );
        
        // So we don't need to do this again
        softwareProjectRepository.save(project);
        return project;
        */
    }
}
