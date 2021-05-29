package org.jnode.mavenizer;

import java.io.File;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class ProjectPrinter {
    public static void print(final MavenMultiProject root) throws Exception {
        MavenProjectVisitor v = new MavenProjectVisitor() {
            private final File rootPath = root.getBaseDirectory().getParentFile();
            
            @Override
            public void visitMultiProject(MavenMultiProject project) throws Exception {
                print(project, false);
            }

            @Override
            public void visitPluginProject(MavenPluginProject mavenPluginProject) throws Exception {
                print(mavenPluginProject, true);
            }
            
            private void print(MavenProject project, boolean plugin) {
                StringBuilder sb = new StringBuilder(); 
                leftPad(sb, getLevel(project) * 4);
                
                sb.append(project.getBaseDirectory().getName());
                if (plugin) {
                    rightPad(sb, 50);
                    MavenPluginProject pp = (MavenPluginProject) project;
                    sb.append(" <=").append(shorten(pp.getSrcRoot().getDirectory())).append(" + ").append(pp.getName());
                }
                
                Log.debug(sb.toString());
            }
            
            private String shorten(File f) {
                return Utils.getRelativePath(rootPath, f, "...");
            }
            
            private void leftPad(StringBuilder sb, int size) {
                for (int i = sb.length(); i < size; i++) {
                    sb.insert(0, ' ');
                }
            }
            
            private void rightPad(StringBuilder sb, int size) {
                for (int i = sb.length(); i < size; i++) {
                    sb.append(' ');
                }
            }
            
            private int getLevel(MavenProject project) {
                int level = 0;
            
                if (project.getParent() != null) {
                    level = 1 + getLevel(project.getParent());
                }
                
                return level;
            }
        };
        root.accept(v);
    }
}
