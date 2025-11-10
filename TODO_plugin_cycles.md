Classification des 12 cycles par difficulté (du plus facile au plus difficile):

**FACILES (interfaces isolées, peu de dépendances):**
1. **org.jnode.permission ↔ rt.vm** - Permission utilise probablement des types VM basiques. Solution: créer un module d'interfaces de sécurité.

2. **org.jnode.runtime.core ↔ org.jnode.runtime.core.bootlog** - Bootlog est une interface simple. Solution: déplacer l'interface bootlog vers runtime.core ou créer un module api.

**MOYENS (nécessitent extraction d'interfaces):**
3. **org.jnode.fs ↔ org.jnode.fs.service** - org.jnode.fs importe fs.service (ligne 32 du descriptor). Solution: extraire les interfaces de service vers un module séparé.

4. **org.jnode.driver.block ↔ org.jnode.fs** - Similar au cycle driver.block/partitions déjà résolu. Solution: identifier et déplacer les interfaces communes.

5. **org.jnode.plugin.impl ↔ org.jnode.runtime.core** - Implémentation vs runtime. Solution: séparer les interfaces d'implémentation.

**DIFFICILES (cycles au cœur du VM, interdépendances complexes):**
6. **org.jnode.runtime.core.resource ↔ org.jnode.vm.core** - Resources système vs VM core.

7. **rt.vm ↔ org.jnode.util** - Runtime VM de base utilise des utilitaires. Solution: extraire interfaces utilitaires minimales.

8. **org.jnode.vm.core ↔ org.jnode.vm** - Séparation VM/VM.core peu claire.

9. **org.jnode.vm ↔ org.jnode.runtime.core** - runtime.core importe vm (ligne 34), interdépendance bidirectionnelle profonde.

**TRÈS DIFFICILES (cycles architecturaux fondamentaux):**
10. **org.jnode.plugin ↔ org.jnode.vm.core** - org.jnode.plugin importe vm.core (ligne 31), cycle au cœur du bootstrap. Refonte architecturale majeure requise.

11. **rt.vm ↔ org.classpath.ext.core.vm** - Cycle entre runtime et classpath VM, touches aux fondations du système.

12. **org.jnode.runtime.core ↔ org.jnode.vm.core** - runtime.core importe vm (ligne 34), le plus complexe car au cœur du bootstrap système.

Recommandation: commencer par les cycles 1-5.