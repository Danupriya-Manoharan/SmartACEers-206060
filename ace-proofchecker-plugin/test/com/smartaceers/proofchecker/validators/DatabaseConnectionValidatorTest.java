package com.smartaceers.proofchecker.validators;

import static org.junit.Assert.*;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Unit tests for DatabaseConnectionValidator.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class DatabaseConnectionValidatorTest {
    
    private DatabaseConnectionValidator validator;
    
    @Before
    public void setUp() {
        validator = new DatabaseConnectionValidator();
    }
    
    @Test
    public void testValidatorMetadata() {
        assertEquals("database.connection.config", validator.getValidatorId());
        assertEquals("Database Connection Validator", validator.getValidatorName());
        assertEquals("Performance", validator.getCategory());
        assertTrue(validator.isEnabled());
    }
    
    @Test
    public void testAppliesToDatabaseNodes() {
        assertTrue(validator.appliesTo("DatabaseInput"));
        assertTrue(validator.appliesTo("DatabaseRetrieve"));
        assertTrue(validator.appliesTo("DatabaseRoute"));
        assertFalse(validator.appliesTo("Compute"));
        assertFalse(validator.appliesTo("MQInput"));
    }
    
    @Test
    public void testConnectionPoolingDisabled() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("useConnectionPooling", "false");
        node.setLineNumber(10);
        
        List<Finding> findings = validator.validate(node);
        
        assertFalse(findings.isEmpty());
        Finding finding = findByRuleId(findings, "database.connection.config.pooling");
        assertNotNull("Should find pooling issue", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
        assertTrue(finding.getMessage().contains("connection pooling disabled"));
    }
    
    @Test
    public void testConnectionPoolingEnabled() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("useConnectionPooling", "true");
        node.setProperty("dataSourceName", "MyDataSource");
        node.setProperty("timeout", "30");
        node.setLineNumber(10);
        
        List<Finding> findings = validator.validate(node);
        
        // Should not have pooling issue
        Finding poolingFinding = findByRuleId(findings, "database.connection.config.pooling");
        assertNull("Should not find pooling issue when enabled", poolingFinding);
    }
    
    @Test
    public void testMissingDataSource() {
        FlowNode node = new FlowNode("TestDB", "DatabaseRetrieve", "node1");
        node.setProperty("dataSourceName", "");
        node.setLineNumber(15);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.datasource");
        assertNotNull("Should find missing datasource", finding);
        assertEquals(Severity.CRITICAL, finding.getSeverity());
        assertTrue(finding.getMessage().contains("does not have a data source"));
    }
    
    @Test
    public void testTimeoutTooLow() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("timeout", "2");
        node.setProperty("dataSourceName", "MyDS");
        node.setLineNumber(20);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.timeout.low");
        assertNotNull("Should find low timeout", finding);
        assertEquals(Severity.MEDIUM, finding.getSeverity());
        assertTrue(finding.getMessage().contains("very low timeout"));
    }
    
    @Test
    public void testTimeoutTooHigh() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("timeout", "500");
        node.setProperty("dataSourceName", "MyDS");
        node.setLineNumber(25);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.timeout.high");
        assertNotNull("Should find high timeout", finding);
        assertEquals(Severity.MEDIUM, finding.getSeverity());
        assertTrue(finding.getMessage().contains("very high timeout"));
    }
    
    @Test
    public void testTimeoutInValidRange() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("timeout", "30");
        node.setProperty("dataSourceName", "MyDS");
        node.setProperty("useConnectionPooling", "true");
        node.setLineNumber(30);
        
        List<Finding> findings = validator.validate(node);
        
        // Should not have timeout issues
        assertNull(findByRuleId(findings, "database.connection.config.timeout.low"));
        assertNull(findByRuleId(findings, "database.connection.config.timeout.high"));
    }
    
    @Test
    public void testInvalidTimeoutValue() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("timeout", "invalid");
        node.setProperty("dataSourceName", "MyDS");
        node.setLineNumber(35);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.timeout.invalid");
        assertNotNull("Should find invalid timeout", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
    }
    
    @Test
    public void testMissingTimeout() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("dataSourceName", "MyDS");
        node.setLineNumber(40);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.timeout.missing");
        assertNotNull("Should find missing timeout", finding);
        assertEquals(Severity.LOW, finding.getSeverity());
    }
    
    @Test
    public void testUnconnectedCatchTerminal() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("dataSourceName", "MyDS");
        node.setProperty("timeout", "30");
        node.addTerminal("catch");
        node.setLineNumber(45);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.error.handling");
        assertNotNull("Should find unconnected catch terminal", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
    }
    
    @Test
    public void testTransactionModeDisabled() {
        FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
        node.setProperty("dataSourceName", "MyDS");
        node.setProperty("transactionMode", "No");
        node.setLineNumber(50);
        
        List<Finding> findings = validator.validate(node);
        
        Finding finding = findByRuleId(findings, "database.connection.config.transaction");
        assertNotNull("Should find transaction mode disabled", finding);
        assertEquals(Severity.HIGH, finding.getSeverity());
    }
    
    @Test
    public void testNonDatabaseNode() {
        FlowNode node = new FlowNode("TestCompute", "Compute", "node1");
        node.setLineNumber(55);
        
        List<Finding> findings = validator.validate(node);
        
        assertTrue("Should not validate non-database nodes", findings.isEmpty());
    }
    
    @Test
    public void testEnableDisable() {
        validator.setEnabled(false);
        assertFalse(validator.isEnabled());
        
        validator.setEnabled(true);
        assertTrue(validator.isEnabled());
    }
    
    @Test
    public void testGetApplicableNodeTypes() {
        List<String> types = validator.getApplicableNodeTypes();
        assertTrue(types.contains("DatabaseInput"));
        assertTrue(types.contains("DatabaseRetrieve"));
        assertTrue(types.contains("DatabaseRoute"));
        assertEquals(3, types.size());
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