package org.jnode.mavenizer;

import static java.nio.file.Paths.get;
import static org.apache.tools.ant.ProjectHelper.configureProject;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.Utils.createAntProject;

public class Constants {
    static final org.apache.tools.ant.Project ANT_PROJECT;
    static final String JNODE_VERSION;

    static {
        ANT_PROJECT = createAntProject(true);
        ANT_PROJECT.init();
        configureProject(ANT_PROJECT, SRC_ROOT.getDirectory().toAbsolutePath().resolve(get("all", "build.xml")).toFile());

        JNODE_VERSION = ANT_PROJECT.getProperty("jnode-ver");
    }
}
