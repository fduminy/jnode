package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSortedSet;

public class Export {
    private final Path sourceDirectory;
    private final SortedSet<String> packageFilters;

    public Export(Path sourceDirectory, List<String> packageFilters) {
        this.sourceDirectory = sourceDirectory;
        this.packageFilters = unmodifiableSortedSet(new TreeSet<>(packageFilters));
    }

    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    public SortedSet<String> getPackageFilters() {
        return packageFilters;
    }

    @Override
    public String toString() {
        return "Export{" +
            "sourceDirectory=" + sourceDirectory +
            ", packageFilters=" + packageFilters +
            '}';
    }
}
