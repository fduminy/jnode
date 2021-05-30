package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginInfoFinderTest.NB_TEST_PROJECT_MODULES;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.Project.Core;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ProjectPOMWriterTest extends AbstractPOMWriterTest {
    private PluginInfos pluginInfos;
    private ProjectPOMWriter writer;

    @Before
    public void setUp() {
        pluginInfos = mock(PluginInfos.class);
        writer = new ProjectPOMWriter(SRC_ROOT, destinationRoot, pluginInfos);
    }

    @Test
    public void write_jnode_plugin() throws IOException {
        write(TEST_PROJECT, NB_TEST_PROJECT_MODULES);
    }

    @Test
    public void write_jnode_vm_plugin() throws IOException {
        List<String> modules = write(Core, 88);
        assertThat(modules)
            .doesNotContain("org.classpath.core.vm").contains("rt.vm")
            .doesNotContain("org.classpath.core").contains("rt");
    }

    private List<String> write(Project project, int nbModules) throws IOException {
        List<PluginInfo> plugins = new ArrayList<PluginInfo>();
        for (File file : project.getDescriptorFiles()) {
            plugins.add(new PluginInfo(project, file));
        }
        doReturn(plugins).when(pluginInfos).plugins();

        File pomFile = writer.write(project);

        String projectDir = new File(destinationRoot.getDirectory(), project.getDirectory()).getAbsolutePath();
        String pom = assertCommon(projectDir, "project", false, pomFile, "pom", project.getDirectory());
        List<String> modules = extractModules(pom);
        assertThat(modules).doesNotHaveDuplicates().hasSize(nbModules);
        assertThat(extractDependencies(pom)).isEmpty();
        return modules;
    }
}