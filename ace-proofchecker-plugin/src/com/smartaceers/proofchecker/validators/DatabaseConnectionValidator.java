package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import com.smartaceers.proofchecker.utils.ValidationLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validator that checks database connection configurations.
 * Validates connection pooling, timeout settings, and error handling.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class DatabaseConnectionValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "database.connection.config";
    private static final String VALIDATOR_NAME = "Database Connection Validator";
    private static final String CATEGORY = "Performance";
    
    // Node types that interact with databases
    private static final List<String> APPLICABLE_NODE_TYPES = Arrays.asList(
        "DatabaseInput",
        "DatabaseRetrieve",
        "DatabaseRoute"
    );
    
    // Recommended timeout values (in seconds)
    private static final int MIN_TIMEOUT = 5;
    private static final int MAX_TIMEOUT = 300;
    private static final int RECOMMENDED_TIMEOUT = 30;
    
    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        ValidationLogger.log("[DatabaseConnectionValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        // Only validate database nodes
        if (!appliesTo(node.getType())) {
            ValidationLogger.log("[DatabaseConnectionValidator] Skipping - not a database node");
            return findings;
        }
        
        // Check connection pooling
        findings.addAll(validateConnectionPooling(node));
        
        // Check timeout settings
        findings.addAll(validateTimeoutSettings(node));
        
        // Check error handling
        findings.addAll(validateErrorHandling(node));
        
        ValidationLogger.log("[DatabaseConnectionValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            ValidationLogger.log("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Validates connection pooling configuration.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateConnectionPooling(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        String dataSourceName = node.getProperty("dataSourceName");
        String useConnectionPooling = node.getProperty("useConnectionPooling");
        
        // Check if connection pooling is disabled
        if ("false".equalsIgnoreCase(useConnectionPooling) || 
            "No".equalsIgnoreCase(useConnectionPooling)) {
            
            String message = String.format(
                "Database node '%s' has connection pooling disabled. " +
                "This can lead to performance issues and connection exhaustion.",
                node.getName()
            );
            
            String suggestion = 
                "Enable connection pooling to improve performance and resource management.\n\n" +
                "Benefits:\n" +
                "- Reuses existing connections instead of creating new ones\n" +
                "- Reduces connection overhead\n" +
                "- Prevents connection exhaustion\n" +
                "- Improves overall throughput\n\n" +
                "To fix:\n" +
                "1. Open node properties\n" +
                "2. Navigate to 'Database' tab\n" +
                "3. Enable 'Use connection pooling'\n" +
                "4. Configure appropriate pool size based on load";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".pooling",
                Severity.HIGH,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        // Check if data source is properly configured
        if (dataSourceName == null || dataSourceName.trim().isEmpty()) {
            String message = String.format(
                "Database node '%s' does not have a data source configured. " +
                "This will cause runtime failures.",
                node.getName()
            );
            
            String suggestion = 
                "Configure a proper data source for the database connection.\n\n" +
                "To fix:\n" +
                "1. Open node properties\n" +
                "2. Navigate to 'Database' tab\n" +
                "3. Select or create a data source\n" +
                "4. Ensure the data source is properly configured in the broker";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".datasource",
                Severity.CRITICAL,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        return findings;
    }
    
    /**
     * Validates timeout settings.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateTimeoutSettings(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        String timeoutStr = node.getProperty("timeout");
        
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            try {
                int timeout = Integer.parseInt(timeoutStr);
                
                // Check if timeout is too low
                if (timeout < MIN_TIMEOUT) {
                    String message = String.format(
                        "Database node '%s' has a very low timeout (%d seconds). " +
                        "This may cause premature timeouts for legitimate queries.",
                        node.getName(),
                        timeout
                    );
                    
                    String suggestion = String.format(
                        "Increase the timeout to at least %d seconds. " +
                        "Recommended value is %d seconds for most scenarios.\n\n" +
                        "Consider:\n" +
                        "- Network latency\n" +
                        "- Query complexity\n" +
                        "- Database load\n" +
                        "- Expected response times",
                        MIN_TIMEOUT,
                        RECOMMENDED_TIMEOUT
                    );
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".timeout.low",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
                
                // Check if timeout is too high
                if (timeout > MAX_TIMEOUT) {
                    String message = String.format(
                        "Database node '%s' has a very high timeout (%d seconds). " +
                        "This may cause threads to hang for extended periods.",
                        node.getName(),
                        timeout
                    );
                    
                    String suggestion = String.format(
                        "Consider reducing the timeout to a more reasonable value (recommended: %d seconds). " +
                        "Very high timeouts can:\n" +
                        "- Tie up execution threads\n" +
                        "- Delay error detection\n" +
                        "- Impact overall system responsiveness",
                        RECOMMENDED_TIMEOUT
                    );
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".timeout.high",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
            } catch (NumberFormatException e) {
                String message = String.format(
                    "Database node '%s' has an invalid timeout value: '%s'",
                    node.getName(),
                    timeoutStr
                );
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".timeout.invalid",
                    Severity.HIGH,
                    message,
                    "Provide a valid numeric timeout value in seconds.",
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        } else {
            // No timeout configured - warn about using defaults
            String message = String.format(
                "Database node '%s' does not have an explicit timeout configured. " +
                "Using default timeout which may not be appropriate.",
                node.getName()
            );
            
            String suggestion = String.format(
                "Explicitly configure a timeout value (recommended: %d seconds). " +
                "This ensures predictable behavior and prevents indefinite waits.",
                RECOMMENDED_TIMEOUT
            );
            
            findings.add(new Finding(
                VALIDATOR_ID + ".timeout.missing",
                Severity.LOW,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        return findings;
    }
    
    /**
     * Validates error handling configuration.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateErrorHandling(FlowNode node) {
        List<Finding> findings = new ArrayList<>();

        // Catch-terminal checks are handled exclusively by CatchTerminalValidator
        // (limited to MQInput / HTTPInput / FileInput nodes).

        // Check transaction mode for DatabaseInput
        if ("DatabaseInput".equals(node.getType())) {
            String transactionMode = node.getProperty("transactionMode");
            if ("No".equalsIgnoreCase(transactionMode) || 
                "false".equalsIgnoreCase(transactionMode)) {
                
                String message = String.format(
                    "DatabaseInput node '%s' has transaction mode disabled. " +
                    "This may lead to data inconsistency.",
                    node.getName()
                );
                
                String suggestion = 
                    "Enable transaction mode to ensure data consistency and " +
                    "proper rollback on errors.";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".transaction",
                    Severity.HIGH,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        }
        
        return findings;
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
        return "Validates database connection configurations including connection pooling, " +
               "timeout settings, and error handling. Ensures optimal performance and " +
               "reliability for database operations.";
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