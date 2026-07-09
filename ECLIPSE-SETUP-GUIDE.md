# Eclipse Setup Guide for ACE Proofchecker Plugin

## Problem
The error "The import org.eclipse cannot be resolved" occurs because Eclipse needs to be configured as a Plugin Development Environment with a Target Platform.

## Solution: Step-by-Step Setup

### Step 1: Verify Eclipse Installation
You need **Eclipse IDE for RCP and RAP Developers** (or Eclipse with PDE installed).

1. Open Eclipse
2. Go to **Help → About Eclipse IDE**
3. Check if you see "Eclipse IDE for RCP and RAP Developers" or similar

**If you have standard Eclipse IDE:**
- Go to **Help → Install New Software**
- Select your Eclipse release site (e.g., "2023-12 - https://download.eclipse.org/releases/2023-12")
- Expand "General Purpose Tools"
- Check "Eclipse Plug-in Development Environment"
- Click Next → Finish → Restart Eclipse

### Step 2: Import the Project

1. **File → Import**
2. Select **General → Existing Projects into Workspace**
3. Click **Next**
4. **Select root directory:** Browse to `/home/UGH8FQA/Documents/myfolder/SmartACEers-206060/ace-proofchecker-plugin`
5. Make sure the project is checked
6. Click **Finish**

### Step 3: Configure Target Platform

This is the **CRITICAL STEP** that provides the org.eclipse.* libraries.

#### Option A: Use Running Platform (Easiest)
1. **Window → Preferences**
2. Navigate to **Plug-in Development → Target Platform**
3. You should see "Running Platform" in the list
4. **Check the box** next to "Running Platform"
5. Click **Apply and Close**

#### Option B: Create New Target Platform (If Running Platform doesn't exist)
1. **Window → Preferences**
2. Navigate to **Plug-in Development → Target Platform**
3. Click **Add...**
4. Select **Default** → Click **Next**
5. Give it a name like "Eclipse SDK"
6. Click **Finish**
7. **Check the box** next to your new target platform
8. Click **Apply and Close**

### Step 4: Clean and Rebuild

1. **Project → Clean...**
2. Select **Clean all projects**
3. Click **Clean**

### Step 5: Verify the Fix

1. Open any Java file with org.eclipse imports (e.g., `Activator.java`)
2. The imports should now be resolved (no red underlines)
3. In Package Explorer, you should see "Plug-in Dependencies" folder with Eclipse libraries

## Troubleshooting

### Still seeing import errors?

**Check 1: Is it a Plugin Project?**
- In Package Explorer, the project icon should have a small puzzle piece overlay
- If not, right-click project → Configure → Convert to Plug-in Project

**Check 2: Verify MANIFEST.MF**
- Open `META-INF/MANIFEST.MF`
- Go to the "Dependencies" tab
- Verify these are listed:
  - org.eclipse.ui
  - org.eclipse.core.runtime
  - org.eclipse.core.resources
  - org.eclipse.jface
  - org.eclipse.ui.ide
  - org.eclipse.ui.workbench

**Check 3: Java Version**
- Right-click project → Properties → Java Compiler
- Should be set to Java 11 (JavaSE-11)

**Check 4: Refresh Target Platform**
- Window → Preferences → Plug-in Development → Target Platform
- Select your target platform → Click **Edit**
- Click **Finish** → **Apply and Close**

### Alternative: Quick Fix in Eclipse

1. Open a file with import errors
2. Click on the red error line
3. Press **Ctrl+1** (Quick Fix)
4. If Eclipse suggests "Configure build path", it means PDE is not properly set up
5. Go back to Step 1 and ensure PDE is installed

## Expected Result

After completing these steps:
- All `org.eclipse.*` imports should resolve
- No red error markers in Java files
- Project builds without errors
- You can see "Plug-in Dependencies" in Package Explorer with all Eclipse libraries

## Need More Help?

If imports still don't resolve after following all steps:
1. Check Eclipse error log: Window → Show View → Error Log
2. Verify Eclipse version supports Java 11
3. Try creating a new workspace and re-importing the project