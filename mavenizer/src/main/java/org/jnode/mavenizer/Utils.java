package org.jnode.mavenizer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.Runtime;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static org.apache.tools.ant.util.FileUtils.safeReadFully;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.plugin.model.Factory.parseDescriptor;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Utils {

    static final BuildListener LISTENER = new BuildListener() {

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

    static Path getLibrary(IAntProject project, String libraryName) {
        Path library = null;
        String libPath = project.getProperty(libraryName);
        if (!isBlank(libPath)) {
            library = get(libPath);
        }
        return library;
    }

    static List<Export> getExports(IAntProject project, Path projectSourceDirectory, PluginInfo pluginInfo) {
        List<Export> exports = new ArrayList<>();
        Runtime runtime = pluginInfo.getPluginDescriptor().getRuntime();
        if (exists(projectSourceDirectory) && (runtime != null) && (runtime.getLibraries() != null)) {
            for (Library library : runtime.getLibraries()) {
                Path libFile = getLibrary(project, library.getName());
                if ((libFile != null) && isDirectory(libFile)) {
                    exports.add(new Export(projectSourceDirectory, asList(library.getExports())));
                }
            }
        }
        return exports;
    }

    /**
     * Removes ".*" at the end of the string.
     */
    static String removeDotStarFilterAtTheEndOf(String filter) {
        return filter.substring(0, filter.length() - 2);
    }
}
