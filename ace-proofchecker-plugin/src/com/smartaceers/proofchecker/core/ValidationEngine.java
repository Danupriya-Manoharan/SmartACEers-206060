package com.smartaceers.proofchecker.core;

import com.smartaceers.proofchecker.parser.FlowNode;
import com.smartaceers.proofchecker.parser.MessageFlowParser;
import com.smartaceers.proofchecker.validators.IValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main validation engine that orchestrates the validation process.
 * Coordinates parsing, validation rule execution, and result collection.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class ValidationEngine {
    
    private static final Logger LOGGER = Logger.getLogger(ValidationEngine.class.getName());
    
    private final List<IValidator> validators;
    private final MessageFlowParser parser;
    
    /**
     * Creates a new validation engine with default parser.
     */
    public ValidationEngine() {
        this.validators = new ArrayList<>();
        this.parser = new MessageFlowParser();
    }
    
    /**
     * Creates a new validation engine with custom parser.
     * 
     * @param parser Custom message flow parser
     */
    public ValidationEngine(MessageFlowParser parser) {
        this.validators = new ArrayList<>();
        this.parser = parser != null ? parser : new MessageFlowParser();
    }
    
    /**
     * Registers a validator with the engine.
     * 
     * @param validator Validator to register
     */
    public void registerValidator(IValidator validator) {
        if (validator != null && !validators.contains(validator)) {
            validators.add(validator);
            LOGGER.info("Registered validator: " + validator.getValidatorName());
        }
    }
    
    /**
     * Registers multiple validators with the engine.
     * 
     * @param validators List of validators to register
     */
    public void registerValidators(List<IValidator> validators) {
        if (validators != null) {
            for (IValidator validator : validators) {
                registerValidator(validator);
            }
        }
    }
    
    /**
     * Unregisters a validator from the engine.
     * 
     * @param validator Validator to unregister
     */
    public void unregisterValidator(IValidator validator) {
        if (validator != null) {
            validators.remove(validator);
            LOGGER.info("Unregistered validator: " + validator.getValidatorName());
        }
    }
    
    /**
     * Gets all registered validators.
     * 
     * @return List of validators
     */
    public List<IValidator> getValidators() {
        return new ArrayList<>(validators);
    }
    
    /**
     * Clears all registered validators.
     */
    public void clearValidators() {
        validators.clear();
        LOGGER.info("Cleared all validators");
    }
    
    /**
     * Validates a message flow file.
     * 
     * @param flowFilePath Path to the message flow file
     * @return Validation context with findings
     * @throws Exception if parsing or validation fails
     */
    public ValidationContext validate(String flowFilePath) throws Exception {
        if (flowFilePath == null || flowFilePath.isEmpty()) {
            throw new IllegalArgumentException("Flow file path cannot be null or empty");
        }
        
        LOGGER.info("Starting validation of: " + flowFilePath);
        
        // Parse the message flow
        List<FlowNode> nodes = parser.parse(flowFilePath);
        LOGGER.info("Parsed " + nodes.size() + " nodes from flow");
        
        // Create validation context
        ValidationContext context = new ValidationContext(flowFilePath, nodes);
        
        // Run all validators
        int validatorCount = 0;
        for (IValidator validator : validators) {
            if (context.isCancelled()) {
                LOGGER.warning("Validation cancelled");
                break;
            }
            
            try {
                LOGGER.fine("Running validator: " + validator.getValidatorName());
                List<Finding> findings = runValidator(validator, nodes);
                context.addFindings(findings);
                validatorCount++;
                
                LOGGER.fine(String.format("Validator %s found %d issues", 
                        validator.getValidatorName(), findings.size()));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error running validator: " + validator.getValidatorName(), e);
                // Continue with other validators
            }
        }
        
        LOGGER.info(String.format("Validation completed: ran %d validators, found %d issues",
                validatorCount, context.getFindingsCount()));
        
        return context;
    }
    
    /**
     * Runs a single validator on all nodes.
     * 
     * @param validator Validator to run
     * @param nodes List of nodes to validate
     * @return List of findings
     */
    private List<Finding> runValidator(IValidator validator, List<FlowNode> nodes) {
        List<Finding> allFindings = new ArrayList<>();
        
        for (FlowNode node : nodes) {
            try {
                List<Finding> findings = validator.validate(node);
                if (findings != null && !findings.isEmpty()) {
                    allFindings.addAll(findings);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, 
                        String.format("Error validating node %s with validator %s", 
                                node.getName(), validator.getValidatorName()), e);
            }
        }
        
        return allFindings;
    }
    
    /**
     * Validates a message flow with a custom context.
     * Useful for testing or when you want to provide pre-parsed nodes.
     * 
     * @param context Pre-configured validation context
     * @return Updated validation context with findings
     */
    public ValidationContext validate(ValidationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Validation context cannot be null");
        }
        
        LOGGER.info("Starting validation with custom context");
        
        List<FlowNode> nodes = context.getNodes();
        
        // Run all validators
        for (IValidator validator : validators) {
            if (context.isCancelled()) {
                LOGGER.warning("Validation cancelled");
                break;
            }
            
            try {
                List<Finding> findings = runValidator(validator, nodes);
                context.addFindings(findings);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error running validator: " + validator.getValidatorName(), e);
            }
        }
        
        LOGGER.info("Validation completed: " + context.getSummary());
        
        return context;
    }
    
    /**
     * Gets the number of registered validators.
     * 
     * @return Validator count
     */
    public int getValidatorCount() {
        return validators.size();
    }
    
    /**
     * Checks if a specific validator is registered.
     * 
     * @param validatorId ID of the validator to check
     * @return true if the validator is registered
     */
    public boolean hasValidator(String validatorId) {
        if (validatorId == null) {
            return false;
        }
        
        for (IValidator validator : validators) {
            if (validatorId.equals(validator.getValidatorId())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a validator by its ID.
     * 
     * @param validatorId ID of the validator
     * @return The validator, or null if not found
     */
    public IValidator getValidator(String validatorId) {
        if (validatorId == null) {
            return null;
        }
        
        for (IValidator validator : validators) {
            if (validatorId.equals(validator.getValidatorId())) {
                return validator;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationEngine[validators=%d]", validators.size());
    }
}

// Made with Bob
