# ACE Proofchecker Plugin - Setup Guide

This guide will walk you through setting up the development environment and building the ACE Proofchecker plugin.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Development Environment Setup](#development-environment-setup)
3. [Building the Plugin](#building-the-plugin)
4. [Testing the Plugin](#testing-the-plugin)
5. [Deploying the Plugin](#deploying-the-plugin)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

1. **IBM ACE Toolkit v12.x or higher**
   - Download from IBM website
   - Install with default options
   - Note the installation directory

2. **Java Development Kit (JDK) 11 or higher**
   - Download from Oracle or use OpenJDK
   - Set JAVA_HOME environment variable
   - Verify: `java -version`

3. **Eclipse IDE for RCP and RAP Developers** (if not using ACE Toolkit's Eclipse)
   - Download from eclipse.org
   - Version 2021-06 or later recommended

### Optional Software

- **Maven** (for command-line builds)
- **Git** (for version control)

## Development Environment Setup

### Step 1: Import the Plugin Project

1. **Launch ACE Toolkit or Eclipse**

2. **Import the Project**:
   ```
   File → Import → General → Existing Projects into Workspace
   ```

3. **Select Root Directory**:
   - Browse to: `SmartACEers-206060/ace-proofchecker-plugin`
   - Ensure "Copy projects into workspace" is **unchecked**
   - Click `Finish`

### Step 2: Configure Target Platform

The target platform tells Eclipse which plugins are available for your plugin to use.

1. **Open Target Platform Preferences**:
   ```
   Window → Preferences → Plug-in Development → Target Platform
   ```

2. **Add ACE Toolkit as Target**:
   - Click `Add...`
   - Select `Nothing: Start with an empty target definition`
   - Click `Next`
   
3. **Add Installation Location**:
   - Click `Add...`
   - Select `Installation`
   - Click `Next`
   - Browse to your ACE Toolkit installation directory
   - Click `Finish`

4. **Set as Active Target**:
   - Check the box next to your new target platform
   - Click `Apply and Close`

### Step 3: Resolve Dependencies

1. **Check for Errors**:
   - Open `Problems` view: `Window → Show View → Problems`
   - Look for any compilation errors

2. **If Dependencies are Missing**:
   - Right-click on project → `Plug-in Tools → Update Classpath`
   - Clean and rebuild: `Project → Clean → Clean all projects`

## Building the Plugin

### Method 1: Export as Deployable Plugin (Recommended)

1. **Right-click on the project** → `Export`

2. **Select Export Wizard**:
   ```
   Plug-in Development → Deployable plug-ins and fragments
   ```

3. **Configure Export**:
   - **Available Plug-ins and Fragments**: Check `com.smartaceers.proofchecker`
   - **Destination**: 
     - Select `Directory`
     - Browse to a location (e.g., `C:\ACE-Plugins\export`)
   - **Options**:
     - ✅ Package plug-ins as individual JAR archives
     - ✅ Generate source bundles (optional)

4. **Click Finish**

5. **Output Location**:
   ```
   <export-directory>/plugins/com.smartaceers.proofchecker_1.0.0.qualifier.jar
   ```

### Method 2: Run as Eclipse Application (For Testing)

1. **Right-click on the project** → `Run As → Eclipse Application`

2. **A new Eclipse instance will launch** with your plugin installed

3. **Test your plugin** in this runtime workspace

4. **To stop**: Close the runtime Eclipse instance

### Method 3: Build with Maven (If Configured)

```bash
cd ace-proofchecker-plugin
mvn clean package

# Output: target/com.smartaceers.proofchecker-1.0.0-SNAPSHOT.jar
```

## Testing the Plugin

### Running Unit Tests

#### In Eclipse:

1. **Navigate to test class**:
   ```
   test/com/smartaceers/proofchecker/validators/MQTransactionValidatorTest.java
   ```

2. **Right-click on the test class** → `Run As → JUnit Test`

3. **View Results** in the JUnit view

#### Run All Tests:

1. **Right-click on the `test` folder** → `Run As → JUnit Test`

### Testing in Runtime Eclipse

1. **Launch Runtime Eclipse**:
   ```
   Right-click project → Run As → Eclipse Application
   ```

2. **Create a Test Workspace**:
   - The runtime Eclipse will ask for a workspace location
   - Choose a temporary directory

3. **Import a Test Message Flow**:
   - Create or import a `.msgflow` file
   - Ensure it has some validation issues (e.g., MQ Input with transaction mode = No)

4. **Run Proofcheck**:
   - Right-click on the `.msgflow` file
   - Select `Run Proofcheck`
   - Check the Problems view for results

5. **Verify**:
   - ✅ Context menu appears
   - ✅ Validation runs without errors
   - ✅ Results appear in Problems view
   - ✅ Double-clicking a problem navigates to the issue

## Deploying the Plugin

### Option 1: Install in ACE Toolkit (Dropins)

1. **Locate the Plugin JAR**:
   ```
   <export-directory>/plugins/com.smartaceers.proofchecker_1.0.0.qualifier.jar
   ```

2. **Copy to ACE Toolkit**:
   ```
   Copy JAR to: <ACE_TOOLKIT_INSTALL>/dropins/
   ```

3. **Restart ACE Toolkit**

4. **Verify Installation**:
   - Open a `.msgflow` file
   - Right-click → Should see `Run Proofcheck` option

### Option 2: Install via Update Site

1. **Create Update Site** (Advanced):
   ```
   File → Export → Plug-in Development → Deployable features
   ```

2. **Install in ACE Toolkit**:
   ```
   Help → Install New Software → Add → Archive
   Select the update site ZIP
   Follow installation wizard
   ```

### Option 3: P2 Repository (For Team Distribution)

1. **Create P2 Repository**:
   - Use Tycho Maven plugin
   - Or Eclipse PDE Build

2. **Host on Internal Server**

3. **Team Members Install**:
   ```
   Help → Install New Software
   Add repository URL
   Install plugin
   ```

## Troubleshooting

### Issue: "Plugin not found in context menu"

**Symptoms**: Right-click on `.msgflow` file doesn't show "Run Proofcheck"

**Solutions**:
1. Verify plugin is in `dropins` folder
2. Restart ACE Toolkit with `-clean` flag:
   ```
   <ACE_TOOLKIT>/ace.exe -clean
   ```
3. Check Eclipse Error Log:
   ```
   Window → Show View → Error Log
   ```
4. Verify plugin.xml is correct

### Issue: "ClassNotFoundException" or "NoClassDefFoundError"

**Symptoms**: Plugin fails to load with class not found errors

**Solutions**:
1. Check MANIFEST.MF has correct dependencies
2. Verify target platform includes required bundles
3. Clean and rebuild project
4. Check bundle is properly exported in build.properties

### Issue: "Validation doesn't run"

**Symptoms**: Menu appears but nothing happens when clicked

**Solutions**:
1. Check Eclipse Error Log for exceptions
2. Verify message flow file is valid XML
3. Add debug logging to ProofcheckHandler
4. Test with a simple message flow first

### Issue: "Build errors in Eclipse"

**Symptoms**: Red X marks on project, compilation errors

**Solutions**:
1. Verify Java 11+ is configured:
   ```
   Window → Preferences → Java → Installed JREs
   ```
2. Check target platform is set correctly
3. Update classpath:
   ```
   Right-click project → Plug-in Tools → Update Classpath
   ```
4. Clean and rebuild:
   ```
   Project → Clean → Clean all projects
   ```

### Issue: "Tests fail to run"

**Symptoms**: JUnit tests don't execute or fail

**Solutions**:
1. Verify JUnit 5 is in classpath
2. Check test dependencies in MANIFEST.MF
3. Run tests as "JUnit Plug-in Test" instead of "JUnit Test"
4. Verify test data files exist

## Development Tips

### Debugging the Plugin

1. **Set Breakpoints** in your code

2. **Launch in Debug Mode**:
   ```
   Right-click project → Debug As → Eclipse Application
   ```

3. **Debug Perspective** will open automatically

4. **Step through code** as validation runs

### Logging

Add logging to help diagnose issues:

```java
import java.util.logging.Logger;

private static final Logger LOGGER = Logger.getLogger(MyClass.class.getName());

LOGGER.info("Validation started");
LOGGER.warning("Issue detected: " + issue);
LOGGER.severe("Error occurred", exception);
```

View logs in:
- Eclipse Error Log view
- Console output
- ACE Toolkit log files

### Hot Code Replace

When debugging:
1. Make code changes
2. Save the file
3. Eclipse will hot-swap the code (if possible)
4. Continue debugging without restart

## Next Steps

After successful setup:

1. **Read the Code**: Familiarize yourself with the codebase
2. **Run Tests**: Ensure all tests pass
3. **Add a Validator**: Try creating a new validation rule
4. **Contribute**: Submit improvements via pull requests

## Additional Resources

- [Eclipse Plugin Development Guide](https://www.eclipse.org/pde/)
- [IBM ACE Documentation](https://www.ibm.com/docs/en/app-connect/)
- [Project README](README.md)
- [MVP Plan](../MVP-PLAN.md)

## Support

For setup issues:
- Check GitHub Issues
- Review Eclipse Error Log
- Contact: [your-email@example.com]