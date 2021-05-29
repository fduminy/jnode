package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

import static org.jnode.mavenizer.Utils.getRelativePath;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class ProjectTreeBuilder {
    public static MavenMultiProject buildProjectTree(SourceRoot sourceRoot, DestinationRoot destinationRoot, List<PluginDescriptor> systemPlugins) throws IOException, PluginException {
        MavenMultiProject root = new MavenMultiProject(null, sourceRoot, destinationRoot, "");
        PluginInfoFinder pluginInfoFinder = new PluginInfoFinder(sourceRoot);

        for (Project project : Project.values()) {
            systemPlugins.addAll(buildProjectTree(root, project, pluginInfoFinder));
        }

        return root;
    }
    
    private static List<PluginDescriptor> buildProjectTree(MavenMultiProject parent, Project project, PluginInfoFinder pluginInfoFinder) {
        File descriptorsDir = project.getDescriptorsDirectory(parent.getSrcRoot());
        JNodeSubProject jnodeSubProject = new JNodeSubProject(parent, project.getDirectory());
        boolean descriptorsRead = false;
        List<PluginDescriptor> systemPlugins = new ArrayList<PluginDescriptor>();
        
        File rootSourceDirectory = project.getRootSourceDirectory(parent.getSrcRoot());
        File[] srcDirs = project.getSourceDirectories(parent.getSrcRoot());

        for (File srcDir : srcDirs) {
            MavenMultiProject p1 = buildIntermediateProjects(jnodeSubProject, rootSourceDirectory, srcDir);

            if (!descriptorsRead && descriptorsDir.exists()) {
                descriptorsRead = true;
                for (PluginInfo pluginInfo : pluginInfoFinder.find(project)) {
                    MavenPluginProject mavenProject = new MavenPluginProject(p1, project, pluginInfo);
                    p1.addChild(mavenProject);
/* FIXME
                    if (descriptor.isSystemPlugin()) {
                        systemPlugins.add(descriptor);
                    }
*/
                }
            }
        }
        
        parent.addChild(jnodeSubProject);
        
        return systemPlugins;
    }

    private static MavenMultiProject buildIntermediateProjects(JNodeSubProject jnodeSubProject, File rootSourceDirectory, File srcDir) {
        MavenMultiProject p1 = jnodeSubProject;

        MavenMultiProject p2;

        for (String path : getRelativePath(rootSourceDirectory, srcDir)) {
            p2 = (MavenMultiProject) p1.findChildByName(path);
            if (p2 == null) {
                p2 = new MavenMultiProject(p1, path);
                p1.addChild(p2);
            }

            p1 = p2;
        }
        return p1;
    }
}
