package org.jnode.mavenizer;

import java.util.Map;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

import static org.jnode.mavenizer.Utils.LISTENER;

abstract class AbstractAntProject implements IAntProject {
    protected final Project project;

    AbstractAntProject() {
        project = new Project();
        project.addBuildListener(LISTENER);
    }

    @Override
    public final String getProperty(String propertyName) {
        return project.getProperty(propertyName);
    }

    @Override
    public final Map<?, ?> getProperties() {
        return project.getProperties();
    }

    @Override
    public final void setProjectFor(ProjectComponent antComponent) {
        antComponent.setProject(project);
    }
}
