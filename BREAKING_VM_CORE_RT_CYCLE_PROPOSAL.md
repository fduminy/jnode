# Proposal to Break org.jnode.vm.core ↔ rt Circular Dependency

**Date:** 2025-11-16  
**Status:** Proposal for Discussion

## Executive Summary

This document proposes a concrete approach to break the circular dependency between `org.jnode.vm.core` and `rt` plugins, involving 154 classes. The proposal focuses on the most practical and least risky approach: **moving specific classes between plugins and extracting API interfaces**.

## Background

Based on the detailed analysis in `VM_CORE_RT_CYCLE_DETAILED_ANALYSIS.md`:
- **113 classes** in org.jnode.vm.core import from rt
- **41 classes** in rt import from org.jnode.vm.core
- Most dependencies involve fundamental JVM classes (java.lang.*, java.util.*, etc.)

## Analysis of the Problem

### Root Cause

The circular dependency exists because:

1. **Native VM implementations in rt** need access to JNode-specific VM classes (VmType, VmSystem, VmIsolate, etc.)
2. **VM core classes** use standard Java APIs from rt (Collections, I/O, Security, etc.)
3. **javax.isolate API** (in rt) wraps JNode VM implementations

### Why This is Difficult

The dependency is **fundamental to the architecture**:
- The VM needs the standard library to operate
- The standard library needs VM internals for native implementations
- This is a classic bootstrap problem

## Recommended Approach

Given the analysis, I recommend **Strategy 1 with targeted moves** as the most practical solution:

### Phase 1: Move JNodePermission to rt (EASY - Quick Win)

**Rationale:**
- JNodePermission is a simple security permission class
- It only extends java.security.BasicPermission (already in rt)
- It's used by both rt and vm.core
- Moving it eliminates one circular dependency

**Changes:**
1. Move `core/src/core/org/jnode/permission/JNodePermission.java` → `core/src/classpath/vm/org/jnode/permission/JNodePermission.java`
2. Update `rt` plugin descriptor to export `org.jnode.permission.*`
3. Remove `org.jnode.permission` export from `org.jnode.vm.core` descriptor
4. Update any imports in both plugins

**Impact:** Minimal - simple class with no dependencies on vm.core internals

**Risk:** Low - it's already a shared API-like class

### Phase 2: Create org.jnode.vm.isolate.api Plugin (MEDIUM)

**Rationale:**
- The javax.isolate API (in rt) currently wraps VmIsolate classes (in vm.core)
- This creates a tight circular dependency
- Extracting isolate APIs breaks this cycle

**Changes:**

1. **Create new plugin:** `org.jnode.vm.isolate.api`
   
2. **Move isolate API interfaces:**
   - Create interface versions of: VmIsolate, VmLink, VmStreamBindings, LinkMessageFactory
   - Keep implementations in org.jnode.vm.core with interface implementations
   
3. **Update dependencies:**
   ```
   rt → org.jnode.vm.isolate.api (for javax.isolate wrapper implementations)
   org.jnode.vm.core → org.jnode.vm.isolate.api (implements the interfaces)
   org.jnode.vm.isolate.api → rt (uses java.* classes)
   ```

4. **Affected classes in rt (4 classes):**
   - javax.isolate.LinkMessage
   - javax.isolate.Isolate  
   - javax.isolate.StreamBindings
   - javax.isolate.Link

5. **Affected classes in vm.core (7 classes):**
   - org.jnode.vm.isolate.VmIsolate
   - org.jnode.vm.isolate.VmLink
   - org.jnode.vm.isolate.VmStreamBindings
   - org.jnode.vm.isolate.LinkMessageFactory
   - (plus related implementation classes)

**Impact:** Medium - requires interface extraction and implementation updates

**Risk:** Medium - affects isolate subsystem but well-isolated

### Phase 3: Analyze Native Implementation Pattern (COMPLEX - Later)

**Observation:**
Most rt → vm.core dependencies are in "Native*" classes:
- NativeObject, NativeSystem, NativeRuntime, NativeThread, etc.
- These are JNode's implementations of standard Java native methods

**Two Options:**

**Option A: Keep as-is (RECOMMENDED)**
- Accept that Native* implementations need VM access
- Document this as a design decision
- The circular dependency at this level is **by design** - the VM and runtime are tightly coupled

**Option B: Move Native* classes to vm.core**
- Move all Native* implementations from rt to org.jnode.vm.core
- Update rt descriptors to keep only pure Java classpath code
- **Risk:** High - breaks the separation between classpath and VM

**Recommendation:** Keep Native* classes in rt but accept the dependency as **intentional and necessary**. The VM and its native implementations are inherently coupled.

## Detailed Implementation Plan

### Step 1: JNodePermission Move (1-2 hours)

```bash
# 1. Move the file
mkdir -p core/src/classpath/vm/org/jnode/permission
git mv core/src/core/org/jnode/permission/JNodePermission.java \
       core/src/classpath/vm/org/jnode/permission/JNodePermission.java

# 2. Update rt descriptor (core/descriptors/org.classpath.core.xml)
# Add to <export> section:
#   <export name="org.jnode.permission.*"/>

# 3. Update vm.core descriptor (core/descriptors/org.jnode.vm.core.xml)
# Remove from <export> section:
#   <export name="org.jnode.permission.*"/>

# 4. Build and test
./build.sh clean build
```

### Step 2: Create org.jnode.vm.isolate.api (4-8 hours)

```bash
# 1. Create descriptor: core/descriptors/org.jnode.vm.isolate.api.xml
# 2. Create interface directory structure
# 3. Extract interfaces from implementation classes
# 4. Update implementations to implement interfaces
# 5. Update javax.isolate classes to use interfaces
# 6. Build and test extensively
```

### Step 3: Document Decision (1 hour)

Create architectural documentation explaining:
- Why Native* → vm.core dependency is acceptable
- The bootstrap architecture
- Guidelines for future development

## Expected Outcomes

After implementing Phases 1 and 2:

### Reduction in Circular Dependencies
- **Before:** 154 classes involved in cycle
- **After Phase 1:** 153 classes (-1)
- **After Phase 2:** ~145 classes (-9)
- **Remaining:** ~145 classes (mostly Native* implementations)

### Improved Architecture
- Clear API layer for isolate subsystem
- Better separation between public APIs and implementations
- Documented and accepted VM/runtime coupling

### Remaining Circular Dependencies
**Accepted as intentional:**
- Native* implementations need VM internals ✓ By design
- VM needs Java standard library ✓ Fundamental requirement

## Why Not Other Strategies?

### Why not Strategy 4 (Merge vm.core and rt)?
**Answer:** Would lose modularity and make the codebase harder to maintain. The separation between VM implementation and standard library is valuable.

### Why not move all Native* classes?
**Answer:** Native* classes implement the Java standard library's native methods. Moving them to vm.core would break the conceptual separation between "standard library" and "VM implementation."

### Why not create interfaces for everything?
**Answer:** Overhead and complexity. Most VM ↔ rt dependencies are fundamental and cannot be broken without major architectural changes.

## Risk Assessment

| Phase | Risk Level | Mitigation |
|-------|-----------|------------|
| Phase 1: JNodePermission | **LOW** | Simple move, well-isolated class |
| Phase 2: Isolate API | **MEDIUM** | Extensive testing, incremental rollout |
| Remaining dependencies | **LOW** | Accept as design decision, document |

## Testing Plan

For each phase:

1. **Unit Tests:** Verify moved/changed classes work correctly
2. **Integration Tests:** Test isolate functionality end-to-end  
3. **Build Tests:** Ensure clean build with no circular plugin dependencies
4. **Boot Tests:** Verify JNode boots correctly with qemu
5. **Regression Tests:** Run existing test suites

## Timeline

- **Phase 1:** 1 day (implementation + testing)
- **Phase 2:** 3-5 days (implementation + extensive testing)
- **Documentation:** 1 day
- **Total:** 5-7 days of focused development

## Alternatives Considered

1. **Do Nothing:** Not acceptable - circular dependencies cause build and maintenance issues
2. **Major Refactoring:** Too risky - would destabilize the entire VM
3. **Consolidation:** Loses architectural benefits of separation

## Conclusion

This proposal offers a **pragmatic, incremental approach** to breaking the circular dependency:

✅ **Quick wins** with minimal risk (Phase 1)  
✅ **Targeted improvement** where it matters (Phase 2)  
✅ **Accepts reality** for fundamental VM/runtime coupling  
✅ **Documents decisions** for future maintainers  

The key insight: **Not all circular dependencies should be eliminated**. Some represent fundamental architectural relationships that are better accepted and documented than fought against.

## Recommendation

**Proceed with Phase 1 immediately** as a low-risk quick win, then evaluate Phase 2 based on team feedback and priorities.

---

**Next Action:** Review this proposal with the development team and get approval to proceed with Phase 1.
