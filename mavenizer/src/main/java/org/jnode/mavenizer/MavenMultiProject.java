package org.jnode.mavenizer;

import java.util.ArrayList;
import java.util.List;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class MavenMultiProject extends MavenProject {
    private final List<MavenProject> children = new ArrayList<MavenProject>();

    public MavenMultiProject(MavenMultiProject parent, SourceRoot sourceRoot, DestinationRoot destinationRoot, String name) {
        super(parent, sourceRoot, destinationRoot, name);
    }

    public MavenMultiProject(MavenMultiProject parent, String name) {
        this(parent, null, null, name);
    }

    public void addChild(MavenProject childProject) {
        children.add(childProject);        
    }
    
    public MavenProject findChildByName(String name) {
        for (MavenProject child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }
    
    @Override
    public void accept(MavenProjectVisitor visitor) throws Exception {
        visitor.visitMultiProject(this);
        
        for (MavenProject child : children) {
            child.accept(visitor);
        }
    }

    public List<String> getChildrenNames() {
        List<String> result = new ArrayList<String>(children.size());
        for (MavenProject child : children) {
            result.add(child.getBaseDirectory().getName());
        }
        return result;
    }

    public void removeChild(MavenPluginProject mavenPluginProject) {
        children.remove(mavenPluginProject);
    }
}
