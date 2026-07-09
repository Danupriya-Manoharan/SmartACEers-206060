package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import com.smartaceers.proofchecker.utils.ValidationLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator that checks for security issues.
 * Detects hardcoded credentials, validates encryption settings, and ensures sensitive data handling.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class SecurityValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "security.validation";
    private static final String VALIDATOR_NAME = "Security Validator";
    private static final String CATEGORY = "Security";
    
    // Patterns for detecting hardcoded credentials
    private static final List<Pattern> CREDENTIAL_PATTERNS = Arrays.asList(
        Pattern.compile("password\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("pwd\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("apikey\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("api_key\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("secret\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("token\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("authorization\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE)
    );
    
    // Sensitive field names
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "pwd", "passwd", "secret", "apikey", "api_key", "token",
        "authorization", "auth", "credential", "ssn", "social_security",
        "credit_card", "creditcard", "cvv", "pin", "private_key", "privatekey"
    );
    
    // Node types that handle sensitive data
    private static final List<String> SENSITIVE_NODE_TYPES = Arrays.asList(
        "HTTPRequest", "HTTPInput", "RESTRequest", "SOAPRequest",
        "DatabaseRetrieve", "DatabaseInput", "FileRead", "FileWrite",
        "MQInput", "MQOutput", "Compute", "JavaCompute"
    );
    
    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        ValidationLogger.log("[SecurityValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        // Check for hardcoded credentials
        findings.addAll(validateHardcodedCredentials(node));
        
        // Check encryption settings
        findings.addAll(validateEncryption(node));
        
        // Check sensitive data handling
        findings.addAll(validateSensitiveDataHandling(node));
        
        // Check for insecure protocols
        findings.addAll(validateSecureProtocols(node));
        
        // Check logging of sensitive data
        findings.addAll(validateLogging(node));
        
        ValidationLogger.log("[SecurityValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            ValidationLogger.log("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Validates for hardcoded credentials.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateHardcodedCredentials(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check node properties for hardcoded credentials
        String code = node.getProperty("code");
        String url = node.getProperty("url");
        String connectionString = node.getProperty("connectionString");
        String username = node.getProperty("username");
        String password = node.getProperty("password");
        
        // Check for hardcoded passwords
        if (password != null && !password.isEmpty() && 
            !password.startsWith("${") && !password.contains("Environment.")) {
            
            String message = String.format(
                "Node '%s' contains a hardcoded password. " +
                "This is a critical security vulnerability.",
                node.getName()
            );
            
            String suggestion = 
                "Never hardcode credentials. Use secure alternatives:\n\n" +
                "1. Environment Variables:\n" +
                "   - Store credentials in broker environment\n" +
                "   - Access via Environment variables\n" +
                "   - Example: Environment.Variables.PASSWORD\n\n" +
                "2. Security Profiles:\n" +
                "   - Use IIB/ACE security profiles\n" +
                "   - Centralized credential management\n" +
                "   - Encrypted storage\n\n" +
                "3. External Vault:\n" +
                "   - HashiCorp Vault\n" +
                "   - AWS Secrets Manager\n" +
                "   - Azure Key Vault\n\n" +
                "4. Configuration Files:\n" +
                "   - Store in encrypted config files\n" +
                "   - Restrict file permissions\n" +
                "   - Never commit to version control";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".hardcoded.password",
                Severity.CRITICAL,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }
        
        // Check code for credential patterns
        if (code != null) {
            for (Pattern pattern : CREDENTIAL_PATTERNS) {
                if (pattern.matcher(code).find()) {
                    String message = String.format(
                        "Node '%s' appears to contain hardcoded credentials in code. " +
                        "This is a critical security vulnerability.",
                        node.getName()
                    );
                    
                    String suggestion = 
                        "Remove hardcoded credentials from code:\n" +
                        "- Use environment variables\n" +
                        "- Use security profiles\n" +
                        "- Use external credential stores\n" +
                        "- Never commit credentials to source control\n\n" +
                        "If this is a false positive (e.g., example code), " +
                        "ensure it's clearly marked as such.";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".hardcoded.credential",
                        Severity.CRITICAL,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                    break; // Only report once per node
                }
            }
        }
        
        // Check for credentials in URLs
        if (url != null && (url.contains("password=") || url.contains("pwd=") || 
            url.contains("apikey=") || url.contains("token="))) {
            
            String message = String.format(
                "Node '%s' has credentials in the URL. " +
                "This exposes credentials in logs and monitoring tools.",
                node.getName()
            );
            
            String suggestion = 
                "Never include credentials in URLs:\n" +
                "- Use HTTP headers for authentication\n" +
                "- Use POST body for sensitive data\n" +
                "- Use security profiles\n" +
                "- URLs are often logged and cached";
            
            findings.add(new Finding(
                VALIDATOR_ID + ".credential.in.url",
                Severity.CRITICAL,
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
     * Validates encryption settings.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateEncryption(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for unencrypted HTTP connections. This applies to any node that
        // carries an http:// endpoint - not only nodes whose type name contains
        // "HTTP"/"REST" (e.g. SOAP/WS request nodes also have endpoints).
        String url = node.getProperty("url");

        if (url != null && url.toLowerCase().startsWith("http://")) {
            String message = String.format(
                "Node '%s' uses unencrypted HTTP protocol. " +
                "Data will be transmitted in clear text.",
                node.getName()
            );

            String suggestion =
                "Use HTTPS instead of HTTP to encrypt data in transit:\n" +
                "- Protects against eavesdropping\n" +
                "- Prevents man-in-the-middle attacks\n" +
                "- Required for compliance (PCI-DSS, HIPAA, GDPR)\n" +
                "- Industry best practice\n\n" +
                "To fix:\n" +
                "1. Change URL from http:// to https://\n" +
                "2. Configure SSL/TLS certificates\n" +
                "3. Validate server certificates\n" +
                "4. Use appropriate cipher suites";

            findings.add(new Finding(
                VALIDATOR_ID + ".unencrypted.http",
                Severity.HIGH,
                message,
                suggestion,
                node,
                node.getLineNumber(),
                CATEGORY
            ));
        }

        // Check SSL/TLS configuration for HTTP/REST nodes
        if (node.getType().contains("HTTP") || node.getType().contains("REST")) {
            // Check SSL/TLS configuration
            String sslProtocol = node.getProperty("sslProtocol");
            if (sslProtocol != null && 
                (sslProtocol.contains("SSLv2") || sslProtocol.contains("SSLv3") || 
                 sslProtocol.contains("TLSv1.0") || sslProtocol.contains("TLSv1.1"))) {
                
                String message = String.format(
                    "Node '%s' uses outdated SSL/TLS protocol: %s. " +
                    "This protocol has known vulnerabilities.",
                    node.getName(),
                    sslProtocol
                );
                
                String suggestion = 
                    "Use modern TLS protocols:\n" +
                    "- TLS 1.2 (minimum)\n" +
                    "- TLS 1.3 (recommended)\n" +
                    "- Disable SSLv2, SSLv3, TLS 1.0, TLS 1.1\n" +
                    "- Use strong cipher suites\n" +
                    "- Keep certificates up to date";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".weak.ssl",
                    Severity.HIGH,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        }
        
        // Check for unencrypted file operations
        if ("FileWrite".equals(node.getType()) || "FileRead".equals(node.getType())) {
            String encryption = node.getProperty("encryption");
            
            if (encryption == null || "None".equalsIgnoreCase(encryption) || 
                "false".equalsIgnoreCase(encryption)) {
                
                String message = String.format(
                    "File operation node '%s' does not use encryption. " +
                    "Sensitive data may be stored in clear text.",
                    node.getName()
                );
                
                String suggestion = 
                    "Consider encrypting sensitive data at rest:\n" +
                    "- Use file system encryption\n" +
                    "- Encrypt data before writing\n" +
                    "- Use encrypted storage solutions\n" +
                    "- Implement key management\n\n" +
                    "If files don't contain sensitive data, document this decision.";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".unencrypted.file",
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
     * Validates sensitive data handling.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateSensitiveDataHandling(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        if (!SENSITIVE_NODE_TYPES.contains(node.getType())) {
            return findings;
        }
        
        // Check if node handles sensitive data
        String code = node.getProperty("code");
        String fields = node.getProperty("fields");
        
        if (code != null || fields != null) {
            String content = (code != null ? code : "") + (fields != null ? fields : "");
            
            for (String sensitiveField : SENSITIVE_FIELDS) {
                if (content.toLowerCase().contains(sensitiveField.toLowerCase())) {
                    String message = String.format(
                        "Node '%s' appears to handle sensitive data (%s). " +
                        "Ensure proper security measures are in place.",
                        node.getName(),
                        sensitiveField
                    );
                    
                    String suggestion = 
                        "When handling sensitive data:\n\n" +
                        "1. Encryption:\n" +
                        "   - Encrypt data in transit (HTTPS/TLS)\n" +
                        "   - Encrypt data at rest\n" +
                        "   - Use strong encryption algorithms\n\n" +
                        "2. Access Control:\n" +
                        "   - Implement proper authentication\n" +
                        "   - Use role-based access control\n" +
                        "   - Audit access to sensitive data\n\n" +
                        "3. Data Masking:\n" +
                        "   - Mask sensitive data in logs\n" +
                        "   - Redact in error messages\n" +
                        "   - Tokenize when possible\n\n" +
                        "4. Compliance:\n" +
                        "   - Follow PCI-DSS for payment data\n" +
                        "   - Follow HIPAA for health data\n" +
                        "   - Follow GDPR for personal data\n\n" +
                        "5. Retention:\n" +
                        "   - Don't store sensitive data unnecessarily\n" +
                        "   - Implement data retention policies\n" +
                        "   - Securely delete when no longer needed";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".sensitive.data",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                    break; // Only report once per node
                }
            }
        }
        
        return findings;
    }
    
    /**
     * Validates use of secure protocols.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateSecureProtocols(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for FTP instead of SFTP
        if ("FTPInput".equals(node.getType()) || "FTPOutput".equals(node.getType())) {
            String protocol = node.getProperty("protocol");
            
            if (!"SFTP".equalsIgnoreCase(protocol) && !"FTPS".equalsIgnoreCase(protocol)) {
                String message = String.format(
                    "Node '%s' uses FTP which transmits data in clear text. " +
                    "This is insecure for sensitive data.",
                    node.getName()
                );
                
                String suggestion = 
                    "Use secure file transfer protocols:\n" +
                    "- SFTP (SSH File Transfer Protocol)\n" +
                    "- FTPS (FTP over SSL/TLS)\n" +
                    "- SCP (Secure Copy Protocol)\n\n" +
                    "Benefits:\n" +
                    "- Encrypted data transfer\n" +
                    "- Authentication\n" +
                    "- Data integrity\n" +
                    "- Compliance requirements";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".insecure.ftp",
                    Severity.HIGH,
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
     * Validates logging practices for sensitive data.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateLogging(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        // Check for logging of sensitive data
        if ("Trace".equals(node.getType()) || "Log".equals(node.getType())) {
            String logPattern = node.getProperty("pattern");
            String logMessage = node.getProperty("message");
            
            String content = (logPattern != null ? logPattern : "") + 
                           (logMessage != null ? logMessage : "");
            
            for (String sensitiveField : SENSITIVE_FIELDS) {
                if (content.toLowerCase().contains(sensitiveField.toLowerCase())) {
                    String message = String.format(
                        "Logging node '%s' may be logging sensitive data (%s). " +
                        "This can expose sensitive information in log files.",
                        node.getName(),
                        sensitiveField
                    );
                    
                    String suggestion = 
                        "Never log sensitive data:\n" +
                        "- Mask or redact sensitive fields\n" +
                        "- Use tokenization\n" +
                        "- Log only non-sensitive identifiers\n" +
                        "- Implement log sanitization\n\n" +
                        "Example:\n" +
                        "- Instead of: 'Password: abc123'\n" +
                        "- Log: 'Password: ********'\n" +
                        "- Or: 'Authentication successful for user: john'";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".sensitive.logging",
                        Severity.HIGH,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                    break;
                }
            }
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
        return "Validates security configurations including detection of hardcoded credentials, " +
               "encryption settings, sensitive data handling, secure protocols, and logging practices. " +
               "Helps identify security vulnerabilities and compliance issues.";
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
        // Security checks apply to most node types
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled);
    }
}

// Made with Bob