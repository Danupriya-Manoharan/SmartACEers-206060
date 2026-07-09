# Java Version Compatibility Fix

## Problem
Eclipse Compiler error: "Compliance level '11' is incompatible with target level '17'"

This occurs when your ACE Toolkit/Eclipse is configured to use Java 17, but the project is set to Java 11.

## Solution Applied

Updated all Java version configurations from **Java 11** to **Java 17** in the following files:

### 1. `.settings/org.eclipse.jdt.core.prefs`
Changed:
- `org.eclipse.jdt.core.compiler.codegen.targetPlatform=11` → `17`
- `org.eclipse.jdt.core.compiler.compliance=11` → `17`
- `org.eclipse.jdt.core.compiler.source=11` → `17`

### 2. `.classpath`
Changed:
- `JavaSE-11` → `JavaSE-17`

### 3. `META-INF/MANIFEST.MF`
Changed:
- `Bundle-RequiredExecutionEnvironment: JavaSE-11` → `JavaSE-17`

## Next Steps

### In Eclipse/ACE Toolkit:

1. **Refresh the Project**:
   - Right-click on project → **Refresh** (or press F5)

2. **Clean and Rebuild**:
   - **Project → Clean...**
   - Select **Clean all projects**
   - Click **Clean**

3. **Verify Java Version**:
   - Right-click project → **Properties**
   - Go to **Java Compiler**
   - Verify it shows **17** for compiler compliance level
   - Click **Apply and Close**

4. **Update Classpath** (if needed):
   - Right-click project → **Plug-in Tools → Update Classpath**

5. **Verify Build**:
   - Check **Problems** view for any remaining errors
   - All compilation errors should be resolved

## Verification

After these changes, the project should compile successfully with Java 17. You can verify by:

```bash
# Check if project builds without errors
# In Eclipse: Project → Build Project
```

## Why This Happened

Your ACE Toolkit is using Java 17 as the default JRE, but the project was originally configured for Java 11. The Eclipse compiler requires that the compliance level matches or is lower than the target level.

## Compatibility Notes

- **Java 17** is a Long-Term Support (LTS) release
- **IBM ACE 12.0.x** supports Java 11 and Java 17
- This change maintains backward compatibility with Java 11 code
- No code changes are required - only configuration updates

## If You Need Java 11 Instead

If your production environment requires Java 11, you should:

1. Configure Eclipse to use Java 11 JRE:
   - **Window → Preferences → Java → Installed JREs**
   - Add Java 11 JDK if not present
   - Set as default

2. Keep the project configuration as Java 11 (revert these changes)

3. Ensure ACE Toolkit uses Java 11 for compilation

## Additional Resources

- [Eclipse Java Compiler Settings](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.user/reference/preferences/java/compiler/ref-preferences-compiler.htm)
- [IBM ACE Java Support](https://www.ibm.com/docs/en/app-connect/12.0?topic=requirements-java-support)