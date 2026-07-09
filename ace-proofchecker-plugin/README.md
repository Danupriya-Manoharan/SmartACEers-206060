# ACE Proofchecker Eclipse Plugin

An Eclipse plugin for IBM App Connect Enterprise (ACE) Toolkit that validates message flows for common production issues.

## Overview

The ACE Proofchecker plugin helps prevent production issues by automatically detecting:
- **MQ Transaction Mode Issues**: Flags when MQ Input nodes have transaction mode set to "No", which can lead to message loss
- **Unconnected Catch Terminals**: Detects when error handling terminals are not connected, causing silent failures

## Features

✅ **Automated Validation**: Right-click validation on .msgflow files  
✅ **Eclipse Integration**: Results appear in Problems view  
✅ **Critical Issue Detection**: Focuses on production-critical problems  
✅ **Detailed Suggestions**: Provides fix recommendations for each issue  
✅ **Extensible Framework**: Easy to add new validators  

## Prerequisites

- IBM ACE Toolkit v12.x or higher
- Eclipse Plugin Development Environment (PDE)
- Java 11 or higher
- JUnit 5 (for running tests)

## Building the Plugin

### Option 1: Using Eclipse PDE

1. **Import the Plugin Project**
   ```
   File → Import → Existing Projects into Workspace
   Select: ace-proofchecker-plugin directory
   ```

2. **Set Target Platform**
   ```
   Window → Preferences → Plug-in Development → Target Platform
   Select your ACE Toolkit installation
   ```

3. **Build the Plugin**
   ```
   Right-click on project → Export → Plug-in Development → Deployable plug-ins and fragments
   Destination: Choose a directory for the output
   Click Finish
   ```

4. **Output**: The plugin JAR will be created in the specified directory

### Option 2: Using Maven (if pom.xml is configured)

```bash
cd ace-proofchecker-plugin
mvn clean package
```

## Installation

### Method 1: Copy to Dropins Folder

1. Locate your ACE Toolkit installation directory
2. Copy the plugin JAR to: `<ACE_TOOLKIT>/dropins/`
3. Restart ACE Toolkit

### Method 2: Install via Eclipse

1. In ACE Toolkit: `Help → Install New Software`
2. Click `Add` → `Archive`
3. Select the plugin JAR file
4. Follow the installation wizard
5. Restart ACE Toolkit

### Method 3: Run as Eclipse Application (Development)

1. Open the plugin project in Eclipse
2. Right-click on project → `Run As → Eclipse Application`
3. A new Eclipse instance will launch with the plugin installed
4. Test the plugin in this runtime instance

## Usage

1. **Open a Message Flow**: Open any `.msgflow` file in ACE Toolkit

2. **Run Proofcheck**:
   - Right-click on the `.msgflow` file in Project Explorer
   - Select `Run Proofcheck` from the context menu
   - OR: Use the menu `ACE Proofcheck → Run Proofcheck`

3. **View Results**:
   - Results appear in the `Problems` view (Window → Show View → Problems)
   - Critical issues are marked with error icons
   - Warnings are marked with warning icons

4. **Fix Issues**:
   - Double-click on a problem to navigate to the issue
   - Read the suggestion for how to fix it
   - Make the necessary changes to your message flow

## Validation Rules

### Rule 1: MQ Input Transaction Mode

**ID**: `mq.input.transaction.mode`  
**Severity**: Critical  
**Description**: Detects when MQ Input nodes have transaction mode set to "No"

**Why it matters**: Without transaction mode, messages are removed from the queue immediately upon retrieval. If processing fails, the message is lost forever.

**How to fix**:
1. Open the MQ Input node properties
2. Navigate to the "MQ Connection" tab
3. Set "Transaction mode" to "Yes"
4. Save the message flow

### Rule 2: Catch Terminal Connection

**ID**: `error.handling.catch.terminal`  
**Severity**: Critical  
**Description**: Detects when nodes have unconnected catch terminals

**Why it matters**: Unconnected catch terminals mean exceptions are not handled, leading to silent failures and potential data loss.

**How to fix**:
1. Create an error handling subflow or flow
2. Connect the catch terminal to the error handler
3. Implement proper error logging and handling
4. Save the message flow

## Project Structure

```
ace-proofchecker-plugin/
├── META-INF/
│   └── MANIFEST.MF          # Plugin manifest
├── plugin.xml               # Eclipse plugin configuration
├── build.properties         # Build configuration
├── src/
│   └── com/smartaceers/proofchecker/
│       ├── Activator.java   # Plugin activator
│       ├── core/            # Core validation framework
│       ├── parser/          # Message flow parser
│       ├── validators/      # Validation rules
│       ├── results/         # Results handling
│       └── handlers/        # Eclipse command handlers
└── test/
    └── com/smartaceers/proofchecker/
        └── validators/      # Unit tests
```

## Development

### Running Tests

```bash
# Using Maven
mvn test

# Using Eclipse
Right-click on test class → Run As → JUnit Test
```

### Adding New Validators

1. Create a new class implementing `IValidator`
2. Implement the validation logic in the `validate()` method
3. Register the validator in `ProofcheckHandler.java`
4. Add unit tests for the new validator

Example:
```java
public class MyValidator implements IValidator {
    @Override
    public List<Finding> validate(FlowNode node) {
        // Your validation logic here
    }
    
    @Override
    public String getValidatorId() {
        return "my.validator.id";
    }
    
    // Implement other interface methods...
}
```

## Troubleshooting

### Plugin doesn't appear in context menu

**Solution**:
- Ensure the plugin is properly installed
- Restart ACE Toolkit
- Check that you're right-clicking on a `.msgflow` file
- Check Eclipse Error Log: `Window → Show View → Error Log`

### No validation results appear

**Solution**:
- Check the Problems view is open: `Window → Show View → Problems`
- Ensure the message flow file is valid XML
- Check Eclipse Error Log for exceptions

### Build errors

**Solution**:
- Verify Java 11 or higher is installed
- Check that Eclipse PDE is installed
- Ensure target platform is set to ACE Toolkit
- Clean and rebuild: `Project → Clean`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

[Specify your license here]

## Support

For issues and questions:
- GitHub Issues: https://github.com/Danupriya-Manoharan/SmartACEers-206060/issues
- Email: [your-email@example.com]

## Roadmap

### Phase 2 (Future)
- Failure terminal validation
- Transaction boundary checks
- Logging validation
- Configurable severity levels

### Phase 3 (Future)
- Custom results view
- Quick-fix actions
- Batch validation
- Configuration UI

### Phase 4 (Future)
- Security validators
- Performance validators
- Custom rule framework
- CI/CD integration

## Authors

SmartACEers Team - Danupriya Manoharan

## Acknowledgments

- IBM App Connect Enterprise Team
- Eclipse Plugin Development Community