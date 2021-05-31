package org.jnode.mavenizer;

import java.nio.file.Path;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Directory {
    private final Path directory;

    private Directory(Path directory) {
        this.directory = directory;
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return directory.toAbsolutePath().toString();
    }

    public static class SourceRoot extends Directory {
        public static SourceRoot sourceRoot(Path directory) {
            return new SourceRoot(directory);
        }

        private SourceRoot(Path directory) {
            super(directory);
        }
    }

    public static class DestinationRoot extends Directory {
        public static DestinationRoot destinationRoot(Path directory) {
            return new DestinationRoot(directory);
        }

        private DestinationRoot(Path directory) {
            super(directory);
        }
    }
}
