package com.smartaceers.proofchecker.parser;

/**
 * Represents a connection between two nodes in an ACE message flow.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class FlowConnection {
    
    private final String sourceNodeName;
    private final String sourceTerminal;
    private final String targetNodeName;
    private final String targetTerminal;
    private final String connectionId;
    
    /**
     * Creates a new flow connection.
     * 
     * @param sourceNodeName Name of the source node
     * @param sourceTerminal Name of the source terminal (e.g., "out", "failure", "catch")
     * @param targetNodeName Name of the target node
     * @param targetTerminal Name of the target terminal (e.g., "in")
     * @param connectionId Unique identifier for this connection
     */
    public FlowConnection(String sourceNodeName, String sourceTerminal,
                         String targetNodeName, String targetTerminal,
                         String connectionId) {
        this.sourceNodeName = sourceNodeName;
        this.sourceTerminal = sourceTerminal;
        this.targetNodeName = targetNodeName;
        this.targetTerminal = targetTerminal;
        this.connectionId = connectionId;
    }
    
    /**
     * Creates a new flow connection with auto-generated ID.
     * 
     * @param sourceNodeName Name of the source node
     * @param sourceTerminal Name of the source terminal
     * @param targetNodeName Name of the target node
     * @param targetTerminal Name of the target terminal
     */
    public FlowConnection(String sourceNodeName, String sourceTerminal,
                         String targetNodeName, String targetTerminal) {
        this(sourceNodeName, sourceTerminal, targetNodeName, targetTerminal,
             generateConnectionId(sourceNodeName, sourceTerminal, targetNodeName));
    }
    
    /**
     * Generates a connection ID from source and target information.
     * 
     * @param sourceNodeName Source node name
     * @param sourceTerminal Source terminal name
     * @param targetNodeName Target node name
     * @return Generated connection ID
     */
    private static String generateConnectionId(String sourceNodeName, 
                                               String sourceTerminal, 
                                               String targetNodeName) {
        return String.format("%s.%s->%s", sourceNodeName, sourceTerminal, targetNodeName);
    }
    
    /**
     * Gets the source node name.
     * 
     * @return Source node name
     */
    public String getSourceNodeName() {
        return sourceNodeName;
    }
    
    /**
     * Gets the source terminal name.
     * 
     * @return Source terminal name
     */
    public String getSourceTerminal() {
        return sourceTerminal;
    }
    
    /**
     * Gets the target node name.
     * 
     * @return Target node name
     */
    public String getTargetNodeName() {
        return targetNodeName;
    }
    
    /**
     * Gets the target terminal name.
     * 
     * @return Target terminal name
     */
    public String getTargetTerminal() {
        return targetTerminal;
    }
    
    /**
     * Gets the connection ID.
     * 
     * @return Connection ID
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * Checks if this is a catch terminal connection.
     * 
     * @return true if the source terminal is "catch"
     */
    public boolean isCatchConnection() {
        return "catch".equalsIgnoreCase(sourceTerminal);
    }
    
    /**
     * Checks if this is a failure terminal connection.
     * 
     * @return true if the source terminal is "failure"
     */
    public boolean isFailureConnection() {
        return "failure".equalsIgnoreCase(sourceTerminal);
    }
    
    /**
     * Checks if this is an output terminal connection.
     * 
     * @return true if the source terminal is "out" or "output"
     */
    public boolean isOutputConnection() {
        return "out".equalsIgnoreCase(sourceTerminal) || 
               "output".equalsIgnoreCase(sourceTerminal);
    }
    
    /**
     * Checks if this connection originates from a specific node.
     * 
     * @param nodeName Node name to check
     * @return true if the connection originates from the specified node
     */
    public boolean isFromNode(String nodeName) {
        return sourceNodeName != null && sourceNodeName.equals(nodeName);
    }
    
    /**
     * Checks if this connection targets a specific node.
     * 
     * @param nodeName Node name to check
     * @return true if the connection targets the specified node
     */
    public boolean isToNode(String nodeName) {
        return targetNodeName != null && targetNodeName.equals(nodeName);
    }
    
    /**
     * Checks if this connection involves a specific node (as source or target).
     * 
     * @param nodeName Node name to check
     * @return true if the connection involves the specified node
     */
    public boolean involvesNode(String nodeName) {
        return isFromNode(nodeName) || isToNode(nodeName);
    }
    
    @Override
    public String toString() {
        return String.format("Connection[%s.%s -> %s.%s]",
                sourceNodeName, sourceTerminal, targetNodeName, targetTerminal);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FlowConnection other = (FlowConnection) obj;
        return connectionId.equals(other.connectionId);
    }
    
    @Override
    public int hashCode() {
        return connectionId.hashCode();
    }
}

// Made with Bob
