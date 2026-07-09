package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.List;

/**
 * Interface for message flow validators.
 * Each validator implements a specific validation rule.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public interface IValidator {
    
    /**
     * Validates a specific node in the message flow.
     * 
     * @param node The node to validate
     * @return List of findings (empty if no issues found)
     */
    List<Finding> validate(FlowNode node);
    
    /**
     * Gets the unique identifier for this validator.
     * Used for configuration and reporting.
     * 
     * @return Validator ID (e.g., "mq.input.transaction.mode")
     */
    String getValidatorId();
    
    /**
     * Gets the human-readable name of this validator.
     * 
     * @return Validator name (e.g., "MQ Transaction Mode Validator")
     */
    String getValidatorName();
    
    /**
     * Gets a description of what this validator checks.
     * 
     * @return Validator description
     */
    String getDescription();
    
    /**
     * Gets the category of this validator.
     * 
     * @return Category (e.g., "Error Handling", "Message Loss Prevention")
     */
    String getCategory();
    
    /**
     * Checks if this validator is enabled.
     * 
     * @return true if enabled
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * Checks if this validator applies to a specific node type.
     * 
     * @param nodeType Node type to check
     * @return true if this validator should run on the node type
     */
    boolean appliesTo(String nodeType);
}

// Made with Bob
