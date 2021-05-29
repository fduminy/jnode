package org.jnode.mavenizer;

import java.io.File;
import java.util.Arrays;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;
import org.jnode.plugin.Library;
import org.jnode.plugin.Runtime;

import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.SourceFileType.JAVA;
import static org.jnode.mavenizer.SourceFileType.RESOURCES;
import static org.jnode.mavenizer.Utils.createAntProject;
import static org.jnode.mavenizer.Utils.getPluginHome;
import static org.jnode.mavenizer.Utils.isBlank;

public class SourceCopier {
    private final SourceRoot sourceRoot;
    private final DestinationRoot destinationRoot;

    public SourceCopier(SourceRoot sourceRoot, DestinationRoot destinationRoot) {
        this.sourceRoot = sourceRoot;
        this.destinationRoot = destinationRoot;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void copy(PluginInfo pluginInfo) {
        if (pluginInfo.isThirdParty()) {
            return;
        }
        File pluginHome = getPluginHome(destinationRoot, pluginInfo);
        pluginHome.mkdirs();

        copy("main", true, pluginInfo, pluginHome, pluginInfo.getProject().getSourceDirectories(sourceRoot));
        copy("test", false, pluginInfo, pluginHome, pluginInfo.getProject().getTestDirectory(sourceRoot));
    }


    private void copy(String mavenPart, boolean mandatory, PluginInfo pluginInfo, File pluginHome, File... srcDirectories) {
        copyImpl(pluginInfo, pluginHome, mavenPart, JAVA, mandatory, srcDirectories);
        copyImpl(pluginInfo, pluginHome, mavenPart, RESOURCES, false, srcDirectories);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void copyImpl(PluginInfo pluginInfo, File pluginHome, String mavenPart, SourceFileType type, boolean mandatory, File... srcDirectories) {
        Copy c = new Copy();
        File target = new File(pluginHome, "src/" + mavenPart + '/' + type.getMavenDirectory());
        c.setTodir(target);
        target.mkdirs();
        boolean sourceIsDefined = false;

        for (File srcDir : srcDirectories) {
            Runtime runtime = pluginInfo.getPluginDescriptor().getRuntime();
            if (srcDir.exists() && (runtime != null) && (runtime.getLibraries() != null)) {
                for (Library library : runtime.getLibraries()) {
                    String libPath = ANT_PROJECT.getProperty(library.getName());
                    if (!isBlank(libPath)) {
                        File libFile = new File(libPath);

                        if (libFile.isDirectory()) {
                            FileSet fs = new FileSet();

                            fs.setDir(srcDir);
                            sourceIsDefined = true;

                            final String[] exports = library.getExports();
                            for (String export : exports) {
                                addIncludes(fs, export, type);
                            }
                            c.addFileset(fs);
                        }
                    }
                }
            }
        }

        if (sourceIsDefined) {
            c.setProject(createAntProject());
            c.execute();
        } else if (mandatory) {
            Log.warn("No source file to copy from " + Arrays.toString(srcDirectories));
        }
    }

    private void addIncludes(FileSet fileSet, String export, SourceFileType type) {
        for (String fileExtension : type.getFileExtensions()) {
            fileExtension = '.' + fileExtension;
//            if (export.equals("*")) {
//            addInclude(fileSet, "**/*" + fileExtension);
//            } else {
                String exp = export.replace('.', '/');
                addInclude(fileSet, exp + fileExtension);
//            }
        }
    }

    private void addInclude(FileSet fileSet, String include) {
        fileSet.createInclude().setName(include);
    }
}
