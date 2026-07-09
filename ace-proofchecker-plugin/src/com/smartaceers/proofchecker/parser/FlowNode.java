package com.smartaceers.proofchecker.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a node in an ACE message flow.
 * Contains node properties, connections, and terminal information.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class FlowNode {
    
    private final String name;
    private final String type;
    private final String nodeId;
    private final Map<String, String> properties;
    private final List<FlowConnection> outgoingConnections;
    private final List<FlowConnection> incomingConnections;
    private final List<String> terminals;
    private int lineNumber;
    private String sourceDirectory;
    
    /**
     * Creates a new flow node.
     * 
     * @param name Node name
     * @param type Node type (e.g., "MQInput", "Compute", "MQOutput")
     * @param nodeId Unique identifier for the node
     */
    public FlowNode(String name, String type, String nodeId) {
        this.name = name;
        this.type = type;
        this.nodeId = nodeId;
        this.properties = new HashMap<>();
        this.outgoingConnections = new ArrayList<>();
        this.incomingConnections = new ArrayList<>();
        this.terminals = new ArrayList<>();
        this.lineNumber = -1;
        
        // Terminals will be discovered from actual connections in the msgflow file
        // No hardcoded terminal initialization
    }
    
    /**
     * Gets the node name.
     * 
     * @return Node name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the node type.
     * 
     * @return Node type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the node ID.
     * 
     * @return Node ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * Sets a property value.
     * 
     * @param key Property key
     * @param value Property value
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    /**
     * Gets a property value.
     * 
     * @param key Property key
     * @return Property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Gets a property value with a default.
     * 
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value, or default value if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
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
     * Gets all properties.
     * 
     * @return Map of all properties
     */
    public Map<String, String> getProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * Adds an outgoing connection.
     * 
     * @param connection Connection to add
     */
    public void addOutgoingConnection(FlowConnection connection) {
        if (connection != null && !outgoingConnections.contains(connection)) {
            outgoingConnections.add(connection);
        }
    }
    
    /**
     * Adds an incoming connection.
     * 
     * @param connection Connection to add
     */
    public void addIncomingConnection(FlowConnection connection) {
        if (connection != null && !incomingConnections.contains(connection)) {
            incomingConnections.add(connection);
        }
    }
    
    /**
     * Gets all outgoing connections.
     * 
     * @return List of outgoing connections
     */
    public List<FlowConnection> getOutgoingConnections() {
        return new ArrayList<>(outgoingConnections);
    }
    
    /**
     * Gets all incoming connections.
     * 
     * @return List of incoming connections
     */
    public List<FlowConnection> getIncomingConnections() {
        return new ArrayList<>(incomingConnections);
    }
    
    /**
     * Gets outgoing connections from a specific terminal.
     * 
     * @param terminalName Terminal name
     * @return List of connections from the specified terminal
     */
    public List<FlowConnection> getConnectionsFromTerminal(String terminalName) {
        List<FlowConnection> result = new ArrayList<>();
        for (FlowConnection conn : outgoingConnections) {
            if (terminalName.equalsIgnoreCase(conn.getSourceTerminal())) {
                result.add(conn);
            }
        }
        return result;
    }
    
    /**
     * Adds a terminal to this node.
     * 
     * @param terminalName Terminal name
     */
    public void addTerminal(String terminalName) {
        if (terminalName != null && !terminals.contains(terminalName)) {
            terminals.add(terminalName);
        }
    }
    
    /**
     * Gets all terminals.
     * 
     * @return List of terminal names
     */
    public List<String> getTerminals() {
        return new ArrayList<>(terminals);
    }
    
    /**
     * Checks if the node has a specific terminal.
     * 
     * @param terminalName Terminal name
     * @return true if the terminal exists
     */
    public boolean hasTerminal(String terminalName) {
        return terminals.contains(terminalName);
    }
    
    /**
     * Checks if the node has a catch terminal.
     * 
     * @return true if catch terminal exists
     */
    public boolean hasCatchTerminal() {
        return hasTerminal("catch");
    }
    
    /**
     * Checks if the node has a failure terminal.
     * 
     * @return true if failure terminal exists
     */
    public boolean hasFailureTerminal() {
        return hasTerminal("failure");
    }
    
    /**
     * Checks if a terminal is connected.
     * 
     * @param terminalName Terminal name
     * @return true if the terminal has at least one connection
     */
    public boolean isTerminalConnected(String terminalName) {
        return !getConnectionsFromTerminal(terminalName).isEmpty();
    }
    
    /**
     * Checks if the catch terminal is connected.
     * 
     * @return true if catch terminal is connected
     */
    public boolean isCatchTerminalConnected() {
        return isTerminalConnected("catch");
    }
    
    /**
     * Checks if the failure terminal is connected.
     * 
     * @return true if failure terminal is connected
     */
    public boolean isFailureTerminalConnected() {
        return isTerminalConnected("failure");
    }
    
    /**
     * Sets the line number in the source file.
     * 
     * @param lineNumber Line number
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    /**
     * Gets the line number in the source file.
     *
     * @return Line number (-1 if unknown)
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the directory of the message flow this node belongs to.
     * Used by validators to locate external source files (e.g. the .esql module
     * for a Compute node or the .java class for a JavaCompute node).
     *
     * @param sourceDirectory Absolute path of the flow's directory
     */
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * Gets the directory of the message flow this node belongs to.
     *
     * @return Flow directory path, or null if unknown
     */
    public String getSourceDirectory() {
        return sourceDirectory;
    }
    
    /**
     * Checks if this is an input node.
     * 
     * @return true if this is an input node
     */
    public boolean isInputNode() {
        return type.endsWith("Input");
    }
    
    /**
     * Checks if this is an output node.
     * 
     * @return true if this is an output node
     */
    public boolean isOutputNode() {
        return type.endsWith("Output");
    }
    
    /**
     * Checks if this is an MQ node.
     * 
     * @return true if this is an MQ-related node
     */
    public boolean isMQNode() {
        return type.startsWith("MQ");
    }
    
    @Override
    public String toString() {
        return String.format("FlowNode[name=%s, type=%s, terminals=%d, connections=%d]",
                name, type, terminals.size(), outgoingConnections.size());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FlowNode other = (FlowNode) obj;
        return nodeId.equals(other.nodeId);
    }
    
    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
}

// Made with Bob
