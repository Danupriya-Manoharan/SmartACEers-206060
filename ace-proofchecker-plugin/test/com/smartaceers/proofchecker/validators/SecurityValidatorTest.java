package com.smartaceers.proofchecker.validators;

import static org.junit.Assert.*;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Unit tests for SecurityValidator.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class SecurityValidatorTest {
    
    private SecurityValidator validator;
    
    @Before
    public void setUp() {
        validator = new SecurityValidator();
    }
    
    @Test
    public void testValidatorMetadata() {
        assertEquals("security.validation", validator.getValidatorId());
        assertEquals("Security Validator", validator.getValidatorName());
        assertEquals("Security", validator.getCategory());
        assertTrue(validator.isEnabled());
    }
    
    @Test
    public void testAppliesToAllNodes() {
        assertTrue(validator.appliesTo("Compute"));
        assertTrue(validator.appliesTo("HTTPRequest"));
        assertTrue(validator.appliesTo("DatabaseInput"));
        assertTrue(validator.appliesTo("MQInput"));
    }
    
    @Test
    public void testHardcodedPassword() {
        FlowNode node = new FlowNode("TestNode", "HTTPRequest", "node1");
        node.setProperty("password", "mySecretPassword123");
        node.setLineNumber(10);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.hardcoded.password");
        assertNotNull("Should find hardcoded password", finding);
        assertEquals(Severity.CRITICAL, finding.getSeverity());
        assertTrue(finding.getMessage().contains("hardcoded password"));
    }
    
    @Test
    public void testPasswordWithEnvironmentVariable() {
        FlowNode node = new FlowNode("TestNode", "HTTPRequest", "node1");
        node.setProperty("password", "${Environment.PASSWORD}");
        node.setLineNumber(15);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.hardcoded.password");
        assertNull("Should not flag environment variable", finding);
    }
    
    @Test
    public void testHardcodedCredentialInCode() {
        FlowNode node = new FlowNode("TestCompute", "Compute", "node1");
        node.setProperty("code", "SET password = 'myPassword123';");
        node.setLineNumber(20);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.hardcoded.credential");
        assertNotNull("Should find hardcoded credential in code", finding);
        assertEquals(Severity.CRITICAL, finding.getSeverity());
    }
    
    @Test
    public void testCredentialInURL() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "http://api.example.com?apikey=abc123");
        node.setLineNumber(25);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.credential.in.url");
        assertNotNull("Should find credential in URL", finding);
        assertEquals(Severity.CRITICAL, finding.getSeverity());
    }
    
    @Test
    public void testUnencryptedHTTP() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "http://api.example.com/data");
        node.setLineNumber(30);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.unencrypted.http");
        assertNotNull("Should find unencrypted HTTP", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
        assertTrue(finding.getMessage().contains("unencrypted HTTP"));
    }
    
    @Test
    public void testHTTPS() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "https://api.example.com/data");
        node.setLineNumber(35);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.unencrypted.http");
        assertNull("Should not flag HTTPS", finding);
    }
    
    @Test
    public void testWeakSSLProtocol() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "https://api.example.com");
        node.setProperty("sslProtocol", "TLSv1.0");
        node.setLineNumber(40);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.weak.ssl");
        assertNotNull("Should find weak SSL protocol", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
    }
    
    @Test
    public void testStrongSSLProtocol() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "https://api.example.com");
        node.setProperty("sslProtocol", "TLSv1.3");
        node.setLineNumber(45);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.weak.ssl");
        assertNull("Should not flag TLS 1.3", finding);
    }
    
    @Test
    public void testUnencryptedFileOperation() {
        FlowNode node = new FlowNode("TestFile", "FileWrite", "node1");
        node.setProperty("encryption", "None");
        node.setLineNumber(50);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.unencrypted.file");
        assertNotNull("Should find unencrypted file", finding);
        assertEquals(Severity.MEDIUM, finding.getSeverity());
    }
    
    @Test
    public void testSensitiveDataHandling() {
        FlowNode node = new FlowNode("TestCompute", "Compute", "node1");
        node.setProperty("code", "SET creditcard = InputRoot.JSON.Data.cardNumber;");
        node.setLineNumber(55);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.sensitive.data");
        assertNotNull("Should find sensitive data handling", finding);
        assertEquals(Severity.MEDIUM, finding.getSeverity());
        assertTrue(finding.getMessage().contains("sensitive data"));
    }
    
    @Test
    public void testInsecureFTP() {
        FlowNode node = new FlowNode("TestFTP", "FTPInput", "node1");
        node.setProperty("protocol", "FTP");
        node.setLineNumber(60);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.insecure.ftp");
        assertNotNull("Should find insecure FTP", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
    }
    
    @Test
    public void testSecureFTP() {
        FlowNode node = new FlowNode("TestFTP", "FTPInput", "node1");
        node.setProperty("protocol", "SFTP");
        node.setLineNumber(65);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.insecure.ftp");
        assertNull("Should not flag SFTP", finding);
    }
    
    @Test
    public void testSensitiveDataLogging() {
        FlowNode node = new FlowNode("TestLog", "Trace", "node1");
        node.setProperty("message", "User password: {$password}");
        node.setLineNumber(70);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.sensitive.logging");
        assertNotNull("Should find sensitive data in logging", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
    }
    
    @Test
    public void testBasicAuthOverHTTP() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "http://api.example.com");
        node.setProperty("authenticationType", "Basic");
        node.setLineNumber(75);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.authentication.insecure");
        assertNotNull("Should find Basic Auth over HTTP", finding);
        assertEquals(Severity.CRITICAL, finding.getSeverity());
    }
    
    @Test
    public void testBasicAuthOverHTTPS() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "https://api.example.com");
        node.setProperty("authenticationType", "Basic");
        node.setLineNumber(80);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.authentication.insecure");
        assertNull("Should not flag Basic Auth over HTTPS", finding);
    }
    
    @Test
    public void testMissingAuthentication() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "https://api.example.com");
        node.setLineNumber(85);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "security.validation.authentication.missing");
        assertNotNull("Should find missing authentication", finding);
        assertEquals(Severity.LOW, finding.getSeverity());
    }
    
    @Test
    public void testMultipleSecurityIssues() {
        FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
        node.setProperty("url", "http://api.example.com?password=secret");
        node.setProperty("password", "hardcodedPass");
        node.setLineNumber(90);
        
        List<Finding> findings = validator.validate(node);
        
        // Should find multiple issues
        assertTrue("Should find multiple security issues", findings.size() >= 2);
        assertNotNull(findByRuleId(findings, "security.validation.unencrypted.http"));
        assertNotNull(findByRuleId(findings, "security.validation.credential.in.url"));
    }
    
    /**
     * Helper method to find a finding by rule ID.
     */
    private Finding findByRuleId(List<Finding> findings, String ruleId) {
        for (Finding finding : findings) {
            if (finding.getRuleId().equals(ruleId)) {
                return finding;
            }
        }
        return null;
    }
}

// Made with Bob