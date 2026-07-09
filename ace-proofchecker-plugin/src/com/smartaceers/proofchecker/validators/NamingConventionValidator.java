package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator that checks naming conventions.
 * Enforces consistent naming, checks for descriptive names, and validates against standards.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class NamingConventionValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "naming.convention";
    private static final String VALIDATOR_NAME = "Naming Convention Validator";
    private static final String CATEGORY = "Maintainability";
    
    // Naming patterns
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern PASCAL_CASE_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final Pattern UPPER_SNAKE_CASE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    
    // Non-descriptive names
    private static final List<String> NON_DESCRIPTIVE_NAMES = Arrays.asList(
        "node", "node1", "node2", "temp", "tmp", "test", "foo", "bar",
        "compute", "compute1", "input", "output", "data", "process",
        "handler", "handler1", "flow", "flow1", "untitled"
    );
    
    // Reserved/problematic prefixes
    private static final List<String> PROBLEMATIC_PREFIXES = Arrays.asList(
        "new", "old", "temp", "tmp", "test", "debug", "copy"
    );
    
    // Minimum name length
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 50;
    
    private boolean enabled = true;
    private NamingStyle preferredStyle = NamingStyle.PASCAL_CASE;
    
    /**
     * Naming style enumeration.
     */
    public enum NamingStyle {
        CAMEL_CASE,
        PASCAL_CASE,
        SNAKE_CASE,
        UPPER_SNAKE_CASE
    }
    
    @Override
    public List<Finding> validate(FlowNode node) {
        System.out.println("[NamingConventionValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        String nodeName = node.getName();
        
        if (nodeName == null || nodeName.isEmpty()) {
            findings.add(createEmptyNameFinding(node));
            System.out.println("[NamingConventionValidator] Node has empty name");
            return findings;
        }
        
        // Check for descriptive names
        findings.addAll(validateDescriptiveness(node, nodeName));
        
        // Check naming style consistency
        findings.addAll(validateNamingStyle(node, nodeName));
        
        // Check name length
        findings.addAll(validateNameLength(node, nodeName));
        
        // Check for problematic patterns
        findings.addAll(validateProblematicPatterns(node, nodeName));
        
        // Check for special characters
        findings.addAll(validateSpecialCharacters(node, nodeName));
        
        System.out.println("[NamingConventionValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            System.out.println("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Creates a finding for empty node name.
     * 
     * @param node Node with empty name
     * @return Finding object
     */
    private Finding createEmptyNameFinding(FlowNode node) {
        String message = String.format(
            "Node of type '%s' has no name. All nodes should have descriptive names.",
            node.getType()
        );
        
        String suggestion = 
            "Provide a descriptive name for the node:\n" +
            "- Describes the node's purpose\n" +
            "- Follows naming conventions\n" +
            "- Makes the flow easier to understand\n" +
            "- Helps with debugging and maintenance";
        
        return new Finding(
            VALIDATOR_ID + ".empty.name",
            Severity.MEDIUM,
            message,
            suggestion,
            node,
            node.getLineNumber(),
            CATEGORY
        );
    }
    
    /**
     * Validates name descriptiveness.
     * 
     * @param node Node to validate
     * @param nodeName Name to check
     * @return List of findings
     */
    private List<Finding> validateDescriptiveness(FlowNode node, String nodeName) {
        List<Finding> findings = new ArrayList<>();
        
        String lowerName = nodeName.toLowerCase();
        
        // Check for non-descriptive names
        if (NON_DESCRIPTIVE_NAMES.contains(lowerName)) {
            String message = String.format(
                "Node '%s' has a non-descriptive name. " +
                "Use names that clearly indicate the node's purpose.",
                nodeName
            );
            
            String suggestion = 
                "Use descriptive names that explain what the node does:\n\n" +
                "Bad examples:\n" +
                "- 'Compute1', 'Node2', 'Temp'\n\n" +
                "Good examples:\n" +
                "- 'ValidateCustomerData'\n" +
                "- 'TransformOrderToXML'\n" +
                "- 'EnrichWithAccountInfo'\n" +
                "- 'RouteByMessageType'\n\n" +
                "Benefits:\n" +
                "- Self-documenting code\n" +
                "- Easier maintenance\n" +
                "- Better team collaboration\n" +
                "- Faster debugging";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".non.descriptive",
                Severity.MEDIUM,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        // Check if name is just the node type
        if (lowerName.equals(node.getType().toLowerCase())) {
            String message = String.format(
                "Node '%s' is named the same as its type. " +
                "This doesn't provide useful information.",
                nodeName
            );
            
            String suggestion = 
                "Add descriptive information to the node name:\n" +
                "- What data is being processed?\n" +
                "- What transformation is being applied?\n" +
                "- What business logic is implemented?\n\n" +
                "Example: Instead of 'Compute', use 'CalculateTotalAmount'";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".type.only.name",
                Severity.LOW,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        // Check for numbered suffixes without context
        if (Pattern.compile(".*\\d+$").matcher(nodeName).matches() && 
            nodeName.length() < 10) {
            
            String message = String.format(
                "Node '%s' uses a numbered suffix without context. " +
                "This makes it hard to understand the node's purpose.",
                nodeName
            );
            
            String suggestion = 
                "Instead of numbering nodes, use descriptive names:\n" +
                "- Bad: 'Process1', 'Process2', 'Process3'\n" +
                "- Good: 'ValidateInput', 'TransformData', 'SendResponse'\n\n" +
                "If multiple similar nodes are needed, include context:\n" +
                "- 'ValidateCustomerAddress'\n" +
                "- 'ValidateBillingAddress'\n" +
                "- 'ValidateShippingAddress'";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".numbered.suffix",
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
     * Validates naming style consistency.
     * 
     * @param node Node to validate
     * @param nodeName Name to check
     * @return List of findings
     */
    private List<Finding> validateNamingStyle(FlowNode node, String nodeName) {
        List<Finding> findings = new ArrayList<>();
        
        boolean matchesPreferred = false;
        String expectedStyle = "";
        
        switch (preferredStyle) {
            case CAMEL_CASE:
                matchesPreferred = CAMEL_CASE_PATTERN.matcher(nodeName).matches();
                expectedStyle = "camelCase (e.g., validateCustomerData)";
                break;
            case PASCAL_CASE:
                matchesPreferred = PASCAL_CASE_PATTERN.matcher(nodeName).matches();
                expectedStyle = "PascalCase (e.g., ValidateCustomerData)";
                break;
            case SNAKE_CASE:
                matchesPreferred = SNAKE_CASE_PATTERN.matcher(nodeName).matches();
                expectedStyle = "snake_case (e.g., validate_customer_data)";
                break;
            case UPPER_SNAKE_CASE:
                matchesPreferred = UPPER_SNAKE_CASE_PATTERN.matcher(nodeName).matches();
                expectedStyle = "UPPER_SNAKE_CASE (e.g., VALIDATE_CUSTOMER_DATA)";
                break;
        }
        
        if (!matchesPreferred) {
            // Check if it matches any other style
            boolean matchesAnyStyle = 
                CAMEL_CASE_PATTERN.matcher(nodeName).matches() ||
                PASCAL_CASE_PATTERN.matcher(nodeName).matches() ||
                SNAKE_CASE_PATTERN.matcher(nodeName).matches() ||
                UPPER_SNAKE_CASE_PATTERN.matcher(nodeName).matches();
            
            if (matchesAnyStyle) {
                String message = String.format(
                    "Node '%s' doesn't follow the preferred naming convention. " +
                    "Expected: %s",
                    nodeName,
                    expectedStyle
                );
                
                String suggestion = 
                    "Follow consistent naming conventions across the project:\n" +
                    "- Improves readability\n" +
                    "- Makes code more professional\n" +
                    "- Easier for team collaboration\n" +
                    "- Follows industry standards\n\n" +
                    "Recommended: " + expectedStyle;
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".style.mismatch",
                    Severity.LOW,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            } else {
                String message = String.format(
                    "Node '%s' doesn't follow any standard naming convention.",
                    nodeName
                );
                
                String suggestion = 
                    "Use a standard naming convention:\n" +
                    "- PascalCase: ValidateCustomerData (recommended for nodes)\n" +
                    "- camelCase: validateCustomerData\n" +
                    "- snake_case: validate_customer_data\n\n" +
                    "Avoid:\n" +
                    "- Mixed styles: Validate_customerData\n" +
                    "- Spaces: 'Validate Customer Data'\n" +
                    "- Special characters: Validate-Customer-Data";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".invalid.style",
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
     * Validates name length.
     * 
     * @param node Node to validate
     * @param nodeName Name to check
     * @return List of findings
     */
    private List<Finding> validateNameLength(FlowNode node, String nodeName) {
        List<Finding> findings = new ArrayList<>();
        
        if (nodeName.length() < MIN_NAME_LENGTH) {
            String message = String.format(
                "Node name '%s' is too short (%d characters). " +
                "Use more descriptive names.",
                nodeName,
                nodeName.length()
            );
            
            String suggestion = String.format(
                "Use names with at least %d characters that clearly describe the node's purpose.",
                MIN_NAME_LENGTH
            );
            
            findings.add(new Finding(
                VALIDATOR_ID + ".name.too.short",
                Severity.LOW,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        if (nodeName.length() > MAX_NAME_LENGTH) {
            String message = String.format(
                "Node name '%s' is too long (%d characters). " +
                "Consider using a more concise name.",
                nodeName,
                nodeName.length()
            );
            
            String suggestion = String.format(
                "Keep names under %d characters:\n" +
                "- Be concise but descriptive\n" +
                "- Remove unnecessary words\n" +
                "- Use abbreviations if widely understood\n" +
                "- Consider breaking into subflows if logic is complex",
                MAX_NAME_LENGTH
            );
            
            findings.add(new Finding(
                VALIDATOR_ID + ".name.too.long",
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
     * Validates problematic naming patterns.
     * 
     * @param node Node to validate
     * @param nodeName Name to check
     * @return List of findings
     */
    private List<Finding> validateProblematicPatterns(FlowNode node, String nodeName) {
        List<Finding> findings = new ArrayList<>();
        
        String lowerName = nodeName.toLowerCase();
        
        // Check for problematic prefixes
        for (String prefix : PROBLEMATIC_PREFIXES) {
            if (lowerName.startsWith(prefix)) {
                String message = String.format(
                    "Node '%s' uses problematic prefix '%s'. " +
                    "This suggests temporary or incomplete implementation.",
                    nodeName,
                    prefix
                );
                
                String suggestion = 
                    "Avoid temporary-sounding prefixes:\n" +
                    "- 'temp', 'tmp' - suggests temporary code\n" +
                    "- 'test', 'debug' - suggests non-production code\n" +
                    "- 'new', 'old' - suggests migration in progress\n" +
                    "- 'copy' - suggests duplication\n\n" +
                    "Use descriptive names that reflect the actual purpose.";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".problematic.prefix",
                    Severity.MEDIUM,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
                break;
            }
        }
        
        return findings;
    }
    
    /**
     * Validates special characters in names.
     * 
     * @param node Node to validate
     * @param nodeName Name to check
     * @return List of findings
     */
    private List<Finding> validateSpecialCharacters(FlowNode node, String nodeName) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for spaces
        if (nodeName.contains(" ")) {
            String message = String.format(
                "Node name '%s' contains spaces. " +
                "This can cause issues in some contexts.",
                nodeName
            );
            
            String suggestion = 
                "Remove spaces from node names:\n" +
                "- Use PascalCase: 'ValidateCustomerData'\n" +
                "- Use camelCase: 'validateCustomerData'\n" +
                "- Use snake_case: 'validate_customer_data'\n\n" +
                "Spaces can cause:\n" +
                "- Parsing issues\n" +
                "- Problems in scripts\n" +
                "- Inconsistent behavior";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".spaces.in.name",
                Severity.MEDIUM,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        // Check for special characters (except underscore)
        if (nodeName.matches(".*[^a-zA-Z0-9_].*")) {
            String message = String.format(
                "Node name '%s' contains special characters. " +
                "Use only letters, numbers, and underscores.",
                nodeName
            );
            
            String suggestion = 
                "Avoid special characters in node names:\n" +
                "- Use alphanumeric characters\n" +
                "- Underscores are acceptable\n" +
                "- Avoid: -, ., @, #, $, %, etc.\n\n" +
                "Special characters can cause:\n" +
                "- Compatibility issues\n" +
                "- Parsing problems\n" +
                "- Unexpected behavior";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".special.characters",
                Severity.MEDIUM,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
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
        return "Validates naming conventions for nodes including style consistency, " +
               "descriptiveness, length, and problematic patterns. Ensures maintainable " +
               "and professional code through consistent naming standards.";
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
    
    /**
     * Sets the preferred naming style.
     * 
     * @param style Preferred naming style
     */
    public void setPreferredStyle(NamingStyle style) {
        this.preferredStyle = style;
    }
    
    /**
     * Gets the preferred naming style.
     * 
     * @return Preferred naming style
     */
    public NamingStyle getPreferredStyle() {
        return preferredStyle;
    }
    
    @Override
    public boolean appliesTo(String nodeType) {
        // Naming conventions apply to all node types
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s, Style: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled, preferredStyle);
    }
}

// Made with Bob