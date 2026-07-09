package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validator that checks for performance issues.
 * Detects blocking operations in loops, inefficient patterns, and message size handling.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class PerformanceValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "performance.optimization";
    private static final String VALIDATOR_NAME = "Performance Validator";
    private static final String CATEGORY = "Performance";
    
    // Node types that can cause performance issues
    private static final List<String> BLOCKING_NODE_TYPES = Arrays.asList(
        "DatabaseRetrieve",
        "DatabaseInput",
        "HTTPRequest",
        "RESTRequest",
        "SOAPRequest",
        "FileRead",
        "FileWrite"
    );
    
    // Loop node types
    private static final List<String> LOOP_NODE_TYPES = Arrays.asList(
        "ForEach",
        "While",
        "Loop"
    );
    
    // Message size thresholds (in bytes)
    private static final int LARGE_MESSAGE_THRESHOLD = 10 * 1024 * 1024; // 10 MB
    private static final int WARNING_MESSAGE_THRESHOLD = 5 * 1024 * 1024; // 5 MB
    
    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        System.out.println("[PerformanceValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        // Check for blocking operations in loops
        findings.addAll(validateBlockingInLoops(node));
        
        // Check for inefficient patterns
        findings.addAll(validateInefficientPatterns(node));
        
        // Check message size handling
        findings.addAll(validateMessageSizeHandling(node));
        
        // Check for excessive transformations
        findings.addAll(validateTransformations(node));
        
        System.out.println("[PerformanceValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            System.out.println("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Validates blocking operations in loops.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateBlockingInLoops(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check if this is a blocking operation inside a loop
        if (BLOCKING_NODE_TYPES.contains(node.getType())) {
            // Check if node is inside a loop (simplified check - in real implementation,
            // would need to traverse the flow graph)
            if (isInsideLoop(node)) {
                String message = String.format(
                    "Blocking operation '%s' (type: %s) detected inside a loop. " +
                    "This can cause severe performance degradation.",
                    node.getName(),
                    node.getType()
                );
                
                String suggestion = 
                    "Avoid blocking operations inside loops. Consider:\n\n" +
                    "1. Batch Processing:\n" +
                    "   - Collect all items first\n" +
                    "   - Process in a single batch operation\n" +
                    "   - Use bulk APIs when available\n\n" +
                    "2. Asynchronous Processing:\n" +
                    "   - Use async request nodes\n" +
                    "   - Implement message aggregation\n" +
                    "   - Consider event-driven patterns\n\n" +
                    "3. Caching:\n" +
                    "   - Cache frequently accessed data\n" +
                    "   - Use shared variables for lookup data\n" +
                    "   - Implement local caching strategies\n\n" +
                    "4. Optimization:\n" +
                    "   - Move operations outside the loop if possible\n" +
                    "   - Reduce loop iterations\n" +
                    "   - Use more efficient data structures";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".blocking.in.loop",
                    Severity.HIGH,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        }
        
        // Check for nested loops
        if (LOOP_NODE_TYPES.contains(node.getType())) {
            if (isInsideLoop(node)) {
                String message = String.format(
                    "Nested loop detected at node '%s'. " +
                    "Nested loops can cause exponential performance degradation.",
                    node.getName()
                );
                
                String suggestion = 
                    "Avoid nested loops when possible:\n" +
                    "- Flatten the data structure\n" +
                    "- Use hash maps for lookups instead of inner loops\n" +
                    "- Consider alternative algorithms\n" +
                    "- Process data in a single pass if possible\n\n" +
                    "If nested loops are necessary:\n" +
                    "- Minimize iterations in inner loop\n" +
                    "- Add early exit conditions\n" +
                    "- Consider pagination or chunking";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".nested.loop",
                    Severity.MEDIUM,
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
    
    /**
     * Validates inefficient patterns.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateInefficientPatterns(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for inefficient ESQL patterns in Compute nodes
        if ("Compute".equals(node.getType()) || "JavaCompute".equals(node.getType())) {
            String code = node.getProperty("code");
            
            if (code != null) {
                // Check for string concatenation in loops
                if (code.contains("SET") && code.contains("||") && 
                    (code.contains("WHILE") || code.contains("FOR"))) {
                    
                    String message = String.format(
                        "Compute node '%s' appears to use string concatenation in a loop. " +
                        "This creates many intermediate string objects.",
                        node.getName()
                    );
                    
                    String suggestion = 
                        "Use more efficient string building techniques:\n" +
                        "- Build arrays and join at the end\n" +
                        "- Use CAST with appropriate sizing\n" +
                        "- Consider using BLOB for large text operations\n" +
                        "- Minimize string operations in loops";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".string.concatenation",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
                
                // Check for SELECT * usage
                if (code.toUpperCase().contains("SELECT *")) {
                    String message = String.format(
                        "Compute node '%s' uses 'SELECT *' which retrieves all columns. " +
                        "This can be inefficient for large tables.",
                        node.getName()
                    );
                    
                    String suggestion = 
                        "Specify only the columns you need:\n" +
                        "- Reduces data transfer\n" +
                        "- Improves query performance\n" +
                        "- Makes code more maintainable\n" +
                        "- Reduces memory usage";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".select.all",
                        Severity.LOW,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
            }
        }
        
        // Check for excessive message tree navigation
        if ("Compute".equals(node.getType())) {
            String navigationType = node.getProperty("navigationType");
            if ("XPath".equalsIgnoreCase(navigationType)) {
                String message = String.format(
                    "Compute node '%s' uses XPath navigation which can be slow for large messages.",
                    node.getName()
                );
                
                String suggestion = 
                    "Consider using ESQL field references for better performance:\n" +
                    "- Direct field access is faster than XPath\n" +
                    "- Cache navigation results if used multiple times\n" +
                    "- Use XPath only when dynamic navigation is required";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".xpath.navigation",
                    Severity.LOW,
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
    
    /**
     * Validates message size handling.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateMessageSizeHandling(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for large message handling in input nodes
        if (node.getType().contains("Input")) {
            String maxMessageSize = node.getProperty("maxMessageSize");
            
            if (maxMessageSize != null && !maxMessageSize.isEmpty()) {
                try {
                    long size = Long.parseLong(maxMessageSize);
                    
                    if (size > LARGE_MESSAGE_THRESHOLD) {
                        String message = String.format(
                            "Input node '%s' allows very large messages (%d bytes). " +
                            "This can cause memory issues and performance degradation.",
                            node.getName(),
                            size
                        );
                        
                        String suggestion = 
                            "For large messages, consider:\n\n" +
                            "1. Streaming:\n" +
                            "   - Use BLOB domain for large payloads\n" +
                            "   - Process data in chunks\n" +
                            "   - Avoid parsing entire message into memory\n\n" +
                            "2. Pagination:\n" +
                            "   - Split large messages into smaller ones\n" +
                            "   - Process in batches\n" +
                            "   - Use message aggregation\n\n" +
                            "3. External Storage:\n" +
                            "   - Store large payloads externally\n" +
                            "   - Pass references instead of content\n" +
                            "   - Use file-based processing\n\n" +
                            "4. Resource Management:\n" +
                            "   - Increase heap size if necessary\n" +
                            "   - Monitor memory usage\n" +
                            "   - Implement size limits";
                        
                        findings.add(new Finding(
                            VALIDATOR_ID + ".large.message",
                            Severity.HIGH,
                            message,
                            suggestion,
                            node,
                            node.getLineNumber(),
                            CATEGORY
                        ));
                    } else if (size > WARNING_MESSAGE_THRESHOLD) {
                        String message = String.format(
                            "Input node '%s' allows moderately large messages (%d bytes). " +
                            "Monitor memory usage and consider optimization.",
                            node.getName(),
                            size
                        );
                        
                        findings.add(new Finding(
                            VALIDATOR_ID + ".moderate.message",
                            Severity.LOW,
                            message,
                            "Consider implementing streaming or chunking for better performance.",
                            node,
                            node.getLineNumber(),
                            CATEGORY
                        ));
                    }
                } catch (NumberFormatException e) {
                    // Invalid size value
                }
            }
        }
        
        // Check for message copying
        if ("Compute".equals(node.getType())) {
            String copyMessage = node.getProperty("copyMessage");
            if ("true".equalsIgnoreCase(copyMessage) || "Yes".equalsIgnoreCase(copyMessage)) {
                String message = String.format(
                    "Compute node '%s' is configured to copy the entire message. " +
                    "This doubles memory usage.",
                    node.getName()
                );
                
                String suggestion = 
                    "Avoid copying the entire message unless necessary:\n" +
                    "- Copy only required fields\n" +
                    "- Use references when possible\n" +
                    "- Consider using shared variables for data passing\n" +
                    "- Modify message in place when appropriate";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".message.copy",
                    Severity.MEDIUM,
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
    
    /**
     * Validates transformation operations.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateTransformations(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for multiple sequential transformations
        if ("Compute".equals(node.getType()) || "Mapping".equals(node.getType())) {
            // Check if there are multiple transformation nodes in sequence
            // (simplified check - would need flow graph analysis in real implementation)
            String message = String.format(
                "Consider consolidating multiple transformations in node '%s' " +
                "to reduce message tree traversals.",
                node.getName()
            );
            
            String suggestion = 
                "Optimize transformations:\n" +
                "- Combine multiple Compute nodes into one when possible\n" +
                "- Reduce message tree traversals\n" +
                "- Cache intermediate results\n" +
                "- Use efficient ESQL patterns\n" +
                "- Consider using Mapping node for complex transformations";
            
            // This would be added only if we detect multiple sequential transformations
            // For now, it's a placeholder for the pattern
        }
        
        // Check for XSLT transformations
        if ("XSLTransform".equals(node.getType())) {
            String message = String.format(
                "XSLT transformation node '%s' can be resource-intensive. " +
                "Consider alternatives for better performance.",
                node.getName()
            );
            
            String suggestion = 
                "XSLT transformations can be slow. Consider:\n" +
                "- Using Compute node with ESQL for simple transformations\n" +
                "- Using Mapping node for complex transformations\n" +
                "- Caching compiled XSLT stylesheets\n" +
                "- Optimizing XSLT code\n" +
                "- Using Java for complex logic";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".xslt.performance",
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
     * Checks if a node is inside a loop.
     * This is a simplified check - real implementation would need flow graph analysis.
     * 
     * @param node Node to check
     * @return true if node appears to be inside a loop
     */
    private boolean isInsideLoop(FlowNode node) {
        // Simplified check - look for loop indicators in node properties or context
        String context = node.getProperty("context");
        String parentType = node.getProperty("parentType");
        
        return (context != null && context.contains("loop")) ||
               (parentType != null && LOOP_NODE_TYPES.contains(parentType));
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
        return "Detects performance issues including blocking operations in loops, " +
               "inefficient patterns, excessive message size handling, and " +
               "suboptimal transformations. Helps identify bottlenecks and " +
               "optimization opportunities.";
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
        // This validator applies to many node types
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled);
    }
}

// Made with Bob