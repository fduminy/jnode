package org.jnode.mavenizer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

import static java.lang.String.valueOf;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Paths.get;
import static org.apache.tools.ant.util.FileUtils.safeReadFully;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.plugin.model.Factory.parseDescriptor;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Utils {

    private static final BuildListener LISTENER = new BuildListener() {

        @Override
        public void buildFinished(BuildEvent arg0) {
            Log.trace("" + arg0);
        }

        @Override
        public void buildStarted(BuildEvent arg0) {
            Log.trace("" + arg0);
        }

        @Override
        public void messageLogged(BuildEvent arg0) {
            Log.trace("" + arg0);
        }

        @Override
        public void targetFinished(BuildEvent arg0) {
            Log.trace("" + arg0);
        }

        @Override
        public void targetStarted(BuildEvent arg0) {
            Log.trace("" + arg0);
        }

        @Override
        public void taskFinished(BuildEvent arg0) {
            Log.trace("" + arg0);
        }

        @Override
        public void taskStarted(BuildEvent arg0) {
            Log.trace("" + arg0);
        }
    };

    public static boolean isBlank(String value) {
        return (value == null) || value.trim().isEmpty();
    }

    public static PluginDescriptor readDescriptor(Path descriptor) {
        try {
            final XMLElement root = new XMLElement(new Hashtable<>(), true, false);
            String content = readFully(descriptor);
            content = content.replace("@VERSION@", JNODE_VERSION);
            root.parseFromReader(new StringReader(content));
            return parseDescriptor(root);
        } catch (IOException | PluginException e) {
            throw new BuildException(e);
        }
    }

    static String readFully(Path file) throws IOException {
        try (Reader reader = newBufferedReader(file)) {
            return safeReadFully(reader);
        }
    }

    static Path getPluginHome(DestinationRoot destinationRoot, PluginInfo pluginInfo) {
        Path projectRoot = destinationRoot.getDirectory().resolve(pluginInfo.getProject().getDirectory());
        return projectRoot.resolve(pluginInfo.getId());
    }

    static PluginInfo findPlugin(Iterable<PluginInfo> infos, String id) {
        PluginInfo result = null;
        for (PluginInfo pluginInfo : infos) {
            if (pluginInfo.getId().equals(id)) {
                result = pluginInfo;
                break;
            }
        }
        return result;
    }

    static Project createAntProject() {
        return createAntProject(false);
    }

    static Project createAntProject(boolean fromJNode) {
        Project project = new Project();
        project.addBuildListener(LISTENER);
        if (!fromJNode) {
            addJNodeProperties(project);
        }
        return project;
    }

    static Path getLibrary(String libraryName) {
        Path library = null;
        String libPath = ANT_PROJECT.getProperty(libraryName);
        if (!isBlank(libPath)) {
            library = get(libPath);
        }
        return library;
    }

    private static void addJNodeProperties(Project antProject) {
        // copy properties from jnode's ant project
        Map<?,?> properties = ANT_PROJECT.getProperties();
        for (Object key : properties.keySet()) {
            String name = valueOf(key);
            String value = antProject.getProperty(name);
            if (value == null) {
                // define property only if not already defined
                // (avoid overwriting internal default ant properties)
                antProject.setProperty(name, valueOf(properties.get(name)));
            }
        }
    }
}
