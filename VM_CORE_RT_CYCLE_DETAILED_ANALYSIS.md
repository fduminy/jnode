# Detailed Analysis: org.jnode.vm.core ↔ rt Circular Dependency

**Date:** 2025-11-16

## Summary

- **Classes in org.jnode.vm.core that import from rt:** 113
- **Classes in rt that import from org.jnode.vm.core:** 41
- **Total classes involved in circular dependency:** 154

## Dependency Details

### Classes in org.jnode.vm.core importing from rt

(Sorted by number of dependencies, least dependencies first)

#### 1. org.jnode.vm.VmSystemSettings (1 import)
**File:** `core/src/template/org/jnode/vm/VmSystemSettings.java`

**Imports from rt:**
- `java.util.Properties`

#### 2. org.jnode.permission.JNodePermission (1 import)
**File:** `core/src/core/org/jnode/permission/JNodePermission.java`

**Imports from rt:**
- `java.security.BasicPermission`

#### 3. org.jnode.vm.BaseVmArchitecture (1 import)
**File:** `core/src/core/org/jnode/vm/BaseVmArchitecture.java`

**Imports from rt:**
- `java.nio.ByteOrder`

#### 4. org.jnode.vm.VmReflection (1 import)
**File:** `core/src/core/org/jnode/vm/VmReflection.java`

**Imports from rt:**
- `java.lang.reflect.InvocationTargetException`

#### 5. org.jnode.vm.ResourceManagerImpl (1 import)
**File:** `core/src/core/org/jnode/vm/ResourceManagerImpl.java`

**Imports from rt:**
- `javax.naming.NamingException`

#### 6. org.jnode.vm.VmProcessClassLoader (1 import)
**File:** `core/src/core/org/jnode/vm/VmProcessClassLoader.java`

**Imports from rt:**
- `java.util.HashSet`

#### 7. org.jnode.vm.performance.PerformanceCounters (1 import)
**File:** `core/src/core/org/jnode/vm/performance/PerformanceCounters.java`

**Imports from rt:**
- `java.util.Set`

#### 8. org.jnode.vm.bytecode.BytecodeViewer (1 import)
**File:** `core/src/core/org/jnode/vm/bytecode/BytecodeViewer.java`

**Imports from rt:**
- `java.io.PrintStream`

#### 9. org.jnode.vm.bytecode.DeadBlockFinder (1 import)
**File:** `core/src/core/org/jnode/vm/bytecode/DeadBlockFinder.java`

**Imports from rt:**
- `java.util.TreeMap`

#### 10. org.jnode.vm.bytecode.BytecodeParser (1 import)
**File:** `core/src/core/org/jnode/vm/bytecode/BytecodeParser.java`

**Imports from rt:**
- `java.nio.ByteBuffer`

#### 11. org.jnode.vm.bytecode.ControlFlowGraph (1 import)
**File:** `core/src/core/org/jnode/vm/bytecode/ControlFlowGraph.java`

**Imports from rt:**
- `java.util.Iterator`

#### 12. org.jnode.vm.scheduler.VmProcessor (1 import)
**File:** `core/src/core/org/jnode/vm/scheduler/VmProcessor.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 13. org.jnode.vm.facade.Vm (1 import)
**File:** `core/src/core/org/jnode/vm/facade/Vm.java`

**Imports from rt:**
- `java.util.List`

#### 14. org.jnode.vm.facade.VmProcessor (1 import)
**File:** `core/src/core/org/jnode/vm/facade/VmProcessor.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 15. org.jnode.vm.facade.HeapStatistics (1 import)
**File:** `core/src/core/org/jnode/vm/facade/HeapStatistics.java`

**Imports from rt:**
- `java.io.IOException`

#### 16. org.jnode.vm.facade.VmHeapManager (1 import)
**File:** `core/src/core/org/jnode/vm/facade/VmHeapManager.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 17. org.jnode.vm.facade.VmArchitecture (1 import)
**File:** `core/src/core/org/jnode/vm/facade/VmArchitecture.java`

**Imports from rt:**
- `java.nio.ByteOrder`

#### 18. org.jnode.vm.classmgr.VmStaticsAllocator (1 import)
**File:** `core/src/core/org/jnode/vm/classmgr/VmStaticsAllocator.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 19. org.jnode.vm.classmgr.Mangler (1 import)
**File:** `core/src/core/org/jnode/vm/classmgr/Mangler.java`

**Imports from rt:**
- `java.util.StringTokenizer`

#### 20. org.jnode.vm.classmgr.VmPrimitiveClass (1 import)
**File:** `core/src/core/org/jnode/vm/classmgr/VmPrimitiveClass.java`

**Imports from rt:**
- `java.security.ProtectionDomain`

#### 21. org.jnode.vm.classmgr.Signature (1 import)
**File:** `core/src/core/org/jnode/vm/classmgr/Signature.java`

**Imports from rt:**
- `java.util.ArrayList`

#### 22. org.jnode.vm.classmgr.VmNormalClass (1 import)
**File:** `core/src/core/org/jnode/vm/classmgr/VmNormalClass.java`

**Imports from rt:**
- `java.security.ProtectionDomain`

#### 23. org.jnode.vm.classmgr.VmStaticsIterator (1 import)
**File:** `core/src/core/org/jnode/vm/classmgr/VmStaticsIterator.java`

**Imports from rt:**
- `java.util.Iterator`

#### 24. org.jnode.vm.memmgr.VmHeapManager (1 import)
**File:** `core/src/core/org/jnode/vm/memmgr/VmHeapManager.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 25. org.jnode.vm.isolate.LinkLinkMessage (1 import)
**File:** `core/src/core/org/jnode/vm/isolate/LinkLinkMessage.java`

**Imports from rt:**
- `javax.isolate.Link`

#### 26. org.jnode.vm.isolate.IsolateStatusImpl (1 import)
**File:** `core/src/core/org/jnode/vm/isolate/IsolateStatusImpl.java`

**Imports from rt:**
- `javax.isolate.IsolateStatus`

#### 27. org.jnode.vm.isolate.LinkMessageImpl (1 import)
**File:** `core/src/core/org/jnode/vm/isolate/LinkMessageImpl.java`

**Imports from rt:**
- `javax.isolate.LinkMessage`

#### 28. org.jnode.vm.isolate.StatusLinkMessage (1 import)
**File:** `core/src/core/org/jnode/vm/isolate/StatusLinkMessage.java`

**Imports from rt:**
- `javax.isolate.IsolateStatus`

#### 29. org.jnode.vm.isolate.IsolateLinkMessage (1 import)
**File:** `core/src/core/org/jnode/vm/isolate/IsolateLinkMessage.java`

**Imports from rt:**
- `javax.isolate.Isolate`

#### 30. org.jnode.vm.compiler.NativeCodeCompiler (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/NativeCodeCompiler.java`

**Imports from rt:**
- `java.io.Writer`

#### 31. org.jnode.vm.compiler.CompiledExceptionHandler (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/CompiledExceptionHandler.java`

**Imports from rt:**
- `java.io.PrintStream`

#### 32. org.jnode.vm.x86.GDT (1 import)
**File:** `core/src/core/org/jnode/vm/x86/GDT.java`

**Imports from rt:**
- `java.io.PrintStream`

#### 33. org.jnode.vm.x86.PIC8259A (1 import)
**File:** `core/src/core/org/jnode/vm/x86/PIC8259A.java`

**Imports from rt:**
- `javax.naming.NameNotFoundException`

#### 34. org.jnode.vm.x86.VmX86Processor (1 import)
**File:** `core/src/core/org/jnode/vm/x86/VmX86Processor.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 35. org.jnode.vm.memmgr.def.DefaultHeapManager (1 import)
**File:** `core/src/core/org/jnode/vm/memmgr/def/DefaultHeapManager.java`

**Imports from rt:**
- `java.io.PrintWriter`

#### 36. org.jnode.vm.compiler.ir.IRBasicBlock (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/ir/IRBasicBlock.java`

**Imports from rt:**
- `java.util.List`

#### 37. org.jnode.vm.compiler.ir.SSAStack (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/ir/SSAStack.java`

**Imports from rt:**
- `java.util.List`

#### 38. org.jnode.vm.compiler.ir.PhiOperand (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/ir/PhiOperand.java`

**Imports from rt:**
- `java.util.List`

#### 39. org.jnode.vm.compiler.ir.quad.TableswitchQuad (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/ir/quad/TableswitchQuad.java`

**Imports from rt:**
- `java.util.Arrays`

#### 40. org.jnode.vm.compiler.ir.quad.LookupswitchQuad (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/ir/quad/LookupswitchQuad.java`

**Imports from rt:**
- `java.util.Arrays`

#### 41. org.jnode.vm.compiler.ir.quad.PhiAssignQuad (1 import)
**File:** `core/src/core/org/jnode/vm/compiler/ir/quad/PhiAssignQuad.java`

**Imports from rt:**
- `java.util.List`

#### 42. org.jnode.vm.x86.compiler.AbstractX86Compiler (1 import)
**File:** `core/src/core/org/jnode/vm/x86/compiler/AbstractX86Compiler.java`

**Imports from rt:**
- `java.io.Writer`

#### 43. org.jnode.vm.x86.compiler.l1b.ItemFactory (1 import)
**File:** `core/src/core/org/jnode/vm/x86/compiler/l1b/ItemFactory.java`

**Imports from rt:**
- `java.util.ArrayList`

#### 44. org.jnode.vm.x86.compiler.l1a.ItemFactory (1 import)
**File:** `core/src/core/org/jnode/vm/x86/compiler/l1a/ItemFactory.java`

**Imports from rt:**
- `java.util.ArrayList`

#### 45. org.jnode.vm.Unsafe (2 imports)
**File:** `core/src/core/org/jnode/vm/Unsafe.java`

**Imports from rt:**
- `java.io.PrintWriter`
- `java.io.StringWriter`

#### 46. org.jnode.vm.MemoryResourceImpl (2 imports)
**File:** `core/src/core/org/jnode/vm/MemoryResourceImpl.java`

**Imports from rt:**
- `java.nio.ByteBuffer`
- `java.nio.MemoryRawData`

#### 47. org.jnode.vm.VmAbstractClassLoader (2 imports)
**File:** `core/src/core/org/jnode/vm/VmAbstractClassLoader.java`

**Imports from rt:**
- `java.nio.ByteBuffer`
- `java.security.ProtectionDomain`

#### 48. org.jnode.vm.VmJavaClassLoader (2 imports)
**File:** `core/src/core/org/jnode/vm/VmJavaClassLoader.java`

**Imports from rt:**
- `java.io.Writer`
- `java.util.HashMap`

#### 49. org.jnode.vm.ResourceLoader (2 imports)
**File:** `core/src/core/org/jnode/vm/ResourceLoader.java`

**Imports from rt:**
- `java.net.URL`
- `java.nio.ByteBuffer`

#### 50. org.jnode.vm.bytecode.BasicBlockFinder (2 imports)
**File:** `core/src/core/org/jnode/vm/bytecode/BasicBlockFinder.java`

**Imports from rt:**
- `java.util.Comparator`
- `java.util.TreeMap`

#### 51. org.jnode.vm.bytecode.BasicBlock (2 imports)
**File:** `core/src/core/org/jnode/vm/bytecode/BasicBlock.java`

**Imports from rt:**
- `java.util.HashSet`
- `java.util.Set`

#### 52. org.jnode.vm.facade.VmUtils (2 imports)
**File:** `core/src/core/org/jnode/vm/facade/VmUtils.java`

**Imports from rt:**
- `gnu.java.lang.VMClassHelper`
- `java.io.PrintWriter`

#### 53. org.jnode.vm.objects.CounterGroup (2 imports)
**File:** `core/src/core/org/jnode/vm/objects/CounterGroup.java`

**Imports from rt:**
- `java.util.Map`
- `java.util.TreeMap`

#### 54. org.jnode.vm.classmgr.VmArrayClass (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmArrayClass.java`

**Imports from rt:**
- `java.security.ProtectionDomain`
- `java.util.HashSet`

#### 55. org.jnode.vm.classmgr.VmMethod (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmMethod.java`

**Imports from rt:**
- `java.lang.reflect.Member`
- `sun.reflect.ReflectionFactory`

#### 56. org.jnode.vm.classmgr.VmUTF8Convert (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmUTF8Convert.java`

**Imports from rt:**
- `java.io.UTFDataFormatException`
- `java.nio.ByteBuffer`

#### 57. org.jnode.vm.classmgr.VmAnnotatedElement (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmAnnotatedElement.java`

**Imports from rt:**
- `java.lang.annotation.Annotation`
- `java.lang.reflect.AnnotatedElement`

#### 58. org.jnode.vm.classmgr.VmField (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmField.java`

**Imports from rt:**
- `java.lang.reflect.Field`
- `sun.reflect.ReflectionFactory`

#### 59. org.jnode.vm.classmgr.VmInterfaceClass (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmInterfaceClass.java`

**Imports from rt:**
- `java.security.ProtectionDomain`
- `java.util.HashSet`

#### 60. org.jnode.vm.classmgr.VmStatics (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmStatics.java`

**Imports from rt:**
- `java.io.PrintWriter`
- `java.nio.ByteOrder`

#### 61. org.jnode.vm.classmgr.VmAddressMap (2 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmAddressMap.java`

**Imports from rt:**
- `java.io.PrintStream`
- `java.util.ArrayList`

#### 62. org.jnode.vm.isolate.IsolateThread (2 imports)
**File:** `core/src/core/org/jnode/vm/isolate/IsolateThread.java`

**Imports from rt:**
- `java.io.InputStream`
- `java.io.PrintStream`

#### 63. org.jnode.vm.x86.VmX86Architecture (2 imports)
**File:** `core/src/core/org/jnode/vm/x86/VmX86Architecture.java`

**Imports from rt:**
- `java.nio.ByteOrder`
- `java.util.HashMap`

#### 64. org.jnode.vm.memmgr.def.DefHeapStatistics (2 imports)
**File:** `core/src/core/org/jnode/vm/memmgr/def/DefHeapStatistics.java`

**Imports from rt:**
- `java.io.IOException`
- `java.util.TreeMap`

#### 65. org.jnode.vm.compiler.ir.IRGenerator (2 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/IRGenerator.java`

**Imports from rt:**
- `java.util.Iterator`
- `java.util.List`

#### 66. org.jnode.vm.compiler.ir.quad.AssignQuad (2 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/quad/AssignQuad.java`

**Imports from rt:**
- `java.util.Collection`
- `java.util.List`

#### 67. org.jnode.vm.compiler.ir.quad.Quad (2 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/quad/Quad.java`

**Imports from rt:**
- `java.util.Collection`
- `java.util.List`

#### 68. org.jnode.vm.x86.performance.X86PerformanceCounters (2 imports)
**File:** `core/src/core/org/jnode/vm/x86/performance/X86PerformanceCounters.java`

**Imports from rt:**
- `java.util.Collections`
- `java.util.Set`

#### 69. org.jnode.vm.x86.compiler.X86CompilerHelper (2 imports)
**File:** `core/src/core/org/jnode/vm/x86/compiler/X86CompilerHelper.java`

**Imports from rt:**
- `java.util.HashMap`
- `java.util.Map`

#### 70. org.jnode.vm.x86.compiler.l1b.X86BytecodeVisitor (2 imports)
**File:** `core/src/core/org/jnode/vm/x86/compiler/l1b/X86BytecodeVisitor.java`

**Imports from rt:**
- `java.util.HashMap`
- `java.util.Map`

#### 71. org.jnode.vm.x86.compiler.l2.X86Level2Compiler (2 imports)
**File:** `core/src/core/org/jnode/vm/x86/compiler/l2/X86Level2Compiler.java`

**Imports from rt:**
- `java.util.Collection`
- `java.util.List`

#### 72. org.jnode.assembler.NativeStream (3 imports)
**File:** `core/src/core/org/jnode/assembler/NativeStream.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.OutputStream`
- `java.util.Collection`

#### 73. org.jnode.vm.LoadCompileService (3 imports)
**File:** `core/src/core/org/jnode/vm/LoadCompileService.java`

**Imports from rt:**
- `java.nio.ByteBuffer`
- `java.security.ProtectionDomain`
- `java.util.ArrayList`

#### 74. org.jnode.vm.BootLogImpl (3 imports)
**File:** `core/src/core/org/jnode/vm/BootLogImpl.java`

**Imports from rt:**
- `java.io.PrintStream`
- `javax.naming.NameAlreadyBoundException`
- `javax.naming.NamingException`

#### 75. org.jnode.assembler.x86.X86Assembler (3 imports)
**File:** `core/src/core/org/jnode/assembler/x86/X86Assembler.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.OutputStream`
- `java.util.Collection`

#### 76. org.jnode.vm.classmgr.VmClassType (3 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmClassType.java`

**Imports from rt:**
- `java.security.ProtectionDomain`
- `java.util.ArrayList`
- `java.util.HashSet`

#### 77. org.jnode.vm.classmgr.TIBBuilder (3 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/TIBBuilder.java`

**Imports from rt:**
- `gnu.java.lang.VMClassHelper`
- `java.util.ArrayList`
- `java.util.HashMap`

#### 78. org.jnode.vm.classmgr.VmConstantPool (3 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmConstantPool.java`

**Imports from rt:**
- `java.lang.reflect.Field`
- `java.lang.reflect.Member`
- `sun.reflect.ConstantPool`

#### 79. org.jnode.vm.classmgr.VmClassLoader (3 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmClassLoader.java`

**Imports from rt:**
- `java.io.Writer`
- `java.nio.ByteBuffer`
- `java.security.ProtectionDomain`

#### 80. org.jnode.vm.x86.MPConfigTable (3 imports)
**File:** `core/src/core/org/jnode/vm/x86/MPConfigTable.java`

**Imports from rt:**
- `java.io.PrintStream`
- `java.util.ArrayList`
- `java.util.List`

#### 81. org.jnode.vm.x86.IOAPIC (3 imports)
**File:** `core/src/core/org/jnode/vm/x86/IOAPIC.java`

**Imports from rt:**
- `java.io.PrintStream`
- `java.util.ArrayList`
- `java.util.List`

#### 82. org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager (4 imports)
**File:** `core/src/mmtk-vm/org/jnode/vm/memmgr/mmtk/BaseMmtkHeapManager.java`

**Imports from rt:**
- `java.io.PrintWriter`
- `java.lang.reflect.Constructor`
- `java.lang.reflect.InvocationTargetException`
- `javax.naming.NameNotFoundException`

#### 83. org.jnode.vm.IOContext (4 imports)
**File:** `core/src/core/org/jnode/vm/IOContext.java`

**Imports from rt:**
- `java.io.InputStream`
- `java.io.PrintStream`
- `java.util.Map`
- `java.util.Properties`

#### 84. org.jnode.vm.VmIOContext (4 imports)
**File:** `core/src/core/org/jnode/vm/VmIOContext.java`

**Imports from rt:**
- `java.io.InputStream`
- `java.io.PrintStream`
- `java.util.Map`
- `java.util.Properties`

#### 85. org.jnode.vm.bytecode.BytecodeWriter (4 imports)
**File:** `core/src/core/org/jnode/vm/bytecode/BytecodeWriter.java`

**Imports from rt:**
- `java.nio.ByteBuffer`
- `java.util.ArrayList`
- `java.util.HashSet`
- `java.util.List`

#### 86. org.jnode.vm.objects.BootableHashMap (4 imports)
**File:** `core/src/core/org/jnode/vm/objects/BootableHashMap.java`

**Imports from rt:**
- `java.util.Collection`
- `java.util.HashMap`
- `java.util.Map`
- `java.util.Set`

#### 87. org.jnode.vm.classmgr.VmByteCode (4 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmByteCode.java`

**Imports from rt:**
- `java.nio.ByteBuffer`
- `java.util.Arrays`
- `java.util.Collections`
- `java.util.List`

#### 88. org.jnode.vm.isolate.IsolateThreadFactory (4 imports)
**File:** `core/src/core/org/jnode/vm/isolate/IsolateThreadFactory.java`

**Imports from rt:**
- `java.util.concurrent.ThreadFactory`
- `java.util.concurrent.ThreadFactory`
- `java.util.concurrent.atomic.AtomicInteger`
- `java.util.concurrent.atomic.AtomicInteger`

#### 89. org.jnode.vm.isolate.LinkMessageFactory (4 imports)
**File:** `core/src/core/org/jnode/vm/isolate/LinkMessageFactory.java`

**Imports from rt:**
- `java.net.ServerSocket`
- `java.net.Socket`
- `javax.isolate.Link`
- `javax.isolate.LinkMessage`

#### 90. org.jnode.vm.compiler.ir.IRBasicBlockFinder (4 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/IRBasicBlockFinder.java`

**Imports from rt:**
- `java.util.ArrayList`
- `java.util.Collections`
- `java.util.Comparator`
- `java.util.List`

#### 91. org.jnode.vm.x86.performance.DualMSRPerformanceCounters (4 imports)
**File:** `core/src/core/org/jnode/vm/x86/performance/DualMSRPerformanceCounters.java`

**Imports from rt:**
- `java.util.Arrays`
- `java.util.Collections`
- `java.util.Set`
- `java.util.TreeSet`

#### 92. org.jnode.vm.VmAccessControlContext (5 imports)
**File:** `core/src/core/org/jnode/vm/VmAccessControlContext.java`

**Imports from rt:**
- `java.security.AccessControlException`
- `java.security.Permission`
- `java.security.ProtectionDomain`
- `java.util.ArrayList`
- `java.util.List`

#### 93. org.jnode.vm.VmImpl (5 imports)
**File:** `core/src/core/org/jnode/vm/VmImpl.java`

**Imports from rt:**
- `java.lang.reflect.Constructor`
- `java.lang.reflect.InvocationTargetException`
- `java.util.List`
- `java.util.Map`
- `java.util.TreeMap`

#### 94. org.jnode.assembler.x86.X86TextAssembler (5 imports)
**File:** `core/src/core/org/jnode/assembler/x86/X86TextAssembler.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.OutputStream`
- `java.io.PrintWriter`
- `java.io.Writer`
- `java.util.Collection`

#### 95. org.jnode.vm.compiler.ir.LinearScanAllocator (5 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/LinearScanAllocator.java`

**Imports from rt:**
- `java.util.ArrayList`
- `java.util.Arrays`
- `java.util.Collections`
- `java.util.Comparator`
- `java.util.List`

#### 96. org.jnode.vm.VmAccessController (6 imports)
**File:** `core/src/core/org/jnode/vm/VmAccessController.java`

**Imports from rt:**
- `java.security.AccessControlException`
- `java.security.Permission`
- `java.security.PrivilegedAction`
- `java.security.PrivilegedActionException`
- `java.security.PrivilegedExceptionAction`
- `java.security.ProtectionDomain`

#### 97. org.jnode.vm.VmProcess (6 imports)
**File:** `core/src/core/org/jnode/vm/VmProcess.java`

**Imports from rt:**
- `java.io.InputStream`
- `java.io.OutputStream`
- `java.io.PrintStream`
- `java.lang.reflect.Constructor`
- `java.lang.reflect.InvocationTargetException`
- `java.lang.reflect.Method`

#### 98. org.jnode.assembler.x86.X86BinaryAssembler (6 imports)
**File:** `core/src/core/org/jnode/assembler/x86/X86BinaryAssembler.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.OutputStream`
- `java.util.Collection`
- `java.util.HashMap`
- `java.util.LinkedList`
- `java.util.Map`

#### 99. org.jnode.vm.classmgr.ClassDecoder (6 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/ClassDecoder.java`

**Imports from rt:**
- `java.io.UTFDataFormatException`
- `java.lang.annotation.Annotation`
- `java.nio.ByteBuffer`
- `java.security.ProtectionDomain`
- `sun.reflect.annotation.AnnotationParser`
- `sun.reflect.annotation.ExceptionProxy`

#### 100. org.jnode.vm.isolate.LinkImpl (6 imports)
**File:** `core/src/core/org/jnode/vm/isolate/LinkImpl.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.InterruptedIOException`
- `javax.isolate.ClosedLinkException`
- `javax.isolate.Isolate`
- `javax.isolate.Link`
- `javax.isolate.LinkMessage`

#### 101. org.jnode.vm.compiler.ir.NativeTest (6 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/NativeTest.java`

**Imports from rt:**
- `java.io.ByteArrayOutputStream`
- `java.io.File`
- `java.io.FileOutputStream`
- `java.io.IOException`
- `java.io.OutputStreamWriter`
- `java.net.MalformedURLException`

#### 102. org.jnode.vm.x86.performance.P4FamilyPerformanceCounters (6 imports)
**File:** `core/src/core/org/jnode/vm/x86/performance/P4FamilyPerformanceCounters.java`

**Imports from rt:**
- `java.util.Arrays`
- `java.util.Collections`
- `java.util.HashMap`
- `java.util.Map`
- `java.util.Set`
- `java.util.TreeSet`

#### 103. org.jnode.vm.DefaultNameSpace (7 imports)
**File:** `core/src/core/org/jnode/vm/DefaultNameSpace.java`

**Imports from rt:**
- `java.util.HashMap`
- `java.util.HashSet`
- `java.util.Map`
- `java.util.Set`
- `javax.naming.NameAlreadyBoundException`
- `javax.naming.NameNotFoundException`
- `javax.naming.NamingException`

#### 104. org.jnode.vm.objects.BootableArrayList (7 imports)
**File:** `core/src/core/org/jnode/vm/objects/BootableArrayList.java`

**Imports from rt:**
- `java.util.ArrayList`
- `java.util.Arrays`
- `java.util.Collection`
- `java.util.Iterator`
- `java.util.List`
- `java.util.ListIterator`
- `java.util.RandomAccess`

#### 105. org.jnode.vm.isolate.VmLink (7 imports)
**File:** `core/src/core/org/jnode/vm/isolate/VmLink.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.InterruptedIOException`
- `java.util.LinkedList`
- `java.util.Queue`
- `javax.isolate.ClosedLinkException`
- `javax.isolate.Link`
- `javax.isolate.LinkMessage`

#### 106. org.jnode.vm.compiler.ir.IRTest (7 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/IRTest.java`

**Imports from rt:**
- `java.io.File`
- `java.io.FileOutputStream`
- `java.io.IOException`
- `java.io.OutputStreamWriter`
- `java.net.MalformedURLException`
- `java.net.URL`
- `java.util.List`

#### 107. org.jnode.vm.compiler.ir.IRControlFlowGraph (7 imports)
**File:** `core/src/core/org/jnode/vm/compiler/ir/IRControlFlowGraph.java`

**Imports from rt:**
- `java.util.Collection`
- `java.util.Collections`
- `java.util.Comparator`
- `java.util.HashMap`
- `java.util.Iterator`
- `java.util.List`
- `java.util.Map`

#### 108. org.jnode.vm.classmgr.VmAnnotation (8 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmAnnotation.java`

**Imports from rt:**
- `java.lang.annotation.Annotation`
- `java.lang.annotation.Inherited`
- `java.lang.reflect.Array`
- `java.nio.ByteBuffer`
- `java.util.HashMap`
- `java.util.Map`
- `java.util.Set`
- `sun.reflect.annotation.AnnotationParser`

#### 109. org.jnode.vm.classmgr.VmType (8 imports)
**File:** `core/src/core/org/jnode/vm/classmgr/VmType.java`

**Imports from rt:**
- `gnu.java.lang.VMClassHelper`
- `java.io.Serializable`
- `java.io.Writer`
- `java.lang.reflect.InvocationTargetException`
- `java.security.ProtectionDomain`
- `java.util.Arrays`
- `java.util.Comparator`
- `java.util.HashSet`

#### 110. org.jnode.vm.isolate.VmStreamBindings (9 imports)
**File:** `core/src/core/org/jnode/vm/isolate/VmStreamBindings.java`

**Imports from rt:**
- `java.io.FileInputStream`
- `java.io.FileOutputStream`
- `java.io.FilterInputStream`
- `java.io.FilterOutputStream`
- `java.io.IOException`
- `java.io.InputStream`
- `java.io.OutputStream`
- `java.io.PrintStream`
- `java.net.Socket`

#### 111. org.jnode.vm.VmSystem (10 imports)
**File:** `core/src/core/org/jnode/vm/VmSystem.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.InputStream`
- `java.io.OutputStream`
- `java.io.PrintStream`
- `java.nio.ByteOrder`
- `java.util.Locale`
- `java.util.Properties`
- `javax.naming.NameNotFoundException`
- `sun.nio.ch.Interruptible`
- `sun.reflect.annotation.AnnotationType`

#### 112. org.jnode.vm.VmSystemClassLoader (15 imports)
**File:** `core/src/core/org/jnode/vm/VmSystemClassLoader.java`

**Imports from rt:**
- `java.io.ByteArrayInputStream`
- `java.io.ByteArrayOutputStream`
- `java.io.IOException`
- `java.io.InputStream`
- `java.io.Writer`
- `java.net.MalformedURLException`
- `java.net.URL`
- `java.nio.ByteBuffer`
- `java.util.ArrayList`
- `java.util.Arrays`
- `java.util.Collection`
- `java.util.HashSet`
- `java.util.List`
- `java.util.Map`
- `java.util.TreeMap`

#### 113. org.jnode.vm.isolate.VmIsolate (20 imports)
**File:** `core/src/core/org/jnode/vm/isolate/VmIsolate.java`

**Imports from rt:**
- `java.io.IOException`
- `java.io.InputStream`
- `java.io.PrintStream`
- `java.lang.reflect.Method`
- `java.net.URL`
- `java.net.URLClassLoader`
- `java.security.AccessController`
- `java.security.PrivilegedAction`
- `java.util.ArrayList`
- `java.util.Iterator`
- `java.util.LinkedList`
- `java.util.List`
- `java.util.Properties`
- `java.util.concurrent.ExecutorService`
- `java.util.concurrent.Executors`
- `javax.isolate.Isolate`
- `javax.isolate.IsolateStartupException`
- `javax.isolate.IsolateStatus`
- `javax.isolate.Link`
- `javax.naming.NameNotFoundException`

### Classes in rt importing from org.jnode.vm.core

(Sorted by number of dependencies, least dependencies first)

#### 1. javax.isolate.LinkMessage (1 import)
**File:** `core/src/classpath/ext/javax/isolate/LinkMessage.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.isolate.LinkMessageFactory`

#### 2. javax.isolate.Isolate (1 import)
**File:** `core/src/classpath/ext/javax/isolate/Isolate.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.isolate.VmIsolate`

#### 3. javax.isolate.StreamBindings (1 import)
**File:** `core/src/classpath/ext/javax/isolate/StreamBindings.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.isolate.VmStreamBindings`

#### 4. javax.isolate.Link (1 import)
**File:** `core/src/classpath/ext/javax/isolate/Link.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.isolate.VmLink`

#### 5. java.lang.VMSecurityManager (1 import)
**File:** `core/src/classpath/vm/java/lang/VMSecurityManager.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 6. java.security.NativeAccessControlContext (1 import)
**File:** `core/src/classpath/vm/java/security/NativeAccessControlContext.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmAccessControlContext`

#### 7. gnu.classpath.NativeVMStackWalker (1 import)
**File:** `core/src/classpath/vm/gnu/classpath/NativeVMStackWalker.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 8. gnu.classpath.NativeSystemProperties (1 import)
**File:** `core/src/classpath/vm/gnu/classpath/NativeSystemProperties.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 9. java.io.NativeObjectInputStream (1 import)
**File:** `core/src/openjdk/vm/java/io/NativeObjectInputStream.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 10. java.awt.NativeToolkit (1 import)
**File:** `core/src/openjdk/vm/java/awt/NativeToolkit.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 11. java.lang.NativeDouble (1 import)
**File:** `core/src/openjdk/vm/java/lang/NativeDouble.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmMagic`

#### 12. java.lang.NativeFloat (1 import)
**File:** `core/src/openjdk/vm/java/lang/NativeFloat.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmMagic`

#### 13. java.lang.NativeCompiler (1 import)
**File:** `core/src/openjdk/vm/java/lang/NativeCompiler.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.classmgr.VmType`

#### 14. java.lang.NativeSystem (1 import)
**File:** `core/src/openjdk/vm/java/lang/NativeSystem.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 15. java.util.NativeResourceBundle (1 import)
**File:** `core/src/openjdk/vm/java/util/NativeResourceBundle.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 16. java.lang.reflect.NativeProxy (1 import)
**File:** `core/src/openjdk/vm/java/lang/reflect/NativeProxy.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.classmgr.VmClassLoader`

#### 17. com.sun.management.NativeUnixOperatingSystem (1 import)
**File:** `core/src/openjdk/vm/com/sun/management/NativeUnixOperatingSystem.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`

#### 18. java.lang.NativeVMClassLoader (2 imports)
**File:** `core/src/classpath/vm/java/lang/NativeVMClassLoader.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.classmgr.VmType`

#### 19. java.lang.ThreadHelper (2 imports)
**File:** `core/src/classpath/vm/java/lang/ThreadHelper.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.permission.JNodePermission`
- `org.jnode.vm.scheduler.VmThread`

#### 20. java.io.NativeObjectStreamClass (2 imports)
**File:** `core/src/openjdk/vm/java/io/NativeObjectStreamClass.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmType`

#### 21. java.lang.NativeObject (2 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeObject.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.scheduler.MonitorManager`

#### 22. java.lang.NativeProcessEnvironment (2 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeProcessEnvironment.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmIOContext`
- `org.jnode.vm.VmSystem`

#### 23. java.lang.NativeRuntime (2 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeRuntime.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.facade.VmUtils`

#### 24. java.lang.NativeString (2 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeString.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.InternString`
- `org.jnode.vm.objects.BootableHashMap`

#### 25. java.lang.NativeShutdown (2 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeShutdown.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmExit`
- `org.jnode.vm.isolate.VmIsolate`

#### 26. sun.reflect.NativeReflection (2 imports)
**File:** `core/src/openjdk/vm/sun/reflect/NativeReflection.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.classmgr.VmType`

#### 27. sun.management.NativeVMManagementImpl (2 imports)
**File:** `core/src/openjdk/vm/sun/management/NativeVMManagementImpl.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.facade.VmUtils`
- `org.jnode.vm.isolate.VmIsolate`

#### 28. java.lang.VMSystem (3 imports)
**File:** `core/src/classpath/vm/java/lang/VMSystem.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmImpl`
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.facade.VmUtils`

#### 29. java.security.NativeAccessController (3 imports)
**File:** `core/src/classpath/vm/java/security/NativeAccessController.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.Unsafe`
- `org.jnode.vm.VmAccessControlContext`
- `org.jnode.vm.VmAccessController`

#### 30. sun.reflect.NativeNativeConstructorAccessorImpl (3 imports)
**File:** `core/src/openjdk/vm/sun/reflect/NativeNativeConstructorAccessorImpl.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmReflection`
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmType`

#### 31. sun.reflect.NativeNativeMethodAccessorImpl (3 imports)
**File:** `core/src/openjdk/vm/sun/reflect/NativeNativeMethodAccessorImpl.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmReflection`
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmType`

#### 32. sun.management.NativeThreadImpl (3 imports)
**File:** `core/src/openjdk/vm/sun/management/NativeThreadImpl.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.facade.VmThread`
- `org.jnode.vm.facade.VmThreadVisitor`
- `org.jnode.vm.facade.VmUtils`

#### 33. java.lang.VMRuntime (5 imports)
**File:** `core/src/classpath/vm/java/lang/VMRuntime.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmExit`
- `org.jnode.vm.VmProcess`
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.facade.VmUtils`
- `org.jnode.vm.isolate.VmIsolate`

#### 34. java.lang.NativeClassLoader (5 imports)
**File:** `core/src/classpath/vm/java/lang/NativeClassLoader.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.permission.JNodePermission`
- `org.jnode.vm.VmJavaClassLoader`
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.classmgr.VmClassLoader`
- `org.jnode.vm.classmgr.VmType`

#### 35. java.lang.NativeThread (5 imports)
**File:** `core/src/classpath/vm/java/lang/NativeThread.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.classmgr.VmIsolatedStatics`
- `org.jnode.vm.scheduler.MonitorManager`
- `org.jnode.vm.scheduler.VmProcessor`
- `org.jnode.vm.scheduler.VmThread`

#### 36. java.lang.reflect.NativeArray (5 imports)
**File:** `core/src/openjdk/vm/java/lang/reflect/NativeArray.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmImpl`
- `org.jnode.vm.classmgr.VmArrayClass`
- `org.jnode.vm.classmgr.VmClassLoader`
- `org.jnode.vm.classmgr.VmType`
- `org.jnode.vm.facade.VmUtils`

#### 37. java.lang.NativeThrowable (6 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeThrowable.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmStackFrame`
- `org.jnode.vm.classmgr.VmInstanceField`
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmType`
- `org.jnode.vm.scheduler.VmProcessor`
- `org.jnode.vm.scheduler.VmThread`

#### 38. gnu.classpath.jdwp.NativeVMVirtualMachine (7 imports)
**File:** `core/src/classpath/vm/gnu/classpath/jdwp/NativeVMVirtualMachine.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.classmgr.ClassDecoder`
- `org.jnode.vm.classmgr.VmIsolatedStatics`
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmStaticsIterator`
- `org.jnode.vm.classmgr.VmType`
- `org.jnode.vm.facade.VmUtils`
- `org.jnode.vm.isolate.VmIsolate`

#### 39. java.lang.NativeClass (7 imports)
**File:** `core/src/openjdk/vm/java/lang/NativeClass.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.SoftByteCodes`
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.classmgr.VmArrayClass`
- `org.jnode.vm.classmgr.VmClassLoader`
- `org.jnode.vm.classmgr.VmConstantPool`
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmType`

#### 40. sun.misc.NativeUnsafe (11 imports)
**File:** `core/src/openjdk/vm/sun/misc/NativeUnsafe.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.VmMagic`
- `org.jnode.vm.VmReflection`
- `org.jnode.vm.classmgr.VmArray`
- `org.jnode.vm.classmgr.VmClassLoader`
- `org.jnode.vm.classmgr.VmConstString`
- `org.jnode.vm.classmgr.VmField`
- `org.jnode.vm.classmgr.VmInstanceField`
- `org.jnode.vm.classmgr.VmStaticField`
- `org.jnode.vm.classmgr.VmType`
- `org.jnode.vm.facade.VmUtils`
- `org.jnode.vm.scheduler.VmProcessor`

#### 41. gnu.classpath.jdwp.JDIVirtualMachine (12 imports)
**File:** `core/src/classpath/vm/gnu/classpath/jdwp/JDIVirtualMachine.java`

**Imports from org.jnode.vm.core:**
- `org.jnode.vm.BaseVmArchitecture`
- `org.jnode.vm.VmSystem`
- `org.jnode.vm.VmSystemClassLoader`
- `org.jnode.vm.classmgr.ClassDecoder`
- `org.jnode.vm.classmgr.VmByteCode`
- `org.jnode.vm.classmgr.VmClassLoader`
- `org.jnode.vm.classmgr.VmIsolatedStatics`
- `org.jnode.vm.classmgr.VmMethod`
- `org.jnode.vm.classmgr.VmStaticsIterator`
- `org.jnode.vm.classmgr.VmType`
- `org.jnode.vm.facade.VmUtils`
- `org.jnode.vm.isolate.VmIsolate`

## Recommendations to Break the Circular Dependency

### Strategy 1: Extract Common API Interfaces

Create a new plugin `org.jnode.vm.api` that contains only interfaces:

1. **Identify common interfaces** used by both vm.core and rt
2. **Move interfaces** to the new API plugin
3. **Update dependencies:**
   - Both `org.jnode.vm.core` and `rt` depend on `org.jnode.vm.api`
   - Remove direct dependencies between vm.core and rt

### Strategy 2: Move Classes with Fewest Dependencies

Start with classes that have the least dependencies (listed above):

**From org.jnode.vm.core to rt (or new plugin):**
- `org.jnode.vm.VmSystemSettings` (1 dependency/dependencies)
- `org.jnode.permission.JNodePermission` (1 dependency/dependencies)
- `org.jnode.vm.BaseVmArchitecture` (1 dependency/dependencies)
- `org.jnode.vm.VmReflection` (1 dependency/dependencies)
- `org.jnode.vm.ResourceManagerImpl` (1 dependency/dependencies)

**From rt to org.jnode.vm.core (or new plugin):**
- `javax.isolate.LinkMessage` (1 dependency/dependencies)
- `javax.isolate.Isolate` (1 dependency/dependencies)
- `javax.isolate.StreamBindings` (1 dependency/dependencies)
- `javax.isolate.Link` (1 dependency/dependencies)
- `java.lang.VMSecurityManager` (1 dependency/dependencies)

### Strategy 3: Dependency Inversion

Apply the Dependency Inversion Principle:

1. **Define interfaces** for high-level dependencies
2. **Inject implementations** at runtime
3. **Use service locators** or dependency injection

### Strategy 4: Consolidate Plugins

If separation is not critical:

1. **Merge vm.core and rt** into a single plugin
2. **Document the merger** in plugin documentation
3. **Update all plugin descriptors** that reference these plugins

### Priority Actions

Based on the analysis, prioritize:

1. **Quick wins:** Classes with 1-2 dependencies
2. **Medium effort:** Classes with 3-5 dependencies
3. **Major refactoring:** Classes with >5 dependencies

## Next Steps

1. Review this detailed analysis with the development team
2. Choose a strategy based on project constraints and goals
3. Create a plan for incremental refactoring
4. Test each change thoroughly to ensure system stability
5. Document the architectural decisions
