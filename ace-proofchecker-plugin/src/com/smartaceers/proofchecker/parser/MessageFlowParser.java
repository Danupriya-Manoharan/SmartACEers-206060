package com.smartaceers.proofchecker.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for ACE message flow XML files.
 * Extracts nodes, connections, and properties from .msgflow files.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class MessageFlowParser {
    
    private static final Logger LOGGER = Logger.getLogger(MessageFlowParser.class.getName());

    /**
     * Maps ACE implementation node-type names onto the canonical type names the
     * validators recognize. ACE's legacy SOAP-over-HTTP nodes are named
     * WSInput/WSReply/WSRequest (from ComIbmWSInput/Reply/Request) but are
     * functionally SOAP nodes, and the *AsyncRequest variants are validated like
     * their synchronous counterparts. Without this mapping those nodes resolve
     * to a type no validator applies to, so they are silently skipped.
     */
    private static final Map<String, String> NODE_TYPE_ALIASES = new HashMap<>();
    static {
        NODE_TYPE_ALIASES.put("WSRequest", "SOAPRequest");
        NODE_TYPE_ALIASES.put("WSInput", "SOAPInput");
        NODE_TYPE_ALIASES.put("WSReply", "SOAPReply");
        NODE_TYPE_ALIASES.put("SOAPAsyncRequest", "SOAPRequest");
        NODE_TYPE_ALIASES.put("HTTPAsyncRequest", "HTTPRequest");
    }

    /**
     * Node types that expose a 'catch' terminal in ACE.
     * These are the terminals a node *can* have; whether they are connected is
     * determined separately from the parsed connections. This is what allows
     * validators to detect unconnected catch terminals.
     */
    private static final Set<String> CATCH_TERMINAL_NODE_TYPES = new HashSet<>(Arrays.asList(
        "MQInput", "HTTPInput", "SOAPInput", "FileInput", "TCPIPServerInput",
        "Compute", "JavaCompute", "Mapping", "XSLTransform", "Filter",
        "Database", "DatabaseInput", "DatabaseRetrieve", "DatabaseRoute",
        "HTTPRequest", "RESTRequest", "RESTAsyncRequest", "SOAPRequest"
    ));

    /**
     * Node types that expose a 'failure' terminal in ACE.
     */
    private static final Set<String> FAILURE_TERMINAL_NODE_TYPES = new HashSet<>(Arrays.asList(
        "MQInput", "MQOutput", "MQGet", "HTTPInput", "HTTPRequest", "HTTPReply",
        "SOAPInput", "SOAPRequest", "SOAPReply", "RESTRequest", "RESTAsyncRequest",
        "Database", "DatabaseInput", "DatabaseRetrieve", "DatabaseRoute",
        "Compute", "JavaCompute", "Mapping", "XSLTransform",
        "FileInput", "FileRead", "FileOutput", "FileWrite"
    ));

    /**
     * Parses an ACE message flow file.
     * 
     * @param flowFilePath Path to the .msgflow file
     * @return List of flow nodes
     * @throws Exception if parsing fails
     */
    public List<FlowNode> parse(String flowFilePath) throws Exception {
        if (flowFilePath == null || flowFilePath.isEmpty()) {
            throw new IllegalArgumentException("Flow file path cannot be null or empty");
        }
        
        File flowFile = new File(flowFilePath);
        if (!flowFile.exists()) {
            throw new IOException("Flow file not found: " + flowFilePath);
        }
        
        LOGGER.info("Parsing message flow: " + flowFilePath);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(flowFile);
            document.getDocumentElement().normalize();
            
            List<FlowNode> nodes = parseNodes(document);
            parseConnections(document, nodes);

            // Record the flow's directory on each node so validators can locate
            // external source files (e.g. the .esql / .java for Compute nodes).
            String flowDirectory = flowFile.getAbsoluteFile().getParent();
            for (FlowNode node : nodes) {
                node.setSourceDirectory(flowDirectory);
            }

            LOGGER.info("Successfully parsed " + nodes.size() + " nodes");
            return nodes;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing message flow: " + flowFilePath, e);
            throw new Exception("Failed to parse message flow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parses nodes from the XML document.
     * 
     * @param document XML document
     * @return List of flow nodes
     */
    private List<FlowNode> parseNodes(Document document) {
        List<FlowNode> nodes = new ArrayList<>();
        Map<String, FlowNode> nodeMap = new HashMap<>();
        
        // Parse composition nodes (the actual flow nodes)
        NodeList compositionNodes = document.getElementsByTagName("composition");
        
        for (int i = 0; i < compositionNodes.getLength(); i++) {
            Node compositionNode = compositionNodes.item(i);
            
            if (compositionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element compositionElement = (Element) compositionNode;
                
                // Get nodes within composition
                NodeList nodeElements = compositionElement.getElementsByTagName("nodes");
                
                for (int j = 0; j < nodeElements.getLength(); j++) {
                    Node nodeElement = nodeElements.item(j);
                    
                    if (nodeElement.getNodeType() == Node.ELEMENT_NODE) {
                        FlowNode flowNode = parseNode((Element) nodeElement);
                        if (flowNode != null) {
                            nodes.add(flowNode);
                            nodeMap.put(flowNode.getNodeId(), flowNode);
                        }
                    }
                }
            }
        }
        
        return nodes;
    }
    
    /**
     * Parses a single node element.
     * 
     * @param nodeElement XML element representing a node
     * @return FlowNode object
     */
    private FlowNode parseNode(Element nodeElement) {
        try {
            // Get node attributes
            String nodeId = getAttributeValue(nodeElement, "xmi:id", "");
            
            // Get node name with comprehensive fallback logic
            String nodeName = getAttributeValue(nodeElement, "name", "");
            
            if (nodeName.isEmpty()) {
                nodeName = getAttributeValue(nodeElement, "label", "");
            }
            
            if (nodeName.isEmpty()) {
                nodeName = getAttributeValue(nodeElement, "nodeName", "");
            }
            
            if (nodeName.isEmpty()) {
                nodeName = getAttributeValue(nodeElement, "displayName", "");
            }
            
            String nodeType = getAttributeValue(nodeElement, "xmi:type", "Unknown");
            
            // Extract node type from namespace (e.g., "ComIbmMQInput.msgnode:FCMComposite_1" -> "MQInput")
            String extractedType = extractNodeType(nodeType);

            // Normalize implementation-specific type names (e.g. WSRequest -> SOAPRequest)
            // so the validators recognize them.
            extractedType = NODE_TYPE_ALIASES.getOrDefault(extractedType, extractedType);
            
            // If still no name, use node type as fallback
            if (nodeName.isEmpty()) {
                nodeName = extractedType + "_" + nodeId.substring(Math.max(0, nodeId.length() - 8));
                LOGGER.warning("No name found for node " + nodeId + ", using generated name: " + nodeName);
            }
            
            LOGGER.fine("Raw xmi:type: " + nodeType + " -> Extracted type: " + extractedType + ", Name: " + nodeName);
            
            // Create flow node
            FlowNode flowNode = new FlowNode(nodeName, extractedType, nodeId);

            // Parse node properties
            parseNodeProperties(nodeElement, flowNode);

            // Map ACE-specific attribute names onto the canonical property names
            // the validators look up (e.g. timeoutForServer -> timeout,
            // webServiceURL -> url) so the checks work on real .msgflow files.
            addCanonicalAliases(flowNode);

            // Register the terminals this node type is capable of having.
            // Connections will mark which of these are actually connected, so
            // validators can detect terminals that exist but are left unconnected.
            addDefaultTerminals(flowNode);

            // Set line number if available
            Object lineNumber = nodeElement.getUserData("lineNumber");
            if (lineNumber instanceof Integer) {
                flowNode.setLineNumber((Integer) lineNumber);
            }
            
            LOGGER.fine("Parsed node: " + nodeName + " (type: " + extractedType + ")");
            
            return flowNode;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing node element", e);
            return null;
        }
    }
    
    /**
     * Parses properties for a node.
     * 
     * @param nodeElement XML element representing a node
     * @param flowNode FlowNode to populate with properties
     */
    private void parseNodeProperties(Element nodeElement, FlowNode flowNode) {
        // Get all attributes as properties
        org.w3c.dom.NamedNodeMap attributes = nodeElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            
            // Skip XMI attributes
            if (!attrName.startsWith("xmi:")) {
                flowNode.setProperty(attrName, attrValue);
            }
        }
        
        // Parse specific properties based on node type
        String nodeType = flowNode.getType();
        if (nodeType.equals("MQInput") || nodeType.equals("ComIbmMQInput")) {
            parseMQInputProperties(nodeElement, flowNode);
        }
    }

    /**
     * Maps ACE-specific attribute names onto the canonical property names the
     * validators read. ACE stores, for example, the request timeout under
     * {@code timeoutForServer} and the endpoint under {@code webServiceURL} /
     * {@code requestURL}, whereas the validators look up {@code timeout} and
     * {@code url}. Without this mapping those checks silently never fire on real
     * message flows.
     *
     * @param flowNode FlowNode whose canonical properties should be populated
     */
    private void addCanonicalAliases(FlowNode flowNode) {
        // Request/response timeout -> "timeout"
        aliasProperty(flowNode, "timeout",
            "timeout", "timeoutForServer", "requestTimeout", "responseTimeout",
            "timeoutInSeconds", "timeoutForTheServer");

        // Endpoint URL -> "url"
        aliasProperty(flowNode, "url",
            "url", "requestURL", "webServiceURL", "URLSpecifier", "baseURL",
            "webServiceURLToOverride");
    }

    /**
     * Copies the first matching source property to a canonical property name,
     * unless the canonical property is already set. If none of the explicit
     * candidates are present, falls back to any property whose name contains the
     * canonical key (case-insensitive) - e.g. "timeoutForServer" satisfies
     * "timeout" and "webServiceURL" satisfies "url".
     *
     * @param flowNode   Node to update
     * @param canonical  Canonical property name the validators read
     * @param candidates Source property names to try, in priority order
     */
    private void aliasProperty(FlowNode flowNode, String canonical, String... candidates) {
        String existing = flowNode.getProperty(canonical);
        if (existing != null && !existing.isEmpty()) {
            return;
        }

        for (String candidate : candidates) {
            String value = flowNode.getProperty(candidate);
            if (value != null && !value.isEmpty()) {
                flowNode.setProperty(canonical, value);
                return;
            }
        }

        // Fallback: any property whose name contains the canonical key.
        String lowerKey = canonical.toLowerCase();
        for (Map.Entry<String, String> entry : flowNode.getProperties().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name != null && value != null && !value.isEmpty()
                    && name.toLowerCase().contains(lowerKey)) {
                flowNode.setProperty(canonical, value);
                return;
            }
        }
    }

    /**
     * Registers the terminals that a node type is capable of exposing.
     * Terminals are registered as capabilities of the node; connection parsing
     * later marks which terminals are actually connected. This separation is
     * what lets validators flag terminals that exist but are not connected
     * (e.g. an unconnected catch terminal).
     *
     * @param flowNode FlowNode to populate with its standard terminals
     */
    private void addDefaultTerminals(FlowNode flowNode) {
        String nodeType = flowNode.getType();
        if (nodeType == null) {
            return;
        }

        // Virtually every processing/input/output node exposes an 'out' terminal.
        flowNode.addTerminal("out");

        if (CATCH_TERMINAL_NODE_TYPES.contains(nodeType)) {
            flowNode.addTerminal("catch");
        }

        if (FAILURE_TERMINAL_NODE_TYPES.contains(nodeType)) {
            flowNode.addTerminal("failure");
        }
    }
    
    /**
     * Extracts the actual node type from the xmi:type attribute.
     * Handles formats like "ComIbmMQInput.msgnode:FCMComposite_1" -> "MQInput"
     *
     * @param xmiType The xmi:type attribute value
     * @return Extracted node type
     */
    private String extractNodeType(String xmiType) {
        if (xmiType == null || xmiType.isEmpty()) {
            return "Unknown";
        }
        
        // Handle format: "ComIbmMQInput.msgnode:FCMComposite_1"
        // Extract "ComIbmMQInput" from the namespace prefix
        if (xmiType.contains(".msgnode:")) {
            String prefix = xmiType.substring(0, xmiType.indexOf(".msgnode:"));
            // Remove "ComIbm" prefix to get node type
            if (prefix.startsWith("ComIbm")) {
                return prefix.substring(6); // Remove "ComIbm"
            }
            return prefix;
        }
        
        // Handle simple format with colon separator
        if (xmiType.contains(":")) {
            String beforeColon = xmiType.substring(0, xmiType.indexOf(":"));
            if (beforeColon.contains(".")) {
                beforeColon = beforeColon.substring(0, beforeColon.indexOf("."));
            }
            if (beforeColon.startsWith("ComIbm")) {
                return beforeColon.substring(6);
            }
            return beforeColon;
        }
        
        // Return as-is if no special format detected
        return xmiType;
    }
    
    /**
     * Parses MQ Input specific properties.
     * 
     * @param nodeElement XML element
     * @param flowNode FlowNode to populate
     */
    private void parseMQInputProperties(Element nodeElement, FlowNode flowNode) {
        // Look for transaction mode property
        String transactionMode = getAttributeValue(nodeElement, "transactionMode", "");
        if (!transactionMode.isEmpty()) {
            flowNode.setProperty("transactionMode", transactionMode);
        }
        
        // Look for queue name
        String queueName = getAttributeValue(nodeElement, "queueName", "");
        if (!queueName.isEmpty()) {
            flowNode.setProperty("queueName", queueName);
        }
        
        // Look for connection property
        String connection = getAttributeValue(nodeElement, "connection", "");
        if (!connection.isEmpty()) {
            flowNode.setProperty("connection", connection);
        }
    }
    
    /**
     * Parses connections between nodes.
     * 
     * @param document XML document
     * @param nodes List of flow nodes
     */
    private void parseConnections(Document document, List<FlowNode> nodes) {
        Map<String, FlowNode> nodeMap = new HashMap<>();
        for (FlowNode node : nodes) {
            nodeMap.put(node.getNodeId(), node);
        }
        
        // Parse connections
        NodeList connectionElements = document.getElementsByTagName("connections");
        
        for (int i = 0; i < connectionElements.getLength(); i++) {
            Node connectionNode = connectionElements.item(i);
            
            if (connectionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element connectionElement = (Element) connectionNode;
                parseConnection(connectionElement, nodeMap);
            }
        }
    }
    
    /**
     * Parses a single connection element.
     * 
     * @param connectionElement XML element representing a connection
     * @param nodeMap Map of node IDs to FlowNode objects
     */
    private void parseConnection(Element connectionElement, Map<String, FlowNode> nodeMap) {
        try {
            // ACE .msgflow files use sourceNode/targetNode and
            // sourceTerminalName/targetTerminalName (e.g. "OutTerminal.catch").
            // Fall back to the simplified source/target/sourceTerminal names so
            // both formats are supported.
            String sourceNodeRef = firstNonEmpty(
                getAttributeValue(connectionElement, "sourceNode", ""),
                getAttributeValue(connectionElement, "source", ""));
            String targetNodeRef = firstNonEmpty(
                getAttributeValue(connectionElement, "targetNode", ""),
                getAttributeValue(connectionElement, "target", ""));
            String sourceTerminal = normalizeTerminalName(firstNonEmpty(
                getAttributeValue(connectionElement, "sourceTerminalName", ""),
                getAttributeValue(connectionElement, "sourceTerminal", ""),
                "out"));
            String targetTerminal = normalizeTerminalName(firstNonEmpty(
                getAttributeValue(connectionElement, "targetTerminalName", ""),
                getAttributeValue(connectionElement, "targetTerminal", ""),
                "in"));

            // Find source and target nodes
            FlowNode sourceNode = findNodeByReference(sourceNodeRef, nodeMap);
            FlowNode targetNode = findNodeByReference(targetNodeRef, nodeMap);
            
            if (sourceNode != null && targetNode != null) {
                // Automatically add terminals discovered from connections
                sourceNode.addTerminal(sourceTerminal);
                targetNode.addTerminal(targetTerminal);
                
                FlowConnection connection = new FlowConnection(
                    sourceNode.getName(),
                    sourceTerminal,
                    targetNode.getName(),
                    targetTerminal
                );
                
                sourceNode.addOutgoingConnection(connection);
                targetNode.addIncomingConnection(connection);
                
                LOGGER.fine("Parsed connection: " + connection);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing connection element", e);
        }
    }
    
    /**
     * Finds a node by its reference (ID or XPath).
     * 
     * @param reference Node reference
     * @param nodeMap Map of node IDs to FlowNode objects
     * @return FlowNode, or null if not found
     */
    private FlowNode findNodeByReference(String reference, Map<String, FlowNode> nodeMap) {
        if (reference == null || reference.isEmpty()) {
            return null;
        }
        
        // Direct ID lookup
        if (nodeMap.containsKey(reference)) {
            return nodeMap.get(reference);
        }
        
        // Try to extract ID from XPath-style reference
        if (reference.contains("@")) {
            String id = reference.substring(reference.lastIndexOf("@") + 1);
            return nodeMap.get(id);
        }
        
        return null;
    }
    
    /**
     * Gets an attribute value from an element.
     * 
     * @param element XML element
     * @param attributeName Attribute name
     * @param defaultValue Default value if attribute not found
     * @return Attribute value
     */
    private String getAttributeValue(Element element, String attributeName, String defaultValue) {
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        return defaultValue;
    }

    /**
     * Returns the first non-empty value from the supplied candidates.
     *
     * @param values Candidate values in priority order
     * @return First non-null, non-empty value, or "" if none
     */
    private String firstNonEmpty(String... values) {
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return "";
    }

    /**
     * Normalizes an ACE terminal name to its short form.
     * ACE qualifies terminal names (e.g. "OutTerminal.catch", "InTerminal.in");
     * validators compare against short names such as "catch", "failure", "out"
     * and "in", so the qualifying prefix is stripped here.
     *
     * @param terminalName Raw terminal name
     * @return Normalized short terminal name
     */
    private String normalizeTerminalName(String terminalName) {
        if (terminalName == null || terminalName.isEmpty()) {
            return terminalName;
        }
        int lastDot = terminalName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < terminalName.length() - 1) {
            return terminalName.substring(lastDot + 1);
        }
        return terminalName;
    }
    
    /**
     * Validates that a file is a message flow file.
     * 
     * @param filePath File path to validate
     * @return true if the file is a .msgflow file
     */
    public boolean isMessageFlowFile(String filePath) {
        return filePath != null && filePath.toLowerCase().endsWith(".msgflow");
    }
}

// Made with Bob
