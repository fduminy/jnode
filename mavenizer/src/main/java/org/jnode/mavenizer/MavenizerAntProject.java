package org.jnode.mavenizer;

import java.util.Map;

import static java.lang.String.valueOf;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;

public class MavenizerAntProject extends AbstractAntProject {
    public MavenizerAntProject() {
        // copy properties from jnode's ant project
        Map<?, ?> properties = ANT_PROJECT.getProperties();
        for (Object key : properties.keySet()) {
            String name = valueOf(key);
            String value = project.getProperty(name);
            if (value == null) {
                // define property only if not already defined
                // (avoid overwriting internal default ant properties)
                project.setProperty(name, valueOf(properties.get(name)));
            }
        }
    }
}
