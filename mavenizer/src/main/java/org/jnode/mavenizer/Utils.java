package org.jnode.mavenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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
import static org.apache.tools.ant.util.FileUtils.safeReadFully;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.plugin.model.Factory.parseDescriptor;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Utils {
    public static final String PROP_JNODE_VERSION = "jnode.version";
    
    public static final String ALIAS_JNODE_VERSION = "${" + PROP_JNODE_VERSION + "}";

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

    public static boolean isEmpty(Object[] array) {
        return (array == null) || (array.length == 0);
    }
    
    public static boolean isBlank(String value) {
        return (value == null) || value.trim().isEmpty();
    }
    
    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }
    
    public static String processVersion(String version) {
        version = defaultIfBlank(version, ALIAS_JNODE_VERSION);
        if ("@VERSION@".equals(version)) {
            version = ALIAS_JNODE_VERSION;
        }
        
        return version;
    }
    
    public static String[] getRelativePath(File root, File f) {
        String rootPath = root.getAbsolutePath();
        String subDirPath = f.getAbsolutePath();
        String relativePath = subDirPath.substring(rootPath.length());
        if (relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(File.separator.length());
        }
        return relativePath.split(File.separator);
    }
    
    public static String getRelativePath(File root, File f, String prefix) {
        String s = f.getAbsolutePath();
        if (s.startsWith(root.getAbsolutePath())) {
            s = ((prefix != null) ? prefix : "") + s.substring(root.getAbsolutePath().length());
        }
        return s;
    }

    public static PluginDescriptor readDescriptor(File descriptor) {
        try {
            final XMLElement root = new XMLElement(new Hashtable(), true, false);
            String content = readFully(descriptor);
            content = content.replace("@VERSION@", JNODE_VERSION);
            root.parseFromReader(new StringReader(content));
            return parseDescriptor(root);
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        } catch (PluginException e) {
            throw new BuildException(e);
        }
    }

    static String readFully(File file) throws IOException {
        FileReader reader = new FileReader(file);
        String content;
        try {
            content = safeReadFully(reader);
        } finally {
            reader.close();
        }
        return content;
    }

    static File getPluginHome(DestinationRoot destinationRoot, PluginInfo pluginInfo) {
        File projectRoot = new File(destinationRoot.getDirectory(), pluginInfo.getProject().getDirectory());
        return new File(projectRoot, pluginInfo.getId());
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
