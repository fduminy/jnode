package org.jnode.mavenizer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.bsf.util.StringUtils.lineSeparator;
import static org.apache.tools.ant.util.FileUtils.safeReadFully;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Conditions.childOf;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCIES_BEGIN;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCIES_END;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCY_BEGIN;
import static org.jnode.mavenizer.PluginPOMWriter.DEPENDENCY_END;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.PluginPOMWriterTest.XML_EXTENSION;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULES_BEGIN;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULES_END;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULE_BEGIN;
import static org.jnode.mavenizer.ProjectPOMWriter.MODULE_END;

public class AbstractPOMWriterTest extends AbstractTestWithDestinationRoot {
    final String assertCommon(String pomDirectory, String pluginId, boolean thirdParty, File pomFile,
                              String packaging, String projectId) throws IOException {
        assertThat(pomFile).isNotNull();
        assertThat(pomFile)
            .hasName("pom.xml")
            .is(childOf(pomDirectory));
        String pom = safeReadFully(new FileReader(pomFile));
        assertThat(pom)
            .contains("<groupId>org.jnode." + projectId + "</groupId>")
            .contains("<artifactId>" + pluginId + "</artifactId>")
            .contains("<packaging>" + packaging + "</packaging>");
        if (thirdParty) {
            assertThat(pom).doesNotContain("<version>" + JNODE_VERSION + "</version>");
        } else {
            assertThat(pom).contains("<version>" + JNODE_VERSION + "</version>");
        }
        return pom;
    }

    static PluginInfo getPluginInfo(String pluginId) {
        File root = new File(SRC_ROOT.getDirectory(), TEST_PROJECT.getDirectory());
        File descriptorsDir = new File(root, "descriptors");
        File descriptorFile = new File(descriptorsDir, pluginId + '.' + XML_EXTENSION);
        return new PluginInfo(TEST_PROJECT, descriptorFile);
    }

    final List<String> extractModules(String pom) {
        return extractItems(pom, MODULES_BEGIN, MODULES_END, MODULE_BEGIN, MODULE_END);
    }

    final List<String> extractDependencies(String pom) {
        List<String> dependencies =
            extractItems(pom, DEPENDENCIES_BEGIN, DEPENDENCIES_END, DEPENDENCY_BEGIN, DEPENDENCY_END);
        for (int i = 0; i < dependencies.size(); i++) {
            String dependency = dependencies.get(i).replace(" ", "").replace(lineSeparator, "");
            dependency = dependency.replace("<groupId>", "").replace("</groupId>", ":");
            dependency = dependency.replace("<artifactId>", "").replace("</artifactId>", ":");
            dependency = dependency.replace("<version>", "").replace("</version>", "");
            dependency = dependency.trim();
            dependencies.set(i, dependency);
        }
        return dependencies;
    }

    final List<String> extractItems(String pom, String beginItemsTag, String endItemsTag, String beginItemTag, String endItemTag) {
        List<String> items = new ArrayList<String>();
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
        assertThat(end).isGreaterThan(0);

        return new Content(xml, begin, end);
    }

    private static class Content {
        private final String xml;
        private final int begin;
        private final int end;

        private Content(String xml, int begin, int end) {
            this.xml = xml;
            this.begin = begin;
            this.end = end;
        }

        private String getValue() {
            return xml.substring(begin, end);
        }
    }
}