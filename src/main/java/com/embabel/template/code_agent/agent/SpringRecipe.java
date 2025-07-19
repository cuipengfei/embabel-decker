package com.embabel.template.code_agent.agent;

public final class SpringRecipe {

    private final String projectName;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String bootVersion;
    private final String language;
    private final String packaging;
    private final String javaVersion;
    private final String dependencies;

    public SpringRecipe() {
        this("demo", "com.example", "demo", "0.0.1-SNAPSHOT",
                "3.2.0", "kotlin", "jar", "17", "web,actuator,devtools");
    }

    public SpringRecipe(String projectName, String groupId, String artifactId, String version,
                        String bootVersion, String language, String packaging, String javaVersion,
                        String dependencies) {
        this.projectName = projectName;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.bootVersion = bootVersion;
        this.language = language;
        this.packaging = packaging;
        this.javaVersion = javaVersion;
        this.dependencies = dependencies;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getBootVersion() {
        return bootVersion;
    }

    public String getLanguage() {
        return language;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getDependencies() {
        return dependencies;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringRecipe that = (SpringRecipe) obj;
        return projectName.equals(that.projectName) &&
                groupId.equals(that.groupId) &&
                artifactId.equals(that.artifactId) &&
                version.equals(that.version) &&
                bootVersion.equals(that.bootVersion) &&
                language.equals(that.language) &&
                packaging.equals(that.packaging) &&
                javaVersion.equals(that.javaVersion) &&
                dependencies.equals(that.dependencies);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(projectName, groupId, artifactId, version, bootVersion,
                language, packaging, javaVersion, dependencies);
    }

    @Override
    public String toString() {
        return "SpringRecipe{" +
                "projectName='" + projectName + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", bootVersion='" + bootVersion + '\'' +
                ", language='" + language + '\'' +
                ", packaging='" + packaging + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", dependencies='" + dependencies + '\'' +
                '}';
    }
}
