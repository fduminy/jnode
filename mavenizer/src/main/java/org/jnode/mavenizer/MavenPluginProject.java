package org.jnode.mavenizer;


/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class MavenPluginProject extends MavenProject {
    private final Project project;
    private final PluginInfo pluginInfo;

    public MavenPluginProject(MavenMultiProject parent, Project project, PluginInfo pluginInfo) {
        super(parent, null, null, pluginInfo.getId());
        this.project = project;
        this.pluginInfo = pluginInfo;
    }
    
    public void accept(MavenProjectVisitor visitor) throws Exception {
        visitor.visitPluginProject(this);
    }

    public Project getProject() {
        return project;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }
}
