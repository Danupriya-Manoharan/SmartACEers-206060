# Installing Plugin in the Same ACE Toolkit Instance

## Problem
Currently, "Run As → Eclipse Application" opens a **new** ACE Toolkit instance. You want the plugin to run in the **same** toolkit where you're developing it.

## Solution Overview
Instead of launching a runtime workspace, you'll install the plugin directly into your development ACE Toolkit using one of these methods:

---

## Method 1: Install via Dropins Folder (Recommended - Easiest)

This method installs the plugin permanently in your current ACE Toolkit.

### Step 1: Export the Plugin as JAR

1. **Right-click on the plugin project** in Package Explorer
2. Select **Export**
3. Choose **Plug-in Development → Deployable plug-ins and fragments**
4. Click **Next**

### Step 2: Configure Export Settings

1. **Available Plug-ins and Fragments**: 
   - Check `com.smartaceers.proofchecker`
   
2. **Destination**:
   - Select **Directory**
   - Browse to a temporary location (e.g., `/home/itzuser/Documents/plugin-export`)
   
3. **Options**:
   - ✅ Check "Package plug-ins as individual JAR archives"
   - ✅ Check "Qualify version number with build qualifier" (optional)
   
4. Click **Finish**

### Step 3: Copy to Dropins Folder

1. **Locate the exported JAR**:
   ```
   /home/itzuser/Documents/plugin-export/plugins/com.smartaceers.proofchecker_1.0.0.qualifier.jar
   ```

2. **Find your ACE Toolkit installation directory**:
   - Common locations:
     - Linux: `/opt/IBM/ACE/12.0/` or `/opt/ibm/ace-12/`
     - Windows: `C:\Program Files\IBM\ACE\12.0\`
     - Mac: `/Applications/IBM ACE Toolkit.app/`

3. **Copy the JAR to dropins folder**:
   ```bash
   # Example for Linux
   cp /home/itzuser/Documents/plugin-export/plugins/com.smartaceers.proofchecker_1.0.0.*.jar /opt/IBM/ACE/12.0/dropins/
   ```

### Step 4: Restart ACE Toolkit with Clean Flag

1. **Close ACE Toolkit completely**

2. **Restart with -clean flag**:
   ```bash
   # Linux
   /opt/IBM/ACE/12.0/ace -clean
   
   # Windows
   "C:\Program Files\IBM\ACE\12.0\ace.exe" -clean
   ```

   The `-clean` flag forces Eclipse to re-scan all plugins.

### Step 5: Verify Installation

1. **Open a message flow** (`.msgflow` file)
2. **Right-click on the file**
3. **Look for "Run Proofcheck"** in the context menu
4. **Or check the main menu**: Look for "ACE Proofcheck" menu

---

## Method 2: Install as Development Plugin (For Active Development)

This method is better if you're actively developing and want to test changes quickly.

### Step 1: Create a Run Configuration

1. **Right-click on the plugin project**
2. Select **Run As → Run Configurations...**
3. **Double-click "Eclipse Application"** to create a new configuration
4. Give it a name: "ACE Proofchecker - Self Host"

### Step 2: Configure to Use Current Workspace

1. In the **Main** tab:
   - **Workspace Data**: 
     - Select **${workspace_loc}** (this uses your current workspace)
     - OR uncheck "Clear workspace data before launching"

2. In the **Plug-ins** tab:
   - Select **"plug-ins selected below only"**
   - Click **Deselect All**
   - Check **only** `com.smartaceers.proofchecker`
   - Click **Add Required Plug-ins** (this adds dependencies)

3. Click **Apply** then **Run**

**Note**: This still launches a new Eclipse instance but uses your current workspace data.

---

## Method 3: Link Plugin Directly (Advanced - For Development)

This creates a symbolic link so changes are immediately available.

### Step 1: Create Links Folder

```bash
# In your ACE Toolkit installation
mkdir -p /opt/IBM/ACE/12.0/dropins/links
```

### Step 2: Create Link File

Create a file: `/opt/IBM/ACE/12.0/dropins/links/proofchecker.link`

Content:
```
path=/home/itzuser/Documents/ace-plugin-proofcheck/SmartACEers-206060/ace-proofchecker-plugin
```

### Step 3: Restart with Clean

```bash
/opt/IBM/ACE/12.0/ace -clean
```

**Advantage**: Any code changes you make are immediately available after restarting ACE Toolkit (no need to re-export).

---

## Method 4: Install from Update Site (Professional Approach)

### Step 1: Create Update Site Project

1. **File → New → Project**
2. Select **Plug-in Development → Update Site Project**
3. Name it: `ace-proofchecker-updatesite`
4. Click **Finish**

### Step 2: Add Plugin to Site

1. Open `site.xml` in the update site project
2. Click **Add Feature** (you'll need to create a feature first)
3. Or manually edit `site.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<site>
   <feature url="features/com.smartaceers.proofchecker.feature_1.0.0.jar" 
            id="com.smartaceers.proofchecker.feature" 
            version="1.0.0">
      <category name="ace-tools"/>
   </feature>
   <category-def name="ace-tools" label="ACE Development Tools"/>
</site>
```

### Step 3: Build Update Site

1. Click **Build All** in site.xml editor
2. This creates a local update site

### Step 4: Install in ACE Toolkit

1. **Help → Install New Software**
2. Click **Add...**
3. Click **Local...**
4. Browse to your update site project folder
5. Select the plugin and install
6. Restart ACE Toolkit

---

## Quick Development Workflow

For rapid development and testing in the same toolkit:

### Option A: Hot Deployment Script

Create a script `deploy-plugin.sh`:

```bash
#!/bin/bash

# Configuration
PROJECT_DIR="/home/itzuser/Documents/ace-plugin-proofcheck/SmartACEers-206060/ace-proofchecker-plugin"
ACE_DROPINS="/opt/IBM/ACE/12.0/dropins"
EXPORT_DIR="/tmp/plugin-export"

# Export plugin
echo "Exporting plugin..."
# You'll need to do this manually in Eclipse or use Maven/Tycho

# Copy to dropins
echo "Copying to dropins..."
cp $EXPORT_DIR/plugins/com.smartaceers.proofchecker_*.jar $ACE_DROPINS/

echo "Done! Restart ACE Toolkit with: ace -clean"
```

### Option B: Development Mode

1. Keep the plugin project open in ACE Toolkit
2. Make code changes
3. Export to dropins (Method 1)
4. Restart ACE Toolkit
5. Test changes

---

## Troubleshooting

### Plugin Not Appearing After Install

**Check 1: Verify JAR is in dropins**
```bash
ls -la /opt/IBM/ACE/12.0/dropins/
```

**Check 2: Check Eclipse Configuration**
```bash
# Look for your plugin
cat /opt/IBM/ACE/12.0/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info | grep smartaceers
```

**Check 3: View Error Log**
1. In ACE Toolkit: **Window → Show View → Error Log**
2. Look for plugin loading errors

**Check 4: Restart with -clean and -console**
```bash
/opt/IBM/ACE/12.0/ace -clean -console
```
Then type `ss` to see bundle status. Look for your plugin.

### Plugin Loads But Menu Doesn't Appear

**Check 1: Verify plugin.xml**
- Ensure `plugin.xml` has correct menu contributions
- Check the `locationURI` matches Eclipse's menu structure

**Check 2: Check File Extension**
- Plugin only shows for `.msgflow` files
- Verify you're right-clicking on a message flow file

**Check 3: Clear Workspace Cache**
```bash
rm -rf ~/.eclipse/org.eclipse.platform_*/configuration/org.eclipse.ui.workbench/workbench.xml
```

### Changes Not Reflected

**Solution**: Always restart with `-clean` flag after updating plugin:
```bash
/opt/IBM/ACE/12.0/ace -clean
```

---

## Comparison of Methods

| Method | Pros | Cons | Best For |
|--------|------|------|----------|
| **Dropins** | Simple, permanent | Need to re-export for changes | Production use |
| **Self-Host Run Config** | Quick testing | Still separate instance | Development |
| **Link** | Changes immediate | Requires restart | Active development |
| **Update Site** | Professional, versioned | Complex setup | Team distribution |

---

## Recommended Workflow

### For Development:
1. Use **Method 3 (Link)** during active development
2. Make changes → Save → Restart ACE Toolkit
3. Test immediately in same workspace

### For Testing:
1. Use **Method 1 (Dropins)** for stable testing
2. Export → Copy → Restart with -clean
3. Test with real message flows

### For Distribution:
1. Use **Method 4 (Update Site)** for team deployment
2. Create versioned releases
3. Team installs via Help → Install New Software

---

## Summary

**To run the plugin in the SAME toolkit where you develop:**

1. **Export the plugin** as a JAR (File → Export → Deployable plug-ins)
2. **Copy to dropins folder** in your ACE Toolkit installation
3. **Restart ACE Toolkit** with `-clean` flag
4. **Plugin is now active** in your development environment

No more separate Eclipse instances! The plugin runs directly in your working ACE Toolkit.