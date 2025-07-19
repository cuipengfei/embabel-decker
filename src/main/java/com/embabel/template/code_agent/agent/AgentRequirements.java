package com.embabel.template.code_agent.agent;

public final class AgentRequirements {

    private final String projectName;
    private final String groupId;
    private final String packageName;
    private final String language;
    private final String requirements;

    public AgentRequirements() {
        this("demo", "com.example", "", "kotlin", "");
    }

    public AgentRequirements(String projectName, String groupId, String packageName, String language, String requirements) {
        this.projectName = projectName;
        this.groupId = groupId;
        this.packageName = packageName;
        this.language = language;
        this.requirements = requirements;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLanguage() {
        return language;
    }

    public String getRequirements() {
        return requirements;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AgentRequirements that = (AgentRequirements) obj;
        return projectName.equals(that.projectName) &&
                groupId.equals(that.groupId) &&
                packageName.equals(that.packageName) &&
                language.equals(that.language) &&
                requirements.equals(that.requirements);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(projectName, groupId, packageName, language, requirements);
    }

    @Override
    public String toString() {
        return "AgentRequirements{" +
                "projectName='" + projectName + '\'' +
                ", groupId='" + groupId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", language='" + language + '\'' +
                ", requirements='" + requirements + '\'' +
                '}';
    }
}
