package org.jnode.mavenizer;

import java.util.Map;
import org.apache.tools.ant.ProjectComponent;

public interface IAntProject {
    String getProperty(String propertyName);

    void setProjectFor(ProjectComponent antComponent);

    Map<?, ?> getProperties();
}
