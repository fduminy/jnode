package org.jnode.mavenizer;

import java.io.File;
import org.assertj.core.api.Condition;

import static java.lang.String.format;

public class Conditions {
    @SuppressWarnings("SameParameterValue")
    static Condition<File> childOf(final Directory root, final Project project, final String subDirectory) {
        return childOf(getExpectedPath(root, project, subDirectory));
    }

    @SuppressWarnings("SameParameterValue")
    static Condition<File> childOf(final String expectedPath) {
        return new Condition<File>() {
            @Override
            public boolean matches(File file) {
                if ((file == null) || (file.getParentFile() == null)) {
                    return false;
                }
                return file.getParentFile().getAbsolutePath().equals(expectedPath);
            }

            @Override
            public String toString() {
                return "child of " + expectedPath + " directory";
            }
        };
    }

    public static Condition<? super String> extension(final String extension) {
        return new Condition<String>() {
            @Override
            public boolean matches(String value) {
                return value.endsWith("." + extension);
            }

            @Override
            public String toString() {
                return format("extension %s", extension);
            }
        };
    }

    static String getExpectedPath(Directory root, Project project, String subDirectory) {
        return new File(new File(root.getDirectory(), project.getDirectory()), subDirectory).getAbsolutePath();
    }
}
