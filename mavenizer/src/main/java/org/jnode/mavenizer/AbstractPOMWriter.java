package org.jnode.mavenizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FilterSet;
import org.jnode.mavenizer.Directory.DestinationRoot;

import static org.apache.bsf.util.StringUtils.lineSeparator;
import static org.jnode.mavenizer.Utils.createAntProject;
import static org.jnode.mavenizer.Utils.readFully;

abstract class AbstractPOMWriter {
    static final String MODULES_BEGIN = "<modules>";
    static final String MODULES_END = "</modules>";
    static final String MODULE_BEGIN = "<module>";
    static final String MODULE_END = "</module>";
    private static final String PROJECT_END = "</project>";
    static final String INDENT = "    ";
    protected final DestinationRoot destinationRoot;

    AbstractPOMWriter(DestinationRoot destinationRoot) {
        this.destinationRoot = destinationRoot;
    }

    final File write(File directory, String projectId, String artifactId, String version, String packaging) {
        URL pomTemplate = PluginPOMWriter.class.getResource("plugin-pom.template.xml");
        if (pomTemplate == null) {
            throw new RuntimeException("POM template not found");
        }
        File file = new File(directory, "pom.xml");

        Project antProject = createAntProject();
        Copy copy = new Copy();
        copy.setProject(antProject);
        copy.setTofile(file);
        copy.setOverwrite(true);
        copy.setFailOnError(true);
        copy.setFile(new File(pomTemplate.getFile()));
        copy.setFiltering(true);

        FilterSet filter = copy.createFilterSet();
        filter.addFilter("projectId", projectId);
        filter.addFilter("artifactId", artifactId);
        filter.addFilter("version", version);
        filter.addFilter("packaging", packaging);

        copy.execute();
        return file;
    }

    final void addModules(File pomFile, List<String> modules) {
        StringBuilder modulesXML = new StringBuilder(INDENT).append(MODULES_BEGIN).append(lineSeparator);
        for (String module : modules) {
            modulesXML.append(INDENT).append(INDENT).append(MODULE_BEGIN)
                .append(module)
                .append(MODULE_END).append(lineSeparator);
        }
        modulesXML.append(INDENT).append(MODULES_END).append(lineSeparator);
        append(pomFile, modulesXML);
    }

    final void append(File pomFile, StringBuilder modulesXML) {
        try {
            String pom = readFully(pomFile);
            FileWriter writer = new FileWriter(pomFile);
            try {
                writer.write(pom.substring(0, pom.lastIndexOf(PROJECT_END)));
                writer.write(modulesXML.toString());
                writer.write(PROJECT_END);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
}

//TODO extract useful stuff from that class ?
class POMBuilder  {
    private static final String GROUP_ID = "jnode";

    private final PluginPOMWriter pomWriter;

    public POMBuilder(PluginPOMWriter pomWriter) {
        this.pomWriter = pomWriter;
    }

    public void visitPluginProject() {
/*
        pomWriter.write(null, project.getPluginInfo());
        File projectRoot = project.getBaseDirectory();
        projectRoot.mkdirs();
        PluginDescriptor desc = null; //FIXME project.getPluginDescriptor();

        File out = new File(projectRoot, "pom.xml");
        FileWriter fw = new FileWriter(out);

        write(projectRoot, project, fw, desc.getId(), desc.getName(), desc.getProviderUrl(), "jar");

        fw.write("    <dependencies>\n");
        fw.write("        <dependency>\n");
        fw.write("            <groupId>junit</groupId>\n");
        fw.write("            <artifactId>junit</artifactId>\n");
        fw.write("            <version>3.8.1</version>\n");
        fw.write("            <scope>test</scope>\n");
        fw.write("        </dependency>\n");

        for (PluginPrerequisite prereq : desc.getPrerequisites()) {
            writeDependency(fw, prereq.getPluginReference().getId(), getVersion(prereq));
        }

        fw.write("\n        <!-- System plugins -->\n");
        if (desc.isSystemPlugin()) {
            List<String> mySystemDependencies = systemDependencies.get(desc.getId());
            if (mySystemDependencies != null) {
                for (PluginDescriptor systemPlugin : systemPlugins) {
                    if (!(desc.getVersion().equals(systemPlugin.getVersion()) &&
                          desc.getId().equals(systemPlugin.getId())) &&
                          mySystemDependencies.contains(systemPlugin.getId())) {
                        writeDependency(fw, systemPlugin.getId(), getVersion(systemPlugin));
                    }
                }
            }
        } else {
            for (PluginDescriptor systemPlugin : systemPlugins) {
                if (!(desc.getVersion().equals(systemPlugin.getVersion()) &&
                      desc.getId().equals(systemPlugin.getId()))) {
                    writeDependency(fw, systemPlugin.getId(), getVersion(systemPlugin));
                }
            }
        }

        fw.write("    </dependencies>\n");

        fw.write("</project>\n");
        fw.close();
*/
    }

    private void write() {
/*

        w.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        w.write("xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n");

        String tab = "    ";

        writeAttribute(w, "modelVersion", tab, "4.0.0");
        writeAttribute(w, "artifactId", tab, id);
        writeAttribute(w, "packaging", tab, packaging);

        if (project.getParent() != null) {
            String version = null;
            if (project instanceof MavenPluginProject) {
                // use plugin descriptor properties
                MavenPluginProject pp = (MavenPluginProject) project;
                version = getVersion((PluginDescriptor) null */
/*FIXME pp.getPluginDescriptor()*//*
);
            }
            writeAttribute(w, "version", tab, processVersion(version));

            writeAttribute(w, "name", tab, getUniqueName(project.getParent()));

            // define link with parent's POM
            tab += tab;
            w.write("    <parent>\n");
            writeAttribute(w, "groupId", tab, GROUP_ID);
            writeAttribute(w, "artifactId", tab, getUniqueArtifactId(project.getParent()));
            writeAttribute(w, "version", tab, ALIAS_JNODE_VERSION);
            w.write("    </parent>\n");
        } else {
            // root project : define global default properties
            writeAttribute(w, "name", tab, "jnode");
            writeAttribute(w, "groupId", tab, GROUP_ID);
            writeAttribute(w, "version", tab, ALIAS_JNODE_VERSION);
            writeAttribute(w, "url", tab, "http://www.jnode.org");

            w.write("\n    <properties>\n");
            writeAttribute(w, Utils.PROP_JNODE_VERSION, tab, JNODE_VERSION);
            w.write("    </properties>\n");

            w.write("\n    <build>\n");
            w.write("       <plugins>\n");
            w.write("          <plugin>\n");
            w.write("            <groupId>org.apache.maven.plugins</groupId>\n");
            w.write("            <artifactId>maven-compiler-plugin</artifactId>\n");
            w.write("            <version>2.0.2</version>\n");
            w.write("            <configuration>\n");
            w.write("                <source>1.6</source>\n");
            w.write("                <target>1.6</target>\n");

            w.write("                <fork>true</fork>\n");
            w.write("                <meminitial>128m</meminitial>\n");
            w.write("                <maxmem>512m</maxmem>\n");

            w.write("                <compilerArguments>\n");
            w.write("                    <nowarn/>\n");
            w.write("                    <bootclasspath>" + JNODE_HOME + "/core/build/classes</bootclasspath>\n");
            w.write("                </compilerArguments>\n");
            w.write("            </configuration>\n");
            w.write("          </plugin>\n");
            w.write("       </plugins>\n");
            w.write("    </build>\n");
        }
*/
    }
}
