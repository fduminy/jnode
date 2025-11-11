================================================================================
ANALYSE DES DÉPENDANCES CIRCULAIRES AU NIVEAU DU CODE SOURCE
================================================================================

Nombre total de plugins analysés: 229
Nombre de plugins avec des fichiers Java: 185
Nombre total de cycles détectés: 2


================================================================================
DIFFICULTÉ: EASY
================================================================================

Cycle #20: org.jnode.net ↔ org.jnode.net.arp
--------------------------------------------------------------------------------
Difficulté: EASY
Raisonnement: Simple cycle between two plugins - can be resolved by extracting interfaces to a separate module.

Plugins impliqués:
  - org.jnode.net
    Fichier: net/descriptors/org.jnode.net.xml
    Sous-projet: net
    Fichiers Java: 44
  - org.jnode.net.arp
    Fichier: net/descriptors/org.jnode.net.arp.xml
    Sous-projet: net
    Fichiers Java: 8

Dépendances circulaires (au niveau du code source):
  org.jnode.net → org.jnode.net.arp
  org.jnode.net.arp → org.jnode.net


================================================================================
DIFFICULTÉ: VERY_HARD
================================================================================

Cycle #110: org.jnode.plugin ↔ org.jnode.plugin.impl ↔ org.jnode.runtime.core ↔ org.jnode.runtime.core.resource ↔ org.jnode.util ↔ org.jnode.vm ↔ org.jnode.vm.core ↔ org.vmmagic ↔ rt ↔ rt.vm
--------------------------------------------------------------------------------
Difficulté: VERY_HARD
Raisonnement: Cycle involves 4 VM components and 1 runtime components - requires architectural refactoring of bootstrap system.

Plugins impliqués:
  - org.jnode.plugin
    Fichier: core/descriptors/org.jnode.plugin.xml
    Sous-projet: core
    Fichiers Java: 37
  - org.jnode.plugin.impl
    Fichier: core/descriptors/org.jnode.plugin.impl.xml
    Sous-projet: core
    Fichiers Java: 38
  - org.jnode.runtime.core
    Fichier: core/descriptors/org.jnode.runtime.core.xml
    Sous-projet: core
    Fichiers Java: 17
  - org.jnode.runtime.core.resource
    Fichier: core/descriptors/org.jnode.runtime.core.resource.xml
    Sous-projet: core
    Fichiers Java: 15
  - org.jnode.util
    Fichier: core/descriptors/org.jnode.util.xml
    Sous-projet: core
    Fichiers Java: 43
  - org.jnode.vm
    Fichier: core/descriptors/org.jnode.vm_x86.xml
    Sous-projet: core
    Fichiers Java: 121
  - org.jnode.vm.core
    Fichier: core/descriptors/org.jnode.vm.core.xml
    Sous-projet: core
    Fichiers Java: 288
  - org.vmmagic
    Fichier: core/descriptors/org.vmmagic.xml
    Sous-projet: core
    Fichiers Java: 21
  - rt
    Fichier: core/descriptors/org.classpath.core.xml
    Sous-projet: core
    Fichiers Java: 111
  - rt.vm
    Fichier: core/descriptors/org.classpath.core.vm.xml
    Sous-projet: core
    Fichiers Java: 12

Dépendances circulaires (au niveau du code source):
  org.jnode.plugin → org.jnode.runtime.core
  org.jnode.plugin → org.jnode.util
  org.jnode.plugin → rt.vm
  org.jnode.plugin.impl → org.jnode.plugin
  org.jnode.plugin.impl → org.jnode.runtime.core
  org.jnode.plugin.impl → org.jnode.util
  org.jnode.plugin.impl → org.jnode.vm.core
  org.jnode.plugin.impl → rt
  org.jnode.plugin.impl → rt.vm
  org.jnode.runtime.core → org.jnode.plugin
  org.jnode.runtime.core → org.jnode.plugin.impl
  org.jnode.runtime.core → org.jnode.runtime.core.resource
  org.jnode.runtime.core → org.jnode.util
  org.jnode.runtime.core → org.jnode.vm.core
  org.jnode.runtime.core → rt
  org.jnode.runtime.core.resource → org.vmmagic
  org.jnode.runtime.core.resource → rt
  org.jnode.util → org.jnode.runtime.core
  org.jnode.util → rt
  org.jnode.vm → org.jnode.runtime.core
  org.jnode.vm → org.jnode.runtime.core.resource
  org.jnode.vm → org.jnode.util
  org.jnode.vm → org.jnode.vm.core
  org.jnode.vm → org.vmmagic
  org.jnode.vm → rt
  org.jnode.vm.core → org.jnode.plugin
  org.jnode.vm.core → org.jnode.runtime.core
  org.jnode.vm.core → org.jnode.runtime.core.resource
  org.jnode.vm.core → org.jnode.util
  org.jnode.vm.core → org.jnode.vm
  org.jnode.vm.core → org.vmmagic
  org.jnode.vm.core → rt
  org.jnode.vm.core → rt.vm
  org.vmmagic → org.jnode.util
  org.vmmagic → org.jnode.vm.core
  org.vmmagic → rt
  rt → org.jnode.runtime.core
  rt → org.jnode.runtime.core.resource
  rt → org.jnode.vm.core
  rt → org.vmmagic
  rt → rt.vm
  rt.vm → org.jnode.vm.core
  rt.vm → rt


================================================================================
STATISTIQUES
================================================================================

Total de cycles: 2
  EASY: 1 cycle(s)
  VERY_HARD: 1 cycle(s)