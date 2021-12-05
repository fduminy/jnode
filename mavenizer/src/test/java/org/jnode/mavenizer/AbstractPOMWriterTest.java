package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.lang.System.lineSeparator;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Paths.get;
import static org.jnode.mavenizer.Conditions.childOf;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCIES_BEGIN;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCIES_END;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCY_BEGIN;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCY_END;
import static org.jnode.mavenizer.PluginPOMWriterTest.XML_EXTENSION;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULES_BEGIN;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULES_END;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULE_BEGIN;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULE_END;
import static org.jnode.mavenizer.Utils.readDescriptor;

@ExtendWith(SoftAssertionsExtension.class)
public class AbstractPOMWriterTest extends AbstractTestWithDestinationRoot {
    @InjectSoftAssertions
    SoftAssertions softly;

    final String assertCommon(Path pomDirectory, String pluginId, boolean thirdParty, Path pomFile,
                              String packaging, String projectId) throws IOException {
        softly.assertThat(pomFile).isNotNull();
        softly.assertThat(pomFile).endsWith(get("pom.xml"))
            .is(childOf(pomDirectory));
        String pom = readString(pomFile);
        softly.assertThat(pom)
            .contains("<groupId>org.jnode." + projectId + "</groupId>")
            .contains("<artifactId>" + pluginId + "</artifactId>")
            .contains("<packaging>" + packaging + "</packaging>");
        if (thirdParty) {
            softly.assertThat(pom).doesNotContain("<version>" + JNODE_VERSION + "</version>");
        } else {
            softly.assertThat(pom).contains("<version>" + JNODE_VERSION + "</version>");
        }
        return pom;
    }

    static PluginInfo getPluginInfo(Project project, String pluginId) {
        return new PluginInfo(project, getDescriptorFile(project, pluginId));
    }

    static Path getDescriptorFile(Project project, String pluginId) {
        Path root = SRC_ROOT.getDirectory().resolve(project.getDirectory());
        Path descriptorsDir = root.resolve("descriptors");
        Path descriptorFile = descriptorsDir.resolve(pluginId + '.' + XML_EXTENSION);
        if (!exists(descriptorFile)) {
            for (Path descFile : project.getDescriptorFiles()) {
                if (readDescriptor(descFile).getId().equals(pluginId)) {
                    descriptorFile = descFile;
                    break;
                }
            }
        }
        return descriptorFile;
    }

    final List<String> extractModules(String pom) {
        return extractItems(pom, MODULES_BEGIN, MODULES_END, MODULE_BEGIN, MODULE_END);
    }

    final List<String> extractDependencies(String pom) {
        List<String> dependencies =
            extractItems(pom, DEPENDENCIES_BEGIN, DEPENDENCIES_END, DEPENDENCY_BEGIN, DEPENDENCY_END);
        for (int i = 0; i < dependencies.size(); i++) {
            String dependency = dependencies.get(i).replace(" ", "").replace(lineSeparator(), "");
            Content groupId = extractContent(dependency, 0, "<groupId>", "</groupId>");
            Content artifactId = extractContent(dependency, 0, "<artifactId>", "</artifactId>");
            Content version = extractContent(dependency, 0, "<version>", "</version>");
            softly.assertThat(groupId).isNotNull();
            softly.assertThat(artifactId).isNotNull();
            softly.assertThat(version).isNotNull();
            dependency = groupId.getValue() + ':' + artifactId.getValue() + ':' + version.getValue();
            dependencies.set(i, dependency);
        }
        return dependencies;
    }

    final List<String> extractItems(String pom, String beginItemsTag, String endItemsTag, String beginItemTag, String endItemTag) {
        List<String> items = new ArrayList<>();
        Content itemsContent = extractContent(pom, 0, beginItemsTag, endItemsTag);
        if (itemsContent != null) {
            String itemsXML = itemsContent.getValue();

            Content item;
            int fromIndex = 0;
            while ((item = extractContent(itemsXML, fromIndex, beginItemTag, endItemTag)) != null) {
                items.add(item.getValue());
                fromIndex = item.end + endItemTag.length();
            }
        }
        return items;
    }

    private Content extractContent(String xml, int fromIndex, String beginTag, String endTag) {
        int begin = xml.indexOf(beginTag, fromIndex);
        if (begin < 0) {
            return null;
        }
        begin += beginTag.length();

        int end = xml.indexOf(endTag, begin);
        softly.assertThat(end).isGreaterThan(0);

        return new Content(xml, begin, end);
    }

    private record Content(String xml, int begin, int end) {
        private String getValue() {
            return xml.substring(begin, end);
        }
    }
}