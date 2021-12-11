package org.jnode.mavenizer;

import static java.nio.file.Paths.get;
import static org.apache.tools.ant.ProjectHelper.configureProject;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;

public class Constants {
    static final IAntProject ANT_PROJECT;
    static final String JNODE_VERSION;

    static {
        ANT_PROJECT = new JNodeAntProject();
        JNODE_VERSION = ANT_PROJECT.getProperty("jnode-ver");
    }

    private Constants() {
        // utility class
    }

    private static class JNodeAntProject extends AbstractAntProject {
        private JNodeAntProject() {
            project.init();
            configureProject(project,
                SRC_ROOT.getDirectory().toAbsolutePath().resolve(get("all", "build.xml")).toFile());
        }
    }
}
