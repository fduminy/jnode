package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Files {
    private Files() {
        // utility class
    }

    public static void deleteAll(final Path root) throws IOException {
        if (!exists(root)) {
            createDirectories(root);
            return;
        }
        walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Objects.equals(dir, root.resolve("scripts"))) {
                    return SKIP_SUBTREE;
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (!Objects.equals(dir, root)) {
                    delete(dir);
                }
                return CONTINUE;
            }
        });
    }
}
