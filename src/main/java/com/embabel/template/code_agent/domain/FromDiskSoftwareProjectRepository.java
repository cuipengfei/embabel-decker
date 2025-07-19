/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.template.code_agent.domain;

import com.embabel.agent.tools.file.FileReadTools;
import com.embabel.agent.tools.file.FileTools;
import com.embabel.template.code_agent.agent.CoderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Look for parallel directories
 */
@Service
public class FromDiskSoftwareProjectRepository extends InMemoryCrudRepository<SoftwareProject> implements SoftwareProjectRepository {

    private final Logger logger = LoggerFactory.getLogger(FromDiskSoftwareProjectRepository.class);
    private final CoderProperties properties;

    public FromDiskSoftwareProjectRepository(CoderProperties properties) {
        super(
                SoftwareProject::getRoot,  // idGetter
                (entity, id) -> {          // idSetter - shouldn't be called
                    throw new UnsupportedOperationException("shouldn't be called");
                }
        );
        this.properties = properties;

        this.saveAll(findProjectsUnderRoot());
        logger.info("Loaded {} projects from disk", this.count());

        String projectsList = StreamSupport.stream(this.findAll().spliterator(), false)
                .sorted(Comparator.comparing(SoftwareProject::getRoot))
                .map(SoftwareProject::getRoot)
                .collect(Collectors.joining("\n\t"));
        logger.info("Projects:\n\t{}", projectsList);
    }

    private List<SoftwareProject> findProjectsUnderRoot() {
        FileReadTools rootFileTools = FileTools.Companion.readOnly(properties.getRoot().getAbsolutePath(), Collections.emptyList());
        logger.info("Looking under {} for projects", rootFileTools.getRoot());

        List<String> pomFiles = rootFileTools.findFiles(
                "**/pom.xml",
                !properties.getFindNestedProjects()
        );

        logger.info("Found {} Maven projects", pomFiles.size());

        return pomFiles.stream()
                .map(pomFile -> pomFile.replace("pom.xml", ""))
                .map(root -> new SoftwareProject(
                        root,
                        "TODO: call git", // url
                        "Maven, Java, Spring Boot\nJUnit 5, Mockito", // tech
                        "mvn test" // buildCommand
                ))
                .collect(Collectors.toList());
    }
}
