package com.smartaceers.proofchecker.core;

import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context object that holds state during validation of a message flow.
 * Provides access to the flow structure and collects findings.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class ValidationContext {
    
    private final String flowFilePath;
    private final List<FlowNode> nodes;
    private final List<Finding> findings;
    private final Map<String, Object> properties;
    private boolean cancelled;
    
    /**
     * Creates a new validation context.
     * 
     * @param flowFilePath Path to the message flow file being validated
     * @param nodes List of nodes in the message flow
     */
    public ValidationContext(String flowFilePath, List<FlowNode> nodes) {
        this.flowFilePath = flowFilePath;
        this.nodes = nodes != null ? new ArrayList<>(nodes) : new ArrayList<>();
        this.findings = new ArrayList<>();
        this.properties = new HashMap<>();
        this.cancelled = false;
    }
    
    /**
     * Gets the path to the message flow file.
     * 
     * @return Flow file path
     */
    public String getFlowFilePath() {
        return flowFilePath;
    }
    
    /**
     * Gets all nodes in the message flow.
     * 
     * @return List of flow nodes
     */
    public List<FlowNode> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    /**
     * Gets a node by its name.
     * 
     * @param nodeName Name of the node to find
     * @return The node, or null if not found
     */
    public FlowNode getNodeByName(String nodeName) {
        if (nodeName == null) {
            return null;
        }
        
        for (FlowNode node : nodes) {
            if (nodeName.equals(node.getName())) {
                return node;
            }
        }
        
        return null;
    }
    
    /**
     * Gets all nodes of a specific type.
     * 
     * @param nodeType Type of nodes to find (e.g., "MQInput", "Compute")
     * @return List of matching nodes
     */
    public List<FlowNode> getNodesByType(String nodeType) {
        List<FlowNode> result = new ArrayList<>();
        
        if (nodeType == null) {
            return result;
        }
        
        for (FlowNode node : nodes) {
            if (nodeType.equals(node.getType())) {
                result.add(node);
            }
        }
        
        return result;
    }
    
    /**
     * Adds a finding to the context.
     * 
     * @param finding The finding to add
     */
    public void addFinding(Finding finding) {
        if (finding != null) {
            findings.add(finding);
        }
    }
    
    /**
     * Adds multiple findings to the context.
     * 
     * @param findings List of findings to add
     */
    public void addFindings(List<Finding> findings) {
        if (findings != null) {
            this.findings.addAll(findings);
        }
    }
    
    /**
     * Gets all findings collected during validation.
     * 
     * @return List of findings
     */
    public List<Finding> getFindings() {
        return new ArrayList<>(findings);
    }
    
    /**
     * Gets findings of a specific severity.
     * 
     * @param severity Severity level to filter by
     * @return List of findings with the specified severity
     */
    public List<Finding> getFindingsBySeverity(Severity severity) {
        List<Finding> result = new ArrayList<>();
        
        for (Finding finding : findings) {
            if (finding.getSeverity() == severity) {
                result.add(finding);
            }
        }
        
        return result;
    }
    
    /**
     * Gets the count of findings.
     * 
     * @return Number of findings
     */
    public int getFindingsCount() {
        return findings.size();
    }
    
    /**
     * Gets the count of critical findings.
     * Mirrors the "error" marker bucket: CRITICAL and HIGH severities.
     *
     * @return Number of critical findings
     */
    public int getCriticalCount() {
        return getFindingsBySeverity(Severity.CRITICAL).size()
             + getFindingsBySeverity(Severity.HIGH).size();
    }

    /**
     * Gets the count of warning findings.
     * Mirrors the "warning" marker bucket: MEDIUM (and the deprecated WARNING).
     *
     * @return Number of warning findings
     */
    public int getWarningCount() {
        return getFindingsBySeverity(Severity.MEDIUM).size()
             + getFindingsBySeverity(Severity.WARNING).size();
    }
    
    /**
     * Checks if there are any critical findings.
     * 
     * @return true if there are critical findings
     */
    public boolean hasCriticalFindings() {
        return getCriticalCount() > 0;
    }
    
    /**
     * Sets a custom property in the context.
     * 
     * @param key Property key
     * @param value Property value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * Gets a custom property from the context.
     * 
     * @param key Property key
     * @return Property value, or null if not found
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Gets a custom property with a default value.
     * 
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value, or default value if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        Object value = properties.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * Checks if a property exists.
     * 
     * @param key Property key
     * @return true if the property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Marks the validation as cancelled.
     */
    public void cancel() {
        this.cancelled = true;
    }
    
    /**
     * Checks if validation has been cancelled.
     * 
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Clears all findings.
     */
    public void clearFindings() {
        findings.clear();
    }
    
    /**
     * Gets a summary of the validation results.
     * 
     * @return Summary string
     */
    public String getSummary() {
        return String.format("Validation completed: %d findings (%d critical, %d warnings)",
                getFindingsCount(), getCriticalCount(), getWarningCount());
    }
    
    @Override
    public String toString() {
        return String.format("ValidationContext[file=%s, nodes=%d, findings=%d]",
                flowFilePath, nodes.size(), findings.size());
    }
}

// Made with Bob
