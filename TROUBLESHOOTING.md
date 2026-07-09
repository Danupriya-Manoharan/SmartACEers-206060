# ACE Proofchecker - Troubleshooting Guide

## Common Issues and Solutions

### Issue: "Please select a message flow file (.msgflow)" Error

**Symptom:** When you right-click on a .msgflow file and select "Run Proofcheck", you get an error message saying "Please select a message flow file (.msgflow)".

**Possible Causes:**

1. **File not properly selected**
   - **Solution:** Make sure you click on the .msgflow file ONCE to select it (it should be highlighted), then right-click on the selected file.

2. **ACE Toolkit custom file adapter**
   - **Solution:** The updated ProofcheckHandler now includes adapter support. Rebuild and reinstall the plugin:
     ```bash
     # In Eclipse PDE
     1. Clean the project (Project > Clean)
     2. Export the plugin (File > Export > Plug-in Development > Deployable plug-ins)
     3. Copy to ACE Toolkit dropins folder
     4. Restart ACE Toolkit with -clean flag
     ```

3. **Plugin not properly installed**
   - **Solution:** Verify the plugin is in the correct location:
     - Windows: `C:\Program Files\IBM\ACE\<version>\tools\dropins\`
     - Linux: `/opt/IBM/ACE/<version>/tools/dropins/`
   - Restart ACE Toolkit with the `-clean` flag:
     ```bash
     ace toolkit -clean
     ```

### Issue: Plugin Menu Not Appearing

**Symptom:** Right-clicking on .msgflow files doesn't show the "Run Proofcheck" option.

**Solutions:**

1. **Verify plugin installation:**
   - Check that the plugin JAR is in the `dropins` folder
   - Check Eclipse logs: `<workspace>/.metadata/.log`

2. **Check plugin.xml configuration:**
   - Ensure the menu contribution is properly configured
   - Verify the file extension filter is set to `*.msgflow`

3. **Restart with clean:**
   ```bash
   ace toolkit -clean
   ```

### Issue: Validation Runs But No Results

**Symptom:** The validation completes but no problems appear in the Problems view.

**Solutions:**

1. **Check the Problems view is open:**
   - Window > Show View > Problems

2. **Check log output:**
   - Look for validation messages in the Console view
   - Check Eclipse error log: Window > Show View > Error Log

3. **Verify validators are registered:**
   - The ProofcheckHandler should register MQTransactionValidator and CatchTerminalValidator
   - Check the log for "Starting validation for:" messages

### Issue: Parser Errors

**Symptom:** Error messages about XML parsing or file format.

**Solutions:**

1. **Verify .msgflow file format:**
   - Open the .msgflow file in a text editor
   - Ensure it's valid XML
   - Check for proper namespace declarations

2. **Check file encoding:**
   - .msgflow files should be UTF-8 encoded
   - Right-click file > Properties > Resource > Text file encoding

### Debugging Tips

#### Enable Detailed Logging

The plugin uses Java logging. To see detailed logs:

1. **Check Eclipse Console:**
   - Window > Show View > Console
   - Look for messages starting with "ProofcheckHandler"

2. **Check Eclipse Error Log:**
   - Window > Show View > Error Log
   - Filter by "com.smartaceers.proofchecker"

3. **Add debug output:**
   - The updated ProofcheckHandler includes logging for:
     - Selection type
     - First element type
     - File name and extension

#### Verify Plugin Loading

1. **Check installed plugins:**
   - Help > About Eclipse > Installation Details
   - Look for "ACE Proofchecker Plugin"

2. **Check plugin state:**
   - Window > Show View > Other > Plug-in Development > Plug-ins
   - Find "com.smartaceers.proofchecker"
   - Status should be "Active"

#### Test with Sample Flow

Create a simple test message flow:

1. Create new message flow in ACE Toolkit
2. Add an MQInput node
3. Set transaction mode to "No"
4. Save the flow
5. Right-click and select "Run Proofcheck"
6. Should report a critical finding about transaction mode

### Getting Help

If you continue to experience issues:

1. **Check the logs:**
   - Eclipse error log: `<workspace>/.metadata/.log`
   - Console output for validation messages

2. **Verify environment:**
   - ACE Toolkit version
   - Eclipse version
   - Java version (should be Java 8 or higher)

3. **Review setup:**
   - Follow SETUP-GUIDE.md step by step
   - Ensure all dependencies are met

4. **Contact support:**
   - Include error messages from logs
   - Describe steps to reproduce
   - Provide ACE Toolkit version

## Known Limitations

1. **File Selection:** The plugin requires files to be selected in the Project Explorer or Navigator view
2. **Large Files:** Very large message flows (>10MB) may take longer to parse
3. **Custom Nodes:** Validation rules currently cover standard ACE nodes only

## Version History

### v1.0.0 (Current)
- Initial release
- MQ Transaction Mode validator
- Catch Terminal validator
- Enhanced file selection handling with adapter support
- Detailed logging for troubleshooting