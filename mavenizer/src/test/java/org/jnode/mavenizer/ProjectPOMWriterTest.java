package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.stream.Collectors.toCollection;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginInfoFinderTest.NB_TEST_PROJECT_MODULES;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.Project.Core;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectPOMWriterTest extends AbstractPOMWriterTest {
    @Mock
    private PluginInfos pluginInfos;

    private ProjectPOMWriter writer;

    @BeforeEach
    public void setUp() {
        writer = new ProjectPOMWriter(SRC_ROOT, destinationRoot, pluginInfos);
    }

    @Test
    public void write_jnode_plugin() throws IOException {
        write(TEST_PROJECT, NB_TEST_PROJECT_MODULES);
    }

    @Test
    public void write_jnode_vm_plugin() throws IOException {
        List<String> modules = write(Core, 88);
        softly.assertThat(modules)
            .doesNotContain("org.classpath.core.vm").contains("rt.vm")
            .doesNotContain("org.classpath.core").contains("rt");
    }

    private List<String> write(Project project, int nbModules) throws IOException {
        when(pluginInfos.plugins()).thenReturn(project.getDescriptorFiles().stream()
            .map(file -> new PluginInfo(project, file)).collect(toCollection(ArrayList::new)));

        Path pomFile = writer.write(project);

        Path projectDir = destinationRoot.getDirectory().resolve(project.getDirectory()).toAbsolutePath();
        String pom = assertCommon(projectDir, "project", false, pomFile, "pom", project.getDirectory());
        List<String> modules = extractModules(pom);
        softly.assertThat(modules).doesNotHaveDuplicates().hasSize(nbModules);
        softly.assertThat(extractDependencies(pom)).isEmpty();
        return modules;
    }
}