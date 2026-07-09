package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import com.smartaceers.proofchecker.utils.ValidationLogger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Validator that checks for best practices.
 * Validates flow structure, error propagation, and proper logging.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class BestPracticesValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "best.practices";
    private static final String VALIDATOR_NAME = "Best Practices Validator";
    private static final String CATEGORY = "Error Handling";
    
    // Node types that should have logging
    private static final List<String> LOGGING_RECOMMENDED_TYPES = Arrays.asList(
        "HTTPInput", "MQInput", "DatabaseInput", "FileInput",
        "HTTPRequest", "RESTRequest", "SOAPRequest"
    );

    // IF-nesting depth at which the external source is flagged (>= this value)
    private static final int MAX_IF_NESTING = 4;

    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // List all node properties for debugging
        listNodeProperties(node);
        
        // Check flow structure
        findings.addAll(validateFlowStructure(node));
        
        // Check error propagation
        findings.addAll(validateErrorPropagation(node));
        
        // Check logging practices
        //findings.addAll(validateLogging(node));
        
        // Check documentation
        findings.addAll(validateDocumentation(node));
        
        // Check resource management
        findings.addAll(validateResourceManagement(node));

        // Check nested IF depth in the external ESQL/Java source
        findings.addAll(validateExternalSourceNesting(node));

        // List all findings with suggestions
        listFindingsWithSuggestions(node, findings);
        
        return findings;
    }
    
    /**
     * Lists all properties of a node for debugging purposes.
     * Prints property name and value to System.out.
     *
     * @param node Node whose properties to list
     */
    private void listNodeProperties(FlowNode node) {
        ValidationLogger.log("========================================");
        ValidationLogger.log("Node Properties for: " + node.getName());
        ValidationLogger.log("Node Type: " + node.getType());
        ValidationLogger.log("----------------------------------------");
        
        // Get all properties from the node
        java.util.Map<String, String> properties = node.getProperties();
        
        if (properties == null || properties.isEmpty()) {
            ValidationLogger.log("  (No properties found)");
        } else {
            // Sort properties by name for easier reading
            java.util.List<String> sortedKeys = new java.util.ArrayList<>(properties.keySet());
            java.util.Collections.sort(sortedKeys);
            
            for (String propertyName : sortedKeys) {
                String propertyValue = properties.get(propertyName);
                
                // Truncate very long values for readability
                if (propertyValue != null && propertyValue.length() > 100) {
                    propertyValue = propertyValue.substring(0, 97) + "...";
                }
                
                ValidationLogger.log("  " + propertyName + " = " + propertyValue);
            }
        }
        
        ValidationLogger.log("========================================");
        ValidationLogger.log("");
    }
    
    /**
     * Lists all findings with their suggestions for debugging purposes.
     * Prints finding details and suggestions to System.out.
     *
     * @param node Node that was validated
     * @param findings List of findings for this node
     */
    private void listFindingsWithSuggestions(FlowNode node, List<Finding> findings) {
        if (findings.isEmpty()) {
            ValidationLogger.log(">>> No findings for node: " + node.getName());
            ValidationLogger.log("");
            return;
        }
        
        ValidationLogger.log("========================================");
        ValidationLogger.log("Findings for Node: " + node.getName());
        ValidationLogger.log("Node Type: " + node.getType());
        ValidationLogger.log("Total Findings: " + findings.size());
        ValidationLogger.log("========================================");
        
        for (int i = 0; i < findings.size(); i++) {
            Finding finding = findings.get(i);
            
            ValidationLogger.log("\n--- Finding #" + (i + 1) + " ---");
            ValidationLogger.log("Rule ID: " + finding.getRuleId());
            ValidationLogger.log("Severity: " + finding.getSeverity().getDisplayName());
            ValidationLogger.log("Category: " + finding.getCategory());
            ValidationLogger.log("\nMessage:");
            ValidationLogger.log("  " + finding.getMessage());
            
            if (finding.getSuggestion() != null && !finding.getSuggestion().isEmpty()) {
                ValidationLogger.log("\nSuggestion:");
                // Print suggestion with indentation for readability
                String[] suggestionLines = finding.getSuggestion().split("\n");
                for (String line : suggestionLines) {
                    ValidationLogger.log("  " + line);
                }
            } else {
                ValidationLogger.log("\nSuggestion: (none)");
            }
        }
        
        ValidationLogger.log("\n========================================");
        ValidationLogger.log("");
    }
    
    /**
     * Validates flow structure best practices.
     *
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateFlowStructure(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for overly complex flows
        if ("Compute".equals(node.getType()) || "JavaCompute".equals(node.getType())) {
            String code = node.getProperty("code");
            
            if (code != null) {
                int lineCount = code.split("\n").length;
                
                if (lineCount > 200) {
                    String message = String.format(
                        "Node '%s' contains %d lines of code. " +
                        "This is too complex for a single node.",
                        node.getName(),
                        lineCount
                    );
                    
                    String suggestion = 
                        "Break down complex logic into smaller, manageable pieces:\n\n" +
                        "1. Use Subflows:\n" +
                        "   - Extract reusable logic\n" +
                        "   - Improve maintainability\n" +
                        "   - Enable testing of individual components\n\n" +
                        "2. Separate Concerns:\n" +
                        "   - Validation in one node\n" +
                        "   - Transformation in another\n" +
                        "   - Business logic separately\n\n" +
                        "3. Benefits:\n" +
                        "   - Easier to understand\n" +
                        "   - Easier to test\n" +
                        "   - Easier to maintain\n" +
                        "   - Better reusability\n\n" +
                        "Recommended: Keep nodes under 100 lines of code";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".complex.node",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
                
                // Check for deep nesting
                int maxNesting = calculateMaxNesting(code);
                if (maxNesting > 4) {
                    String message = String.format(
                        "Node '%s' has deeply nested code (nesting level: %d). " +
                        "This reduces readability and maintainability.",
                        node.getName(),
                        maxNesting
                    );
                    
                    String suggestion = 
                        "Reduce code nesting:\n" +
                        "- Use early returns/exits\n" +
                        "- Extract nested logic to functions\n" +
                        "- Simplify conditional logic\n" +
                        "- Use guard clauses\n\n" +
                        "Example:\n" +
                        "Instead of:\n" +
                        "  IF condition1 THEN\n" +
                        "    IF condition2 THEN\n" +
                        "      IF condition3 THEN\n" +
                        "        -- do something\n" +
                        "Use:\n" +
                        "  IF NOT condition1 THEN RETURN; END IF;\n" +
                        "  IF NOT condition2 THEN RETURN; END IF;\n" +
                        "  IF NOT condition3 THEN RETURN; END IF;\n" +
                        "  -- do something";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".deep.nesting",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
            }
        }
        
        // Check for proper use of subflows
        if ("Subflow".equals(node.getType())) {
            String subflowName = node.getProperty("subflowName");
            
            if (subflowName == null || subflowName.isEmpty()) {
                String message = String.format(
                    "Subflow node '%s' does not reference a subflow. " +
                    "This will cause runtime errors.",
                    node.getName()
                );
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".missing.subflow",
                    Severity.CRITICAL,
                    message,
                    "Configure the subflow reference in the node properties.",
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        }
        
        return findings;
    }
    
    /**
     * Validates error propagation best practices.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateErrorPropagation(FlowNode node) {
        List<Finding> findings = new ArrayList<>();

        // Catch-terminal checks for input nodes are handled exclusively by
        // CatchTerminalValidator (MQInput / HTTPInput / FileInput).

        // Check for error swallowing
        if ("TryCatch".equals(node.getType())) {
            String catchAction = node.getProperty("catchAction");
            
            if ("ignore".equalsIgnoreCase(catchAction) || "suppress".equalsIgnoreCase(catchAction)) {
                String message = String.format(
                    "Node '%s' is configured to ignore/suppress errors. " +
                    "This can hide problems and make debugging difficult.",
                    node.getName()
                );
                
                String suggestion = 
                    "Avoid swallowing errors:\n" +
                    "- Always log errors, even if handled\n" +
                    "- Provide context for debugging\n" +
                    "- Consider if error should be propagated\n" +
                    "- Document why errors are suppressed\n\n" +
                    "If errors must be suppressed:\n" +
                    "- Log at appropriate level\n" +
                    "- Include business justification\n" +
                    "- Monitor suppressed errors\n" +
                    "- Review periodically";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".error.swallowing",
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
     * Validates logging best practices.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateLogging(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check if logging is present for recommended node types
        if (LOGGING_RECOMMENDED_TYPES.contains(node.getType())) {
            boolean hasLogging = hasLoggingConfigured(node);
            
            if (!hasLogging) {
                String message = String.format(
                    "Node '%s' (type: %s) does not have logging configured. " +
                    "This makes debugging and monitoring difficult.",
                    node.getName(),
                    node.getType()
                );
                
                String suggestion = 
                    "Implement proper logging:\n\n" +
                    "1. Entry Points:\n" +
                    "   - Log incoming requests\n" +
                    "   - Include correlation ID\n" +
                    "   - Log key parameters\n" +
                    "   - Don't log sensitive data\n\n" +
                    "2. Processing:\n" +
                    "   - Log important decisions\n" +
                    "   - Log external calls\n" +
                    "   - Log transformations\n" +
                    "   - Use appropriate log levels\n\n" +
                    "3. Exit Points:\n" +
                    "   - Log responses\n" +
                    "   - Log processing time\n" +
                    "   - Log success/failure\n\n" +
                    "4. Best Practices:\n" +
                    "   - Use structured logging\n" +
                    "   - Include context\n" +
                    "   - Use correlation IDs\n" +
                    "   - Don't log sensitive data\n" +
                    "   - Use appropriate log levels";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".missing.logging",
                    Severity.LOW,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        }
        
        // Check for excessive logging
        if ("Trace".equals(node.getType()) || "Log".equals(node.getType())) {
            String logLevel = node.getProperty("logLevel");
            
            if ("DEBUG".equalsIgnoreCase(logLevel) || "TRACE".equalsIgnoreCase(logLevel)) {
                String message = String.format(
                    "Logging node '%s' uses DEBUG/TRACE level. " +
                    "This may impact performance in production.",
                    node.getName()
                );
                
                String suggestion = 
                    "Use appropriate log levels:\n" +
                    "- ERROR: For errors requiring attention\n" +
                    "- WARN: For potential issues\n" +
                    "- INFO: For important events\n" +
                    "- DEBUG: For development only\n" +
                    "- TRACE: For detailed debugging\n\n" +
                    "In production:\n" +
                    "- Use INFO or higher\n" +
                    "- Enable DEBUG only for troubleshooting\n" +
                    "- Consider performance impact\n" +
                    "- Monitor log volume";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".excessive.logging",
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
     * Validates documentation best practices.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateDocumentation(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        String description = node.getProperty("description");
        String comments = node.getProperty("comments");
        
        // Check for complex nodes without documentation
        if (("Compute".equals(node.getType()) || "JavaCompute".equals(node.getType())) &&
            (description == null || description.trim().isEmpty()) &&
            (comments == null || comments.trim().isEmpty())) {
            
            String message = String.format(
                "Complex node '%s' has no documentation. " +
                "This makes maintenance difficult.",
                node.getName()
            );
            
            String suggestion = 
                "Document complex nodes:\n" +
                "- Explain the purpose\n" +
                "- Describe the logic\n" +
                "- Note any assumptions\n" +
                "- Document edge cases\n" +
                "- Include examples if helpful\n\n" +
                "Good documentation:\n" +
                "- Helps future maintainers\n" +
                "- Reduces onboarding time\n" +
                "- Prevents misunderstandings\n" +
                "- Serves as design documentation";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".missing.documentation",
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
     * Validates resource management best practices.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateResourceManagement(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for proper connection cleanup
        if ("DatabaseRetrieve".equals(node.getType()) || "DatabaseInput".equals(node.getType())) {
            String autoCommit = node.getProperty("autoCommit");
            
            if ("false".equalsIgnoreCase(autoCommit)) {
                String message = String.format(
                    "Database node '%s' has autoCommit disabled. " +
                    "Ensure proper transaction management and cleanup.",
                    node.getName()
                );
                
                String suggestion = 
                    "When managing transactions manually:\n" +
                    "- Always commit or rollback\n" +
                    "- Handle errors properly\n" +
                    "- Close connections in finally blocks\n" +
                    "- Set appropriate timeout\n" +
                    "- Monitor for connection leaks\n\n" +
                    "Consider:\n" +
                    "- Using autoCommit for simple operations\n" +
                    "- Implementing proper error handling\n" +
                    "- Testing rollback scenarios";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".manual.transaction",
                    Severity.LOW,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        }
        
        // Check for file operations without proper cleanup
        if ("FileRead".equals(node.getType()) || "FileWrite".equals(node.getType())) {
            String closeFile = node.getProperty("closeFile");
            
            if (!"true".equalsIgnoreCase(closeFile) && !"Yes".equalsIgnoreCase(closeFile)) {
                String message = String.format(
                    "File operation node '%s' may not close files properly. " +
                    "This can cause resource leaks.",
                    node.getName()
                );
                
                String suggestion = 
                    "Always close file handles:\n" +
                    "- Prevents resource leaks\n" +
                    "- Releases file locks\n" +
                    "- Ensures data is flushed\n" +
                    "- Allows other processes to access files\n\n" +
                    "Best practices:\n" +
                    "- Close in finally blocks\n" +
                    "- Handle errors during close\n" +
                    "- Use try-with-resources when possible\n" +
                    "- Monitor open file handles";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".resource.leak",
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
     * Calculates maximum nesting level in code.
     * Simplified implementation - counts braces/keywords.
     * 
     * @param code Code to analyze
     * @return Maximum nesting level
     */
    private int calculateMaxNesting(String code) {
        int maxNesting = 0;
        int currentNesting = 0;
        
        String[] lines = code.split("\n");
        for (String line : lines) {
            String trimmed = line.trim().toUpperCase();
            
            // Increase nesting
            if (trimmed.startsWith("IF ") || trimmed.startsWith("WHILE ") || 
                trimmed.startsWith("FOR ") || trimmed.startsWith("LOOP ") ||
                trimmed.contains("BEGIN")) {
                currentNesting++;
                maxNesting = Math.max(maxNesting, currentNesting);
            }
            
            // Decrease nesting
            if (trimmed.startsWith("END IF") || trimmed.startsWith("END WHILE") ||
                trimmed.startsWith("END FOR") || trimmed.startsWith("END LOOP") ||
                trimmed.equals("END;")) {
                currentNesting--;
            }
        }

        return maxNesting;
    }

    /**
     * Checks the external source of a Compute (ESQL) or JavaCompute (Java) node
     * for deeply nested IF conditions. A real ACE message flow does not store the
     * implementation inline - the node references an external .esql module or a
     * .java class - so this loads that file from the flow's directory and flags
     * the node when the IF-nesting depth reaches {@link #MAX_IF_NESTING}.
     *
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateExternalSourceNesting(FlowNode node) {
        List<Finding> findings = new ArrayList<>();

        String type = node.getType();
        boolean isCompute = "Compute".equals(type);
        boolean isJava = "JavaCompute".equals(type);
        if (!isCompute && !isJava) {
            return findings;
        }

        String directory = node.getSourceDirectory();
        if (directory == null || directory.isEmpty()) {
            return findings;
        }

        SourceFile source = isCompute ? findEsqlSource(node, directory)
                                      : findJavaSource(node, directory);
        if (source == null || source.content == null || source.content.isEmpty()) {
            // No external source file present - nothing to analyze.
            return findings;
        }

        int nesting = isCompute ? calculateEsqlIfNesting(source.content)
                                : calculateJavaIfNesting(source.content);

        ValidationLogger.log("[BestPracticesValidator] " + type + " node '" + node.getName()
                + "' source '" + source.name + "' max IF nesting = " + nesting);

        if (nesting >= MAX_IF_NESTING) {
            String language = isCompute ? "ESQL" : "Java";
            String message = String.format(
                "Node '%s' has deeply nested IF conditions (nesting depth %d) in its %s source '%s'. " +
                "%d or more nested IF levels are hard to read, test, and maintain.",
                node.getName(), nesting, language, source.name, MAX_IF_NESTING
            );

            String suggestion =
                "Reduce IF nesting to fewer than " + MAX_IF_NESTING + " levels:\n" +
                "- Use guard clauses / early RETURN to handle edge cases first\n" +
                "- Combine related conditions with AND/OR\n" +
                "- Extract nested logic into separate procedures/functions (ESQL) " +
                "or private methods (Java)\n" +
                "- Replace deep conditionals with a routing or lookup approach where possible";

            findings.add(new Finding(
                VALIDATOR_ID + ".nested.if",
                Severity.HIGH,
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
     * Holds the name and content of a resolved external source file.
     */
    private static final class SourceFile {
        final String name;
        final String content;

        SourceFile(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    /**
     * Locates the ESQL source for a Compute node within the flow directory.
     * Resolution order: the module named by the node's computeExpression (or the
     * node name) found inside any .esql file; a file named &lt;module&gt;.esql or
     * &lt;nodeName&gt;.esql; otherwise the only .esql file in the directory.
     *
     * @param node      Compute node
     * @param directory Flow directory
     * @return Resolved source, or null if none found
     */
    private SourceFile findEsqlSource(FlowNode node, String directory) {
        File dir = new File(directory);
        File[] esqlFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".esql"));
        if (esqlFiles == null || esqlFiles.length == 0) {
            return null;
        }

        String moduleName = extractEsqlModuleName(node);

        // 1. The module declared somewhere inside an .esql file.
        if (moduleName != null && !moduleName.isEmpty()) {
            for (File file : esqlFiles) {
                String content = readFileQuietly(file);
                if (content == null) {
                    continue;
                }
                String moduleBody = extractEsqlModule(content, moduleName);
                if (moduleBody != null) {
                    return new SourceFile(file.getName(), moduleBody);
                }
            }
            // 2. A file literally named after the module.
            SourceFile byName = readNamedFile(esqlFiles, moduleName + ".esql");
            if (byName != null) {
                return byName;
            }
        }

        // 3. A file named after the node.
        SourceFile byNode = readNamedFile(esqlFiles, node.getName() + ".esql");
        if (byNode != null) {
            return byNode;
        }

        // 4. The single .esql file in the directory.
        if (esqlFiles.length == 1) {
            String content = readFileQuietly(esqlFiles[0]);
            if (content != null) {
                return new SourceFile(esqlFiles[0].getName(), content);
            }
        }

        return null;
    }

    /**
     * Locates the Java source for a JavaCompute node by its class name.
     *
     * @param node      JavaCompute node
     * @param directory Flow directory (searched recursively)
     * @return Resolved source, or null if none found
     */
    private SourceFile findJavaSource(FlowNode node, String directory) {
        String javaClass = node.getProperty("javaClass");
        String simpleName = null;
        if (javaClass != null && !javaClass.isEmpty()) {
            int lastDot = javaClass.lastIndexOf('.');
            simpleName = lastDot >= 0 ? javaClass.substring(lastDot + 1) : javaClass;
        }
        if (simpleName == null || simpleName.isEmpty()) {
            simpleName = node.getName();
        }

        File javaFile = findFileRecursive(new File(directory), simpleName + ".java");
        if (javaFile == null) {
            return null;
        }
        String content = readFileQuietly(javaFile);
        return content == null ? null : new SourceFile(javaFile.getName(), content);
    }

    /**
     * Returns the module name a Compute node refers to, derived from its
     * computeExpression (e.g. "esql://routine/#MyModule.Main" -> "MyModule"),
     * falling back to the node name.
     *
     * @param node Compute node
     * @return Module name
     */
    private String extractEsqlModuleName(FlowNode node) {
        String expr = node.getProperty("computeExpression");
        if (expr != null && expr.contains("#")) {
            String afterHash = expr.substring(expr.indexOf('#') + 1);
            int dot = afterHash.indexOf('.');
            return dot >= 0 ? afterHash.substring(0, dot) : afterHash;
        }
        return node.getName();
    }

    /**
     * Extracts the body of a named ESQL module from file content.
     *
     * @param content    Full .esql file content
     * @param moduleName Module to extract
     * @return Module body (from CREATE ... MODULE to END MODULE), or null
     */
    private String extractEsqlModule(String content, String moduleName) {
        String upper = content.toUpperCase();
        String marker = "MODULE " + moduleName.toUpperCase();
        int markerPos = upper.indexOf(marker);
        if (markerPos < 0) {
            return null;
        }
        int end = upper.indexOf("END MODULE", markerPos);
        if (end < 0) {
            end = content.length();
        } else {
            end += "END MODULE".length();
        }
        return content.substring(markerPos, end);
    }

    /**
     * Reads the first file in the array whose name matches (case-insensitive).
     *
     * @param files    Candidate files
     * @param fileName Target file name
     * @return SourceFile, or null
     */
    private SourceFile readNamedFile(File[] files, String fileName) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(fileName)) {
                String content = readFileQuietly(file);
                if (content != null) {
                    return new SourceFile(file.getName(), content);
                }
            }
        }
        return null;
    }

    /**
     * Recursively searches a directory tree for a file by name.
     *
     * @param dir      Directory to search
     * @param fileName File name to find (case-insensitive)
     * @return Matching file, or null
     */
    private File findFileRecursive(File dir, String fileName) {
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        File[] children = dir.listFiles();
        if (children == null) {
            return null;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                File found = findFileRecursive(child, fileName);
                if (found != null) {
                    return found;
                }
            } else if (child.getName().equalsIgnoreCase(fileName)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Reads a file's content, returning null (and logging) on error.
     *
     * @param file File to read
     * @return File content, or null
     */
    private String readFileQuietly(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ValidationLogger.log("[BestPracticesValidator] Could not read source file: "
                    + file.getAbsolutePath());
            return null;
        }
    }

    /**
     * Computes the maximum nesting depth of IF conditions in ESQL code.
     * Only IF/END IF affect the depth; ELSEIF and other constructs do not.
     *
     * @param code ESQL code
     * @return Maximum IF nesting depth
     */
    private int calculateEsqlIfNesting(String code) {
        int maxNesting = 0;
        int current = 0;
        for (String line : code.split("\n")) {
            String trimmed = line.trim().toUpperCase();
            if (trimmed.startsWith("END IF")) {
                if (current > 0) {
                    current--;
                }
            } else if (trimmed.startsWith("IF ") || trimmed.equals("IF") || trimmed.startsWith("IF(")) {
                current++;
                if (current > maxNesting) {
                    maxNesting = current;
                }
            }
        }
        return maxNesting;
    }

    /**
     * Computes the maximum nesting depth of {@code if} blocks in Java code.
     * Tracks brace blocks and counts only those opened by an {@code if}/
     * {@code else if}. Best-effort: string/comment contents are not stripped.
     *
     * @param code Java source code
     * @return Maximum if-block nesting depth
     */
    private int calculateJavaIfNesting(String code) {
        int maxNesting = 0;
        int current = 0;
        Deque<Boolean> blockStack = new ArrayDeque<>();
        boolean pendingIf = false;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '{') {
                blockStack.push(pendingIf);
                if (pendingIf) {
                    current++;
                    if (current > maxNesting) {
                        maxNesting = current;
                    }
                }
                pendingIf = false;
            } else if (c == '}') {
                if (!blockStack.isEmpty() && Boolean.TRUE.equals(blockStack.pop())) {
                    if (current > 0) {
                        current--;
                    }
                }
            } else if (c == ';') {
                pendingIf = false;
            } else if ((c == 'i' || c == 'I') && matchesIfKeyword(code, i)) {
                pendingIf = true;
                i++; // skip the 'f'
            }
        }
        return maxNesting;
    }

    /**
     * Tests whether the word {@code if} (followed by '(') starts at index i.
     *
     * @param code Source code
     * @param i    Index to test
     * @return true if an if-keyword starts here
     */
    private boolean matchesIfKeyword(String code, int i) {
        if (i + 1 >= code.length()) {
            return false;
        }
        char c1 = code.charAt(i);
        char c2 = code.charAt(i + 1);
        if (!((c1 == 'i' || c1 == 'I') && (c2 == 'f' || c2 == 'F'))) {
            return false;
        }
        if (i > 0) {
            char prev = code.charAt(i - 1);
            if (Character.isLetterOrDigit(prev) || prev == '_') {
                return false;
            }
        }
        int j = i + 2;
        if (j < code.length()) {
            char after = code.charAt(j);
            if (Character.isLetterOrDigit(after) || after == '_') {
                return false;
            }
        }
        while (j < code.length() && Character.isWhitespace(code.charAt(j))) {
            j++;
        }
        return j < code.length() && code.charAt(j) == '(';
    }

    /**
     * Checks if node has logging configured.
     * 
     * @param node Node to check
     * @return true if logging is configured
     */
    private boolean hasLoggingConfigured(FlowNode node) {
        String logging = node.getProperty("logging");
        String trace = node.getProperty("trace");
        
        return ("true".equalsIgnoreCase(logging) || "Yes".equalsIgnoreCase(logging) ||
                "true".equalsIgnoreCase(trace) || "Yes".equalsIgnoreCase(trace));
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
        return "Validates best practices including flow structure, error propagation, " +
               "logging practices, documentation, and resource management. " +
               "Ensures maintainable, reliable, and professional code.";
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
        // Best practices apply to all node types
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled);
    }
}

// Made with Bob