package com.smartaceers.proofchecker.core;

import com.smartaceers.proofchecker.parser.FlowNode;

/**
 * Represents a validation finding (issue or suggestion) discovered during message flow analysis.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class Finding {
    
    private final String ruleId;
    private final Severity severity;
    private final String message;
    private final String suggestion;
    private final FlowNode node;
    private final int lineNumber;
    private final String category;
    
    /**
     * Creates a new Finding with all details.
     * 
     * @param ruleId Unique identifier for the validation rule
     * @param severity Severity level of the finding
     * @param message Description of the issue
     * @param suggestion Recommended action to fix the issue
     * @param node The flow node where the issue was found
     * @param lineNumber Line number in the message flow file
     * @param category Category of the finding (e.g., "Error Handling", "Message Loss Prevention")
     */
    public Finding(String ruleId, Severity severity, String message, 
                   String suggestion, FlowNode node, int lineNumber, String category) {
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.suggestion = suggestion;
        this.node = node;
        this.lineNumber = lineNumber;
        this.category = category;
    }
    
    /**
     * Creates a new Finding with automatic line number detection.
     * 
     * @param ruleId Unique identifier for the validation rule
     * @param severity Severity level of the finding
     * @param message Description of the issue
     * @param suggestion Recommended action to fix the issue
     * @param node The flow node where the issue was found
     */
    public Finding(String ruleId, Severity severity, String message, 
                   String suggestion, FlowNode node) {
        this(ruleId, severity, message, suggestion, node, 
             node != null ? node.getLineNumber() : -1, 
             extractCategory(ruleId));
    }
    
    /**
     * Extracts category from rule ID.
     * 
     * @param ruleId The rule identifier
     * @return Category name
     */
    private static String extractCategory(String ruleId) {
        if (ruleId == null) {
            return "General";
        }
        
        if (ruleId.startsWith("mq.")) {
            return "Message Queue";
        } else if (ruleId.startsWith("error.")) {
            return "Error Handling";
        } else if (ruleId.startsWith("security.")) {
            return "Security";
        } else if (ruleId.startsWith("performance.")) {
            return "Performance";
        } else if (ruleId.startsWith("logging.")) {
            return "Logging";
        }
        
        return "General";
    }
    
    /**
     * Gets the rule identifier.
     * 
     * @return Rule ID
     */
    public String getRuleId() {
        return ruleId;
    }
    
    /**
     * Gets the severity level.
     * 
     * @return Severity
     */
    public Severity getSeverity() {
        return severity;
    }
    
    /**
     * Gets the issue description.
     * 
     * @return Message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the suggested fix.
     * 
     * @return Suggestion
     */
    public String getSuggestion() {
        return suggestion;
    }
    
    /**
     * Gets the flow node where the issue was found.
     * 
     * @return Flow node
     */
    public FlowNode getNode() {
        return node;
    }
    
    /**
     * Gets the line number in the message flow file.
     * 
     * @return Line number (-1 if unknown)
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Gets the category of the finding.
     * 
     * @return Category name
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Gets the node name if available.
     * 
     * @return Node name or "Unknown"
     */
    public String getNodeName() {
        return node != null ? node.getName() : "Unknown";
    }
    
    /**
     * Gets the node type if available.
     * 
     * @return Node type or "Unknown"
     */
    public String getNodeType() {
        return node != null ? node.getType() : "Unknown";
    }
    
    /**
     * Creates a formatted message for display.
     * 
     * @return Formatted message
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(severity.getDisplayName()).append(": ");
        sb.append(message);
        
        if (node != null) {
            sb.append(" [Node: ").append(node.getName()).append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a detailed message including suggestion.
     * 
     * @return Detailed message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedMessage());
        
        if (suggestion != null && !suggestion.isEmpty()) {
            sb.append("\n\nSuggestion: ").append(suggestion);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Finding[ruleId=%s, severity=%s, node=%s, message=%s]",
                ruleId, severity, getNodeName(), message);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Finding other = (Finding) obj;
        return ruleId.equals(other.ruleId) &&
               severity == other.severity &&
               lineNumber == other.lineNumber &&
               (node != null ? node.equals(other.node) : other.node == null);
    }
    
    @Override
    public int hashCode() {
        int result = ruleId.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result + lineNumber;
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }
}

// Made with Bob
