package org.jnode.mavenizer;

import static java.util.Arrays.asList;

public enum SourceFileType {
    JAVA("java", "java"),
    RESOURCES("resources", "xml", "properties");

    private final String mavenDirectory;
    private final Iterable<String> fileExtensions;

    SourceFileType(String mavenDirectory, String... fileExtensions) {
        this.mavenDirectory = mavenDirectory;
        this.fileExtensions = asList(fileExtensions);
    }

    final Iterable<String> getFileExtensions() {
        return fileExtensions;
    }

    final String getMavenDirectory() {
        return mavenDirectory;
    }
}
