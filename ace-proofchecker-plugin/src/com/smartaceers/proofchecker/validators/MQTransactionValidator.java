package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator that checks MQ Input nodes for transaction mode configuration.
 * Flags nodes where transaction mode is set to "No", which can lead to message loss.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class MQTransactionValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "mq.input.transaction.mode";
    private static final String VALIDATOR_NAME = "MQ Transaction Mode Validator";
    private static final String CATEGORY = "Message Loss Prevention";
    private static final String NODE_TYPE = "MQInput";
    
    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        System.out.println("[MQTransactionValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        // Only validate MQ Input nodes
        if (!appliesTo(node.getType())) {
            System.out.println("[MQTransactionValidator] Skipping - not an MQInput node");
            return findings;
        }
        
        // Check transaction mode property
        String transactionMode = node.getProperty("transactionMode");
        System.out.println("[MQTransactionValidator] Transaction mode: " + transactionMode);
        
        // If transaction mode is explicitly set to "No", flag it
        if ("No".equalsIgnoreCase(transactionMode) || "false".equalsIgnoreCase(transactionMode)) {
            Finding finding = createFinding(node, transactionMode);
            findings.add(finding);
        }
        
        System.out.println("[MQTransactionValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            System.out.println("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Creates a finding for transaction mode issue.
     * 
     * @param node The node with the issue
     * @param transactionMode The current transaction mode value
     * @return Finding object
     */
    private Finding createFinding(FlowNode node, String transactionMode) {
        String message = String.format(
            "MQ Input node '%s' has transaction mode set to '%s'. " +
            "Messages may be lost if processing fails.",
            node.getName(),
            transactionMode
        );
        
        String suggestion = 
            "Set transaction mode to 'Yes' to ensure message persistence and " +
            "automatic rollback on failure. This prevents message loss in case of " +
            "processing errors or system failures.\n\n" +
            "To fix:\n" +
            "1. Open the node properties\n" +
            "2. Navigate to the 'MQ Connection' tab\n" +
            "3. Set 'Transaction mode' to 'Yes'\n" +
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
        return "Checks if MQ Input nodes have transaction mode set to 'No', " +
               "which can lead to message loss if processing fails. " +
               "When transaction mode is disabled, messages are removed from the queue " +
               "immediately upon retrieval, without the ability to rollback on errors.";
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
        return NODE_TYPE.equals(nodeType);
    }
    
    /**
     * Checks if a transaction mode value is considered safe.
     * 
     * @param transactionMode Transaction mode value
     * @return true if the value indicates transactions are enabled
     */
    public boolean isTransactionModeEnabled(String transactionMode) {
        if (transactionMode == null || transactionMode.isEmpty()) {
            // Default behavior - assume safe if not specified
            return true;
        }
        
        return "Yes".equalsIgnoreCase(transactionMode) || 
               "true".equalsIgnoreCase(transactionMode) ||
               "Automatic".equalsIgnoreCase(transactionMode);
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled);
    }
}

// Made with Bob
