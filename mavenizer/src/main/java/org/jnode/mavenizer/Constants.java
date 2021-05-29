package org.jnode.mavenizer;

import java.io.File;

import static org.apache.tools.ant.ProjectHelper.configureProject;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.Utils.createAntProject;

public class Constants {
    static final org.apache.tools.ant.Project ANT_PROJECT;
    static final String JNODE_VERSION;

    static {
        ANT_PROJECT = createAntProject(true);
        ANT_PROJECT.init();
        configureProject(ANT_PROJECT, new File(SRC_ROOT.getDirectory().getAbsolutePath() + "/all/build.xml"));
        Log.debug("keyset:" + ANT_PROJECT.getProperties().keySet());

        JNODE_VERSION = ANT_PROJECT.getProperty("jnode-ver");
    }
}
