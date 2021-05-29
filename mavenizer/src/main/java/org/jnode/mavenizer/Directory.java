package org.jnode.mavenizer;

import java.io.File;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Directory {
    private final File directory;

    private Directory(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return directory.getAbsolutePath();
    }

    public static class SourceRoot extends Directory {
        public static SourceRoot sourceRoot(String directory) {
            return new SourceRoot(new File(directory));
        }

        private SourceRoot(File directory) {
            super(directory);
        }
    }

    public static class DestinationRoot extends Directory {
        public static DestinationRoot destinationRoot(String directory) {
            return new DestinationRoot(new File(directory));
        }

        private DestinationRoot(File directory) {
            super(directory);
        }
    }
}
