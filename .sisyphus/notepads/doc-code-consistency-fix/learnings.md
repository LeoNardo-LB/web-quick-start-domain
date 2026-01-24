# Learnings - Doc Code Consistency Fix

## Configuration Class Naming Convention

### Date: 2026-01-24

### Learning: AdapterListenerConfig Rename

**Task**: Rename `AdapterListenerConfig.java` → `AdapterListenerConfigure.java`

**Pattern** (from _docs/specification/业务代码编写规范.md Section 2.1.1):
- 配置类必须按聚合根命名：`{业务}Aggr` → `{业务}Configure`
- 必须用`Configure`后缀（不是`Config`）

**Execution**:
1. Used `git mv` to preserve history:
   ```bash
   cd start/src/main/java/org/smm/archetype/config
   git mv AdapterListenerConfig.java AdapterListenerConfigure.java
   ```

2. Updated class name inside file:
   ```java
   // Before
   public class AdapterListenerConfig

   // After
   public class AdapterListenerConfigure
   ```

**Key Points**:
- `git mv` preserves file history (better than cp + rm)
- Class name must match file name (Java requirement)
- Always stage both rename and class update together

**Files Changed**:
- start/src/main/java/org/smm/archetype/config/AdapterListenerConfigure.java
  - Renamed from AdapterListenerConfig.java
  - Class name updated from AdapterListenerConfig to AdapterListenerConfigure
