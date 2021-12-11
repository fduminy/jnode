package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.tools.ant.BuildException;
import org.assertj.core.api.Condition;

import static java.lang.String.format;
import static java.nio.file.Files.isSameFile;

public class Conditions {
    @SuppressWarnings("SameParameterValue")
    static Condition<Path> childOf(final Directory root, final Project project, final String subDirectory) {
        return childOf(getExpectedPath(root, project, subDirectory));
    }

    @SuppressWarnings("SameParameterValue")
    static Condition<Path> childOf(final Path expectedPath) {
        return new Condition<>() {
            @Override
            public boolean matches(Path file) {
                if ((file == null) || (file.getParent() == null)) {
                    return false;
                }
                try {
                    return isSameFile(file.getParent(), expectedPath);
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }

            @Override
            public String toString() {
                return "child of " + expectedPath + " directory";
            }
        };
    }

    public static Condition<Path> extension(final String extension) {
        return new Condition<>() {
            @Override
            public boolean matches(Path value) {
                return value.getFileName().toString().endsWith("." + extension);
            }

            @Override
            public String toString() {
                return format("extension %s", extension);
            }
        };
    }

    static Path getExpectedPath(Directory root, IProject project, String subDirectory) {
        return root.getDirectory().resolve(project.getDirectory()).resolve(subDirectory).toAbsolutePath();
    }
}
