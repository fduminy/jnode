package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Parallel;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;
import static org.jnode.mavenizer.Directory.SourceRoot.sourceRoot;
import static org.jnode.mavenizer.FileFinder.delete;
import static org.jnode.mavenizer.Project.*;
import static org.jnode.mavenizer.ProjectPrinter.print;
import static org.jnode.mavenizer.ProjectTreeBuilder.buildProjectTree;
import static org.jnode.mavenizer.Utils.createAntProject;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Mavenizer {
    public static final String JNODE_HOME = "/home/fabien/projets/jnode/jnode2021";
    static final SourceRoot SRC_ROOT = sourceRoot(JNODE_HOME);
    protected static final String MAVEN_PLUGINS_DIR = "maven_plugins"; 
    protected static final String MAVEN_MIGRATION_DIR = "maven"; 
    
    // for faster process, use memory filesystem
    static final DestinationRoot DEST_ROOT = destinationRoot("/dev/shm/jnode_maven");
//    private static final DestinationRoot DEST_ROOT = destinationRoot(JNODE_HOME + "/jnode_maven");

    public static void main(String[] args) throws Exception {
        Log.debug("Migration from " + SRC_ROOT + " to " + DEST_ROOT);
        delete(DEST_ROOT.getDirectory());

        PluginInfoFinder pluginInfoFinder = new PluginInfoFinder(SRC_ROOT);
        PluginPOMWriter pluginPOMWriter = new PluginPOMWriter(DEST_ROOT);
        ProjectPOMWriter projectPOMWriter = new ProjectPOMWriter(SRC_ROOT, DEST_ROOT);
        new RootPOMWriter(DEST_ROOT).write();
        PluginInfos pluginInfos = new PluginInfos();
        for (org.jnode.mavenizer.Project project : values()) {
            projectPOMWriter.write(project);
            for (PluginInfo pluginInfo : pluginInfoFinder.find(project)) {
                pluginInfos.add(pluginInfo);
            }
        }
        for (PluginInfo pluginInfo : pluginInfos.plugins()) {
            pluginPOMWriter.write(pluginInfos, pluginInfo);
        }

        List<PluginDescriptor> systemPlugins = new ArrayList<PluginDescriptor>();
        MavenMultiProject root = buildProjectTree(SRC_ROOT, DEST_ROOT, systemPlugins);
        print(root);

        mavenize(pluginInfos, root);
    }

    public static void check(PluginDescriptor desc) {
        // check exports
        if (desc.getRuntime() == null) {
            Log.warn(desc.getId() + ": no runtime");
        } else if (desc.getRuntime().getLibraries() == null) {
            Log.warn(desc.getId() + ": a runtime without library");
        } else {
            for (Library lib : desc.getRuntime().getLibraries()) {
                boolean exist = ANT_PROJECT.getProperties().containsKey(lib.getName());
                List<String> classes = exist ? scanLibrary(ANT_PROJECT.getProperty(lib.getName())) : EMPTY_LIST;
                
                boolean error1 = false;
                boolean error2 = false;
                boolean error3 = false;
                for (String export : lib.getExports()) {                    
                    if ((export == null) || export.isEmpty()) {
                        if (!error1) {
                            error1 = true;
                            Log.warn(desc.getId() + ": null or empty export");
                        }
                    } else if ("*".equals(export)) {
                        if (!exist) {
                            if (!error2) {
                                error2 = true;
                                Log.warn(desc.getId() + ": too generic (only a single '*') without a library path");
                            }
                        }
                    } else if (!export.endsWith(".*")) {
                        if (!error3) {
                            error3 = true;
                            Log.warn(desc.getId() + ": doesn't end with \".*\")");
                        }
                    }
                }
            }
        }
    }
    
    private static List<String> scanLibrary(String filepath) {
        try {
            File f = new File(filepath);
            if (!f.exists()) {
                Log.warn("`nNOT FOUND : " + filepath + "\n");
            }
            if (!filepath.endsWith("/classes")) {
                if (!filepath.endsWith(".jar")) {
                    Log.warn("NOT A JAR : " + f.getCanonicalPath());
                } else {
                    Log.debug("LIB:" + filepath);
                }
            }
            return emptyList();
            
//            List<String> classes = new ArrayList<String>();
//            File f = new File(filepath);
//            JarFile jar = new JarFile(f);
//            for (Enumeration<JarEntry> e = jar.entries() ; e.hasMoreElements(); ) {
//                JarEntry entry = e.nextElement();
//                classes.add(entry.getName());
//            }
//            return classes;
        } catch (IOException e) {
            e.printStackTrace();
            return emptyList();
        }        
    }

    private static void mavenize(final PluginInfos pluginInfos, MavenMultiProject root) throws Exception {
        final Project antProject = createAntProject();
        final Parallel migrator = new Parallel();
        migrator.setProject(antProject);
        migrator.setThreadsPerProcessor(5);
        
        MavenProjectVisitor v = new MavenProjectVisitor() {
            @Override
            public void visitMultiProject(MavenMultiProject project) {
                // nothing
            }

            @Override
            public void visitPluginProject(MavenPluginProject mavenPluginProject) {
                PluginMavenizer mavenizer = new PluginMavenizer(pluginInfos, mavenPluginProject);
                mavenizer.setProject(antProject);
                migrator.addTask(mavenizer);
            }
        };
        root.accept(v);
        migrator.execute();
    }
}
