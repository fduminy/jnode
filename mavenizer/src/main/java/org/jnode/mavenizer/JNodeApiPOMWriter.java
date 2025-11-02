package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jnode.mavenizer.Directory.DestinationRoot;

/**
 * Générateur de POM pour le module jnode-api qui contient les interfaces
 * communes pour briser les dépendances circulaires.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class JNodeApiPOMWriter extends AbstractPOMWriter {

    public JNodeApiPOMWriter(DestinationRoot destinationRoot) {
        super(destinationRoot);
    }

    public void write() {
        try {
            Path apiDir = destinationRoot.getDirectory().resolve("jnode-api");
            Files.createDirectories(apiDir);

            String pomContent = generateApiPOM();
            Files.write(apiDir.resolve("pom.xml"), pomContent.getBytes());

            // Créer la structure Maven standard
            Files.createDirectories(apiDir.resolve("src/main/java"));
            Files.createDirectories(apiDir.resolve("src/test/java"));

            Log.debug("Générée: jnode-api/pom.xml");

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du module jnode-api", e);
        }
    }

    private String generateApiPOM() {
        StringBuilder pom = new StringBuilder();
        pom.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        pom.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        pom.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        pom.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        pom.append("    <modelVersion>4.0.0</modelVersion>\n\n");

        pom.append("    <parent>\n");
        pom.append("        <groupId>org.jnode</groupId>\n");
        pom.append("        <artifactId>jnode</artifactId>\n");
        pom.append("        <version>1.0-SNAPSHOT</version>\n");
        pom.append("    </parent>\n\n");

        pom.append("    <artifactId>jnode-api</artifactId>\n");
        pom.append("    <packaging>jar</packaging>\n");
        pom.append("    <name>JNode API</name>\n");
        pom.append("    <description>Interfaces et APIs communes de JNode pour éviter les dépendances circulaires</description>\n\n");

        pom.append("    <properties>\n");
        pom.append("        <maven.compiler.source>6</maven.compiler.source>\n");
        pom.append("        <maven.compiler.target>6</maven.compiler.target>\n");
        pom.append("    </properties>\n\n");

        pom.append("    <dependencies>\n");
        pom.append("        <!-- Dépendances minimales pour les interfaces -->\n");
        pom.append("    </dependencies>\n\n");

        pom.append("</project>\n");

        return pom.toString();
    }
}
