Cherche à supprimer la dépendance circulaire entre les plugins

************* PUT PLUGIN LIST HERE *************

Vérifie chaque solution que tu proposes en suivant les étapes ci-dessous :
1 - vérifie que la solution proposée casse effectivement le cycle de dépendance entre les plugins
2 - vérifie que la solution proposée n'introduit pas de nouvelles dépendances circulaires parmi tous les plugins (pas seulement les plugins que j'ai listé plus haut)
3 - lance le script ./build.sh clean cd-x86-lite (le fichier manquant nommé classlib.jar est dans https://github.com/fduminy/classlib6/releases/tag/v0.1, il faut que tu installes java 8)
4 - vérifie le résultat de l'execution du script
5 - si le script a échoué, annule tes modifications , trouve une autre solution et recommence à l'étape 1
6 - si le script n'a pas échoué, lance le script ./qemu.sh
7 - surveille le fichier logs.txt
8 - si le fichier logs.txt contient une erreur ou exception après 60 secondes, annule tes modifications , trouve une autre solution et recommence à l'étape 1
9 - fait une dernière vérification avec le projet mavenizer dont voici la description :

Le répertoire mavenizer contient un projet maven qui convertit les plugins jnode en modules maven. 
Pour cela, il copie les fichiers source java de JNode vers le répertoire ../jnode_mavenized et crée 
les fichiers pom.xml en gardant la même arborescence de fichier source que JNode.
Compile le projet mavenizer et lance la classe principale org.jnode.mavenizer.Mavenizer.
Analyse les fichiers pom.xml générés dans le répertoire ../jnode_mavenized pour comprendre les dépendances entre les plugins JNode.
Il ne doit pas y avoir de dépendance circulaire entre les modules maven générés.