package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validator that checks if catch terminals are properly connected.
 * Flags nodes that have catch terminals but no connections, which means errors won't be handled.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class CatchTerminalValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "error.handling.catch.terminal";
    private static final String VALIDATOR_NAME = "Catch Terminal Validator";
    private static final String CATEGORY = "Error Handling";
    
    // Catch-terminal checks are limited to these input node types
    private static final List<String> APPLICABLE_NODE_TYPES = Arrays.asList(
        "MQInput",
        "HTTPInput",
        "FileInput"
    );
    
    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        System.out.println("[CatchTerminalValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        // Only validate nodes that can have catch terminals
        if (!appliesTo(node.getType())) {
            System.out.println("[CatchTerminalValidator] Skipping - node type not applicable");
            return findings;
        }
        
        // Check if node has a catch terminal
        if (!node.hasCatchTerminal()) {
            System.out.println("[CatchTerminalValidator] Node has no catch terminal");
            return findings;
        }
        
        // Check if catch terminal is connected
        if (!node.isCatchTerminalConnected()) {
            System.out.println("[CatchTerminalValidator] Catch terminal is NOT connected");
            Finding finding = createFinding(node);
            findings.add(finding);
        } else {
            System.out.println("[CatchTerminalValidator] Catch terminal is connected");
        }
        
        System.out.println("[CatchTerminalValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            System.out.println("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Creates a finding for unconnected catch terminal.
     * 
     * @param node The node with the unconnected catch terminal
     * @return Finding object
     */
    private Finding createFinding(FlowNode node) {
        String message = String.format(
            "Node '%s' (type: %s) has an unconnected catch terminal. " +
            "Errors will not be handled, potentially causing data loss or silent failures.",
            node.getName(),
            node.getType()
        );
        
        String suggestion = 
            "Connect the catch terminal to an error handling flow or subflow. " +
            "This ensures that exceptions and errors are properly caught and handled.\n\n" +
            "Best practices:\n" +
            "1. Create an error handling subflow that:\n" +
            "   - Logs the error details\n" +
            "   - Stores failed messages for retry\n" +
            "   - Sends alerts if needed\n" +
            "2. Connect the catch terminal to this error handler\n" +
            "3. Consider also connecting the failure terminal\n\n" +
            "To fix:\n" +
            "1. Right-click on the catch terminal\n" +
            "2. Select 'Create Connection'\n" +
            "3. Connect to an error handling node or subflow\n" +
            "4. Save the message flow";
        
        return new Finding(
            VALIDATOR_ID,
            Severity.CRITICAL,
            message,
            suggestion,
            node,
            node.getLineNumber(),
            CATEGORY
        );
    }
    
    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }
    
    @Override
    public String getValidatorName() {
        return VALIDATOR_NAME;
    }
    
    @Override
    public String getDescription() {
        return "Checks if nodes with catch terminals have them properly connected. " +
               "Unconnected catch terminals mean that exceptions and errors thrown during " +
               "node execution will not be handled, potentially causing data loss, " +
               "silent failures, or unexpected flow termination.";
    }
    
    @Override
    public String getCategory() {
        return CATEGORY;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether this validator is enabled.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean appliesTo(String nodeType) {
        return APPLICABLE_NODE_TYPES.contains(nodeType);
    }
    
    /**
     * Checks if a node type typically has error handling terminals.
     * 
     * @param nodeType Node type to check
     * @return true if the node type typically has catch/failure terminals
     */
    public boolean hasErrorHandlingTerminals(String nodeType) {
        return appliesTo(nodeType);
    }
    
    /**
     * Gets the list of node types this validator applies to.
     * 
     * @return List of applicable node types
     */
    public List<String> getApplicableNodeTypes() {
        return new ArrayList<>(APPLICABLE_NODE_TYPES);
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled);
    }
}

// Made with Bob
