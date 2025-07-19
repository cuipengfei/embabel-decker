package com.embabel.template.code_agent.domain;

import com.embabel.template.code_agent.agent.CoderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.stream.StreamSupport.stream;

/**
 * Stateful component that holds the focus of the agent.
 */
@Component
public class TaskFocus {

    private final Logger logger = LoggerFactory.getLogger(TaskFocus.class);
    private final SoftwareProjectRepository softwareProjectRepository;
    private final CoderProperties coderProperties;

    private SoftwareProject softwareProject;

    public TaskFocus(SoftwareProjectRepository softwareProjectRepository, CoderProperties coderProperties) {
        this.softwareProjectRepository = softwareProjectRepository;
        this.coderProperties = coderProperties;

        if (coderProperties.getDefaultProject() != null) {
            setFocus(coderProperties.getDefaultProject());
        }
    }

    public SoftwareProject getSoftwareProject() {
        return softwareProject;
    }

    public SoftwareProject setFocus(String name) {
        Optional<SoftwareProject> newFocus = stream(softwareProjectRepository.findAll().spliterator(), false)
                .filter(project -> project.getRoot().contains(name))
                .findFirst();

        if (newFocus.isPresent()) {
            logger.info("Set new focus: {}", name);
            softwareProject = newFocus.get();
            return softwareProject;
        } else {
            logger.warn("Cannot set focus: No project found with name: {}", name);
            return null;
        }
    }

    public void saveAndSwitch(SoftwareProject newAgentProject) {
        logger.info("Switching focus to new project: {}", newAgentProject.getRoot());
        softwareProjectRepository.save(newAgentProject);
        softwareProject = newAgentProject;
    }
}
