package com.smartaceers.proofchecker.validators;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import com.smartaceers.proofchecker.utils.ValidationLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validator that checks HTTP/REST configurations.
 * Validates timeout settings, error response handling, and authentication.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class HTTPRestValidator implements IValidator {
    
    private static final String VALIDATOR_ID = "http.rest.config";
    private static final String VALIDATOR_NAME = "HTTP/REST Validator";
    private static final String CATEGORY = "Reliability";
    
    // Node types that make HTTP/REST calls
    private static final List<String> APPLICABLE_NODE_TYPES = Arrays.asList(
        "HTTPRequest",
        "HTTPInput",
        "HTTPReply",
        "RESTRequest",
        "RESTAsyncRequest",
        "SOAPRequest",
        "SOAPInput"
    );
    
    // Recommended timeout values (in seconds)
    private static final int MIN_TIMEOUT = 5;
    private static final int MAX_TIMEOUT = 300;
    private static final int RECOMMENDED_TIMEOUT = 60;
    
    // HTTP status codes
    private static final List<String> ERROR_STATUS_CODES = Arrays.asList(
        "4xx", "5xx", "400", "401", "403", "404", "500", "502", "503", "504"
    );
    
    private boolean enabled = true;
    
    @Override
    public List<Finding> validate(FlowNode node) {
        ValidationLogger.log("[HTTPRestValidator] Validating node: " + node.getName() + " (Type: " + node.getType() + ")");
        
        List<Finding> findings = new ArrayList<>();
        
        // Only validate HTTP/REST nodes
        if (!appliesTo(node.getType())) {
            ValidationLogger.log("[HTTPRestValidator] Skipping - not an HTTP/REST node");
            return findings;
        }
        
        // Check timeout configurations
        findings.addAll(validateTimeoutConfiguration(node));
        
        // Check error response handling
        findings.addAll(validateErrorResponseHandling(node));
        
        // Check authentication
        findings.addAll(validateAuthentication(node));
        
        // Check retry configuration
        findings.addAll(validateRetryConfiguration(node));
        
        ValidationLogger.log("[HTTPRestValidator] Found " + findings.size() + " issue(s) for node: " + node.getName());
        for (Finding f : findings) {
            ValidationLogger.log("  - " + f.getSeverity().getDisplayName() + ": " + f.getMessage());
        }
        
        return findings;
    }
    
    /**
     * Validates timeout configuration.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateTimeoutConfiguration(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        String timeoutStr = node.getProperty("timeout");
        String requestTimeoutStr = node.getProperty("requestTimeout");
        String responseTimeoutStr = node.getProperty("responseTimeout");
        
        // Use whichever timeout property is available
        String effectiveTimeout = timeoutStr != null ? timeoutStr : 
                                 requestTimeoutStr != null ? requestTimeoutStr : 
                                 responseTimeoutStr;
        
        if (effectiveTimeout != null && !effectiveTimeout.isEmpty()) {
            try {
                int timeout = Integer.parseInt(effectiveTimeout);
                
                // Check if timeout is too low
                if (timeout < MIN_TIMEOUT) {
                    String message = String.format(
                        "HTTP/REST node '%s' has a very low timeout (%d seconds). " +
                        "This may cause premature timeouts for legitimate requests.",
                        node.getName(),
                        timeout
                    );
                    
                    String suggestion = String.format(
                        "Increase the timeout to at least %d seconds. " +
                        "Recommended value is %d seconds.\n\n" +
                        "Consider:\n" +
                        "- Network latency\n" +
                        "- Backend processing time\n" +
                        "- Expected response times\n" +
                        "- Retry mechanisms",
                        MIN_TIMEOUT,
                        RECOMMENDED_TIMEOUT
                    );
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".timeout.low",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
                
                // Check if timeout is too high
                if (timeout > MAX_TIMEOUT) {
                    String message = String.format(
                        "HTTP/REST node '%s' has a very high timeout (%d seconds). " +
                        "This may cause threads to hang for extended periods.",
                        node.getName(),
                        timeout
                    );
                    
                    String suggestion = String.format(
                        "Consider reducing the timeout to a more reasonable value (recommended: %d seconds). " +
                        "Very high timeouts can:\n" +
                        "- Tie up execution threads\n" +
                        "- Delay error detection\n" +
                        "- Impact overall system responsiveness\n" +
                        "- Cause cascading failures",
                        RECOMMENDED_TIMEOUT
                    );
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".timeout.high",
                        Severity.MEDIUM,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        CATEGORY
                    ));
                }
            } catch (NumberFormatException e) {
                String message = String.format(
                    "HTTP/REST node '%s' has an invalid timeout value: '%s'",
                    node.getName(),
                    effectiveTimeout
                );
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".timeout.invalid",
                    Severity.HIGH,
                    message,
                    "Provide a valid numeric timeout value in seconds.",
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
        } else {
            // No timeout configured
            String message = String.format(
                "HTTP/REST node '%s' does not have an explicit timeout configured. " +
                "Using default timeout which may not be appropriate.",
                node.getName()
            );
            
            String suggestion = String.format(
                "Explicitly configure a timeout value (recommended: %d seconds). " +
                "This ensures:\n" +
                "- Predictable behavior\n" +
                "- Prevention of indefinite waits\n" +
                "- Better resource management\n" +
                "- Faster failure detection",
                RECOMMENDED_TIMEOUT
            );
            
            findings.add(new Finding(
                VALIDATOR_ID + ".timeout.missing",
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
    
    /**
     * Validates error response handling.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateErrorResponseHandling(FlowNode node) {
        List<Finding> findings = new ArrayList<>();

        // Terminal connection checks are handled exclusively by
        // CatchTerminalValidator (catch terminal only, on input nodes).

        // Check HTTP status code handling for request nodes
        if (node.getType().contains("Request")) {
            String httpStatusCodeHandling = node.getProperty("httpStatusCodeHandling");
            if (httpStatusCodeHandling == null || httpStatusCodeHandling.isEmpty()) {
                String message = String.format(
                    "HTTP/REST node '%s' does not have explicit status code handling configured.",
                    node.getName()
                );
                
                String suggestion = 
                    "Configure HTTP status code handling to:\n" +
                    "- Define which status codes are considered errors\n" +
                    "- Route different status codes appropriately\n" +
                    "- Implement proper error recovery\n\n" +
                    "Common patterns:\n" +
                    "- Treat 4xx as client errors (failure terminal)\n" +
                    "- Treat 5xx as server errors (catch terminal)\n" +
                    "- Retry on specific 5xx codes";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".status.code.handling",
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
     * Validates authentication configuration.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateAuthentication(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        String securityProfile = node.getProperty("securityProfile");
        String useAuthentication = node.getProperty("useAuthentication");
        String authType = node.getProperty("authenticationType");
        
        // Check if authentication is configured for request nodes
        if (node.getType().contains("Request")) {
            if ((securityProfile == null || securityProfile.isEmpty()) &&
                (!"true".equalsIgnoreCase(useAuthentication) && !"Yes".equalsIgnoreCase(useAuthentication))) {
                
                String message = String.format(
                    "HTTP/REST node '%s' does not have authentication configured. " +
                    "This may be a security risk if calling protected endpoints.",
                    node.getName()
                );
                
                String suggestion = 
                    "Configure appropriate authentication if calling protected endpoints:\n" +
                    "- Basic Authentication\n" +
                    "- OAuth 2.0\n" +
                    "- API Keys\n" +
                    "- Client Certificates\n" +
                    "- Custom security profiles\n\n" +
                    "If the endpoint is public, document this decision.";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".authentication.missing",
                    Severity.LOW,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            }
            
            // Check for basic auth without HTTPS
            if ("Basic".equalsIgnoreCase(authType)) {
                String url = node.getProperty("url");
                if (url != null && url.toLowerCase().startsWith("http://")) {
                    String message = String.format(
                        "HTTP/REST node '%s' uses Basic Authentication over HTTP (not HTTPS). " +
                        "Credentials will be transmitted in clear text.",
                        node.getName()
                    );
                    
                    String suggestion = 
                        "Use HTTPS instead of HTTP when using Basic Authentication. " +
                        "This ensures credentials are encrypted in transit.\n\n" +
                        "Security risks of HTTP with Basic Auth:\n" +
                        "- Credentials visible in network traffic\n" +
                        "- Susceptible to man-in-the-middle attacks\n" +
                        "- Compliance violations (PCI-DSS, GDPR, etc.)";
                    
                    findings.add(new Finding(
                        VALIDATOR_ID + ".authentication.insecure",
                        Severity.CRITICAL,
                        message,
                        suggestion,
                        node,
                        node.getLineNumber(),
                        "Security"
                    ));
                }
            }
        }
        
        return findings;
    }
    
    /**
     * Validates retry configuration.
     * 
     * @param node Node to validate
     * @return List of findings
     */
    private List<Finding> validateRetryConfiguration(FlowNode node) {
        List<Finding> findings = new ArrayList<>();
        
        String retryEnabled = node.getProperty("retryEnabled");
        String maxRetries = node.getProperty("maxRetries");
        String retryInterval = node.getProperty("retryInterval");
        
        // Check if retry is configured for request nodes
        if (node.getType().contains("Request")) {
            if (!"true".equalsIgnoreCase(retryEnabled) && !"Yes".equalsIgnoreCase(retryEnabled)) {
                String message = String.format(
                    "HTTP/REST node '%s' does not have retry logic configured. " +
                    "Transient failures may cause unnecessary errors.",
                    node.getName()
                );
                
                String suggestion = 
                    "Consider implementing retry logic for transient failures:\n" +
                    "- Network timeouts\n" +
                    "- Temporary service unavailability (503)\n" +
                    "- Rate limiting (429)\n\n" +
                    "Best practices:\n" +
                    "- Use exponential backoff\n" +
                    "- Limit retry attempts (3-5 typical)\n" +
                    "- Only retry idempotent operations\n" +
                    "- Don't retry on 4xx client errors";
                
                findings.add(new Finding(
                    VALIDATOR_ID + ".retry.missing",
                    Severity.LOW,
                    message,
                    suggestion,
                    node,
                    node.getLineNumber(),
                    CATEGORY
                ));
            } else {
                // Validate retry configuration
                if (maxRetries != null && !maxRetries.isEmpty()) {
                    try {
                        int retries = Integer.parseInt(maxRetries);
                        if (retries > 10) {
                            String message = String.format(
                                "HTTP/REST node '%s' has excessive retry attempts (%d). " +
                                "This may cause long delays and resource exhaustion.",
                                node.getName(),
                                retries
                            );
                            
                            findings.add(new Finding(
                                VALIDATOR_ID + ".retry.excessive",
                                Severity.MEDIUM,
                                message,
                                "Limit retry attempts to 3-5 for most scenarios.",
                                node,
                                node.getLineNumber(),
                                CATEGORY
                            ));
                        }
                    } catch (NumberFormatException e) {
                        // Invalid retry count
                    }
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
        return "Validates HTTP/REST configurations including timeout settings, " +
               "error response handling, authentication, and retry logic. " +
               "Ensures reliable and secure HTTP/REST communications.";
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
        return APPLICABLE_NODE_TYPES.contains(nodeType);
    }
    
    /**
     * Gets the list of node types this validator applies to.
     * 
     * @return List of applicable node types
     */
    public List<String> getApplicableNodeTypes() {
        return new ArrayList<>(APPLICABLE_NODE_TYPES);
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Enabled: %s)", 
            VALIDATOR_NAME, VALIDATOR_ID, enabled);
    }
}

// Made with Bob