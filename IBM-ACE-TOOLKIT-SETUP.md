# IBM ACE Toolkit Setup Guide for ACE Proofchecker Plugin

## Important: IBM ACE Toolkit is Eclipse-Based

**YES, this works with IBM ACE Toolkit!** The IBM App Connect Enterprise Toolkit is built on Eclipse, so the same principles apply.

## Setup for IBM ACE Toolkit

### Step 1: Verify Your IBM ACE Toolkit Version

IBM ACE Toolkit is based on Eclipse and includes the Plugin Development Environment (PDE) capabilities.

1. Open IBM ACE Toolkit
2. Go to **Help → About IBM App Connect Enterprise Toolkit**
3. Note the version (e.g., ACE 12.0)

### Step 2: Import the Plugin Project

1. **File → Import**
2. Select **General → Existing Projects into Workspace**
3. Click **Next**
4. **Select root directory:** Browse to the `ace-proofchecker-plugin` folder
5. Ensure the project is checked
6. Click **Finish**

### Step 3: Configure Target Platform (Critical Step)

Since IBM ACE Toolkit is Eclipse-based, it has a target platform:

1. **Window → Preferences**
2. Navigate to **Plug-in Development → Target Platform**
3. You should see the IBM ACE Toolkit's running platform
4. **Ensure it is checked** (there should be a checkmark)
5. Click **Apply and Close**

**If you don't see "Plug-in Development" in Preferences:**
- IBM ACE Toolkit should have PDE by default
- If missing, you may need to install it from the Eclipse Marketplace

### Step 4: Verify Plugin Dependencies

1. Open `META-INF/MANIFEST.MF` in the project
2. Click the **Dependencies** tab
3. Verify these bundles are listed:
   - org.eclipse.ui
   - org.eclipse.core.runtime
   - org.eclipse.core.resources
   - org.eclipse.jface
   - org.eclipse.ui.ide
   - org.eclipse.ui.workbench

These are standard Eclipse bundles that IBM ACE Toolkit includes.

### Step 5: Clean and Build

1. **Project → Clean...**
2. Select **Clean all projects**
3. Click **Clean**
4. The project should rebuild automatically

### Step 6: Verify the Setup

1. Open `src/com/smartaceers/proofchecker/Activator.java`
2. The `import org.eclipse.*` statements should no longer show errors
3. In Package Explorer, expand the project
4. You should see **Plug-in Dependencies** folder with Eclipse libraries

## IBM ACE Toolkit Specific Considerations

### Integration with ACE Workspace

The plugin is designed to work with IBM ACE message flows:

- **Message Flow Files:** `.msgflow` files in your ACE workspace
- **Validation:** The plugin validates message flow XML structure
- **Problem Markers:** Issues appear in the Problems view (standard Eclipse)

### Running the Plugin in IBM ACE Toolkit

Once the import errors are resolved:

1. **Right-click the plugin project**
2. Select **Run As → Eclipse Application**
3. A new IBM ACE Toolkit instance will launch with your plugin installed
4. Test the plugin on actual `.msgflow` files

### Installing the Plugin Permanently

After development and testing:

1. **File → Export**
2. Select **Plug-in Development → Deployable plug-ins and fragments**
3. Select your plugin
4. Choose destination (e.g., `dropins` folder in ACE Toolkit installation)
5. Restart IBM ACE Toolkit

## Troubleshooting in IBM ACE Toolkit

### Issue: "Plug-in Development" not in Preferences

**Solution:**
1. **Help → Install New Software**
2. Work with: Select the Eclipse update site bundled with ACE Toolkit
3. Expand **General Purpose Tools**
4. Check **Eclipse Plug-in Development Environment**
5. Install and restart

### Issue: Target Platform shows errors

**Solution:**
1. Window → Preferences → Plug-in Development → Target Platform
2. Select the active platform → Click **Edit**
3. Click **Reload** to refresh the platform
4. Click **Finish** → **Apply and Close**

### Issue: Project shows as Java project, not plugin

**Solution:**
1. Right-click project → **Configure → Convert to Plug-in Project**
2. If option not available, ensure `.project` file has PDE nature (already configured)

### Issue: Cannot find message flow files

The plugin expects `.msgflow` files which are XML-based. Ensure:
- You're testing in an ACE workspace with message flows
- The message flow files are in the workspace
- The plugin handler is triggered on `.msgflow` file selection

## Expected Behavior in IBM ACE Toolkit

Once properly configured:

1. **No import errors** - All org.eclipse.* imports resolve
2. **Plugin icon** - Project shows with puzzle piece icon in Package Explorer
3. **Can run** - Right-click → Run As → Eclipse Application works
4. **Validates flows** - Plugin can parse and validate .msgflow files
5. **Shows problems** - Validation findings appear in Problems view

## Testing the Plugin

1. Create or open a message flow in IBM ACE Toolkit
2. Right-click the `.msgflow` file
3. Look for "Run Proofcheck" in the context menu (defined in plugin.xml)
4. Run the validation
5. Check the Problems view for validation results

## IBM ACE Toolkit Versions

This plugin should work with:
- IBM ACE v11.x (Eclipse 4.6+)
- IBM ACE v12.x (Eclipse 4.19+)
- IBM Integration Bus v10.x (Eclipse 4.4+)

All these versions are Eclipse-based and support plugin development.

## Additional Resources

- IBM ACE Toolkit is essentially Eclipse with ACE-specific plugins
- Standard Eclipse plugin development practices apply
- The plugin extends ACE Toolkit's functionality for message flow validation
- Uses Eclipse's marker framework for displaying validation results

## Summary

**YES, this plugin works with IBM ACE Toolkit!** Just follow the setup steps above. The key is ensuring the Target Platform is configured, which provides all the org.eclipse.* libraries needed by the plugin.