# SmartACEers Message Flow Proofchecker

An Eclipse plugin for IBM ACE Toolkit that automatically validates message flows and identifies potential production issues before deployment.

## 🎯 Project Overview

This tool helps ACE developers catch common mistakes that might only surface in production environments by analyzing message flows and flagging potential issues with clear, actionable suggestions.

### Current Version: 1.0.0-MVP

The MVP focuses on two critical validation scenarios:
1. **MQ Input Transaction Mode**: Detects when transaction mode is disabled, risking message loss
2. **Catch Terminal Connection**: Identifies unconnected catch terminals that could lead to unhandled errors

## 🚀 Features

- ✅ Automatic validation of ACE message flows
- ✅ Detection of critical production issues
- ✅ Integration with Eclipse Problems view
- ✅ Right-click menu integration in ACE Toolkit
- ✅ Clear, actionable suggestions for each finding
- ✅ Severity-based categorization (Critical, Warning)

## 📋 Prerequisites

- IBM ACE Toolkit v12.x or higher
- Eclipse IDE with Plugin Development Environment (PDE)
- Java 11 or higher
- Git for version control

## 🛠️ Installation

### Option 1: Install from Update Site (Coming Soon)
1. Open ACE Toolkit
2. Go to Help → Install New Software
3. Add the update site URL
4. Select "SmartACEers Proofchecker" and install

### Option 2: Build from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/Danupriya-Manoharan/SmartACEers-206060.git
   cd SmartACEers-206060
   ```

2. Import the project into Eclipse:
   - File → Import → Existing Projects into Workspace
   - Select the `ace-proofchecker-plugin` directory

3. Build the plugin:
   - Right-click on project → Export → Plug-in Development → Deployable plug-ins and fragments
   - Select destination directory
   - Click Finish

4. Install the plugin:
   - Copy the generated JAR to ACE Toolkit's `dropins` folder
   - Restart ACE Toolkit

## 📖 Usage

### Running Proofcheck

1. Open a message flow in ACE Toolkit
2. Right-click on the message flow file (`.msgflow`)
3. Select "Run Proofcheck" from the context menu
4. View results in the Eclipse Problems view

### Understanding Results

Results are displayed in the Eclipse Problems view with the following information:
- **Severity**: Critical (red X) or Warning (yellow !)
- **Description**: What issue was found
- **Location**: Which node has the issue
- **Suggestion**: How to fix the issue

### Example Output

```
Critical: MQ Input node 'OrderInput' has transaction mode set to 'No'. 
Messages may be lost if processing fails.
→ Suggestion: Set transaction mode to 'Yes' to ensure message persistence.

Critical: Node 'DatabaseInsert' has an unconnected catch terminal. 
Errors will not be handled.
→ Suggestion: Connect the catch terminal to an error handling flow.
```

## 🔍 Validation Rules

### Rule 1: MQ Input Transaction Mode
- **ID**: `mq.input.transaction.mode`
- **Severity**: Critical
- **Description**: Checks if MQ Input nodes have transaction mode disabled
- **Risk**: Messages may be lost if processing fails
- **Fix**: Enable transaction mode in node properties

### Rule 2: Catch Terminal Connection
- **ID**: `error.handling.catch.terminal`
- **Severity**: Critical
- **Description**: Checks if catch terminals are connected
- **Risk**: Errors will not be handled, causing silent failures
- **Fix**: Connect catch terminal to error handling flow

## 🏗️ Project Structure

```
SmartACEers-206060/
├── README.md                          # This file
├── MVP-PLAN.md                        # Detailed MVP implementation plan
├── docs/                              # Documentation
│   ├── setup-guide.md                # Setup instructions
│   └── user-guide.md                 # User guide
├── ace-proofchecker-plugin/          # Eclipse plugin source
│   ├── src/                          # Java source code
│   │   └── com/smartaceers/proofchecker/
│   │       ├── core/                 # Core validation engine
│   │       ├── parser/               # Message flow parser
│   │       ├── validators/           # Validation rules
│   │       ├── results/              # Results handling
│   │       └── handlers/             # Eclipse handlers
│   ├── test/                         # Unit tests
│   └── plugin.xml                    # Eclipse plugin configuration
└── .gitignore                        # Git ignore rules
```

## 🧪 Testing

### Running Unit Tests
```bash
cd ace-proofchecker-plugin
mvn test
```

### Test Coverage
- MQ Transaction Mode validator
- Catch Terminal validator
- Integration with Eclipse Problems view

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java coding conventions
- Write unit tests for new validators
- Update documentation for new features
- Test with real ACE message flows

## 📚 Documentation

- [MVP Plan](MVP-PLAN.md) - Detailed implementation plan
- [Setup Guide](docs/setup-guide.md) - Development environment setup
- [User Guide](docs/user-guide.md) - End-user documentation

## 🗺️ Roadmap

### MVP (Current)
- ✅ MQ Input transaction mode validation
- ✅ Catch terminal connection validation
- ✅ Eclipse Problems view integration

### Phase 2 (Planned)
- [ ] Failure terminal validation
- [ ] Transaction boundary checks
- [ ] Basic logging validation
- [ ] Configurable severity levels

### Phase 3 (Future)
- [ ] Custom results view with grouping
- [ ] Quick-fix actions
- [ ] Batch validation for multiple flows
- [ ] Configuration UI

### Phase 4 (Future)
- [ ] Security validators
- [ ] Performance validators
- [ ] Custom rule framework
- [ ] CI/CD integration

## 🐛 Known Issues

- None currently reported

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- SmartACEers Team
- Repository: https://github.com/Danupriya-Manoharan/SmartACEers-206060

## 🙏 Acknowledgments

- IBM ACE Toolkit team for the excellent integration APIs
- Eclipse community for plugin development resources
- All contributors and testers

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check the [User Guide](docs/user-guide.md)
- Review the [MVP Plan](MVP-PLAN.md)

## 📊 Project Status

**Current Phase**: MVP Development  
**Status**: Planning Complete, Ready for Implementation  
**Next Milestone**: Core Framework Implementation

---

**Last Updated**: 2026-06-11  
**Version**: 1.0.0-MVP