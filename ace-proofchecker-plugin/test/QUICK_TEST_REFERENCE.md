# Quick Test Reference Guide

## Running Tests - Quick Commands

### Run All Tests
```bash
# Eclipse: Right-click ValidatorTestSuite.java → Run As → JUnit Test
# Maven: mvn test
# Gradle: gradle test
```

### Run Single Validator Tests
```bash
mvn test -Dtest=DatabaseConnectionValidatorTest
mvn test -Dtest=SecurityValidatorTest
mvn test -Dtest=PerformanceValidatorTest
```

---

## Quick Test Scenarios

### 1. Database Connection Validator

```java
// ❌ BAD: Connection pooling disabled
node.setProperty("useConnectionPooling", "false");

// ✅ GOOD: Connection pooling enabled
node.setProperty("useConnectionPooling", "true");

// ❌ BAD: Timeout too low
node.setProperty("timeout", "2");

// ✅ GOOD: Timeout in valid range
node.setProperty("timeout", "30");
```

### 2. HTTP/REST Validator

```java
// ❌ BAD: HTTP instead of HTTPS
node.setProperty("url", "http://api.example.com");

// ✅ GOOD: HTTPS
node.setProperty("url", "https://api.example.com");

// ❌ BAD: Basic Auth over HTTP
node.setProperty("url", "http://api.example.com");
node.setProperty("authenticationType", "Basic");

// ✅ GOOD: Basic Auth over HTTPS
node.setProperty("url", "https://api.example.com");
node.setProperty("authenticationType", "Basic");
```

### 3. Performance Validator

```java
// ❌ BAD: Large message without streaming
node.setProperty("maxMessageSize", "15000000"); // 15MB

// ✅ GOOD: Reasonable message size
node.setProperty("maxMessageSize", "5000000"); // 5MB

// ❌ BAD: Message copying enabled
node.setProperty("copyMessage", "true");

// ✅ GOOD: No message copying
node.setProperty("copyMessage", "false");
```

### 4. Security Validator

```java
// ❌ BAD: Hardcoded password
node.setProperty("password", "myPassword123");

// ✅ GOOD: Environment variable
node.setProperty("password", "${Environment.PASSWORD}");

// ❌ BAD: Credential in URL
node.setProperty("url", "http://api.com?apikey=abc123");

// ✅ GOOD: Credential in header
node.setProperty("url", "https://api.com");
node.setProperty("apiKey", "${Environment.API_KEY}");
```

### 5. Naming Convention Validator

```java
// ❌ BAD: Non-descriptive name
FlowNode node = new FlowNode("node1", "Compute", "id1");

// ✅ GOOD: Descriptive name
FlowNode node = new FlowNode("ValidateCustomerData", "Compute", "id1");

// ❌ BAD: Name with spaces
FlowNode node = new FlowNode("Process Data", "Compute", "id1");

// ✅ GOOD: PascalCase
FlowNode node = new FlowNode("ProcessData", "Compute", "id1");
```

### 6. Best Practices Validator

```java
// ❌ BAD: No error handling
FlowNode node = new FlowNode("Process", "Compute", "id1");
// No catch terminal connected

// ✅ GOOD: Error handling configured
FlowNode node = new FlowNode("Process", "Compute", "id1");
node.addTerminal("catch");
// Connect catch terminal to error handler

// ❌ BAD: No logging
node.setProperty("logging", "false");

// ✅ GOOD: Logging enabled
node.setProperty("logging", "true");
```

---

## Test Assertion Examples

```java
// Check finding exists
Finding finding = findByRuleId(findings, "security.validation.hardcoded.password");
assertNotNull("Should find hardcoded password", finding);

// Check severity
assertEquals(Severity.CRITICAL, finding.getSeverity());

// Check message content
assertTrue(finding.getMessage().contains("hardcoded password"));

// Check no findings
assertTrue("Should have no findings", findings.isEmpty());

// Check finding count
assertEquals(2, findings.size());
```

---

## Common Test Patterns

### Pattern 1: Test for Issue Detection
```java
@Test
public void testDetectsIssue() {
    // Arrange
    FlowNode node = new FlowNode("Test", "Type", "id");
    node.setProperty("key", "bad_value");
    
    // Act
    List<Finding> findings = validator.validate(node);
    
    // Assert
    assertFalse(findings.isEmpty());
    assertEquals(Severity.HIGH, findings.get(0).getSeverity());
}
```

### Pattern 2: Test for No Issue
```java
@Test
public void testNoIssueWhenCorrect() {
    // Arrange
    FlowNode node = new FlowNode("Test", "Type", "id");
    node.setProperty("key", "good_value");
    
    // Act
    List<Finding> findings = validator.validate(node);
    
    // Assert
    assertTrue(findings.isEmpty());
}
```

### Pattern 3: Test Multiple Issues
```java
@Test
public void testMultipleIssues() {
    // Arrange
    FlowNode node = new FlowNode("Test", "Type", "id");
    node.setProperty("issue1", "bad");
    node.setProperty("issue2", "bad");
    
    // Act
    List<Finding> findings = validator.validate(node);
    
    // Assert
    assertTrue(findings.size() >= 2);
    assertNotNull(findByRuleId(findings, "rule.id.1"));
    assertNotNull(findByRuleId(findings, "rule.id.2"));
}
```

---

## Severity Levels

| Severity | When to Use | Example |
|----------|-------------|---------|
| CRITICAL | Data loss, security breach, system failure | Hardcoded passwords, unencrypted HTTP with credentials |
| HIGH | Significant issues, potential failures | Missing error handling, connection pooling disabled |
| MEDIUM | Performance issues, maintainability | Timeout too low/high, deep nesting |
| LOW | Suggestions, best practices | Missing documentation, naming conventions |

---

## Test Coverage Checklist

### Per Validator:
- [ ] Test validator metadata (ID, name, category)
- [ ] Test appliesTo() for correct node types
- [ ] Test each validation rule
- [ ] Test positive cases (no issues)
- [ ] Test negative cases (issues found)
- [ ] Test edge cases
- [ ] Test enable/disable functionality
- [ ] Test with null/empty values
- [ ] Test with invalid values
- [ ] Test multiple issues in one node

---

## Debugging Tests

### Enable Verbose Output:
```java
@Test
public void testWithDebug() {
    List<Finding> findings = validator.validate(node);
    
    // Print all findings for debugging
    for (Finding f : findings) {
        System.out.println("Rule: " + f.getRuleId());
        System.out.println("Severity: " + f.getSeverity());
        System.out.println("Message: " + f.getMessage());
        System.out.println("---");
    }
    
    assertFalse(findings.isEmpty());
}
```

### Common Debug Points:
1. Check node type matches validator's applicable types
2. Verify property names are correct
3. Ensure property values are set
4. Check terminal connections
5. Verify line numbers are set

---

## Performance Testing

```java
@Test
public void testPerformance() {
    FlowNode node = createTestNode();
    
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < 1000; i++) {
        validator.validate(node);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Should complete 1000 validations in < 1 second
    assertTrue("Validation too slow: " + duration + "ms", duration < 1000);
}
```

---

## Integration Test Example

```java
@Test
public void testWithRealFlow() {
    // Create a realistic flow
    FlowNode mqInput = new FlowNode("MQInput", "MQInput", "1");
    mqInput.setProperty("transactionMode", "No");
    
    FlowNode compute = new FlowNode("Transform", "Compute", "2");
    compute.addTerminal("catch");
    
    FlowNode httpRequest = new FlowNode("CallAPI", "HTTPRequest", "3");
    httpRequest.setProperty("url", "http://api.example.com");
    
    // Validate all nodes
    List<Finding> allFindings = new ArrayList<>();
    allFindings.addAll(mqValidator.validate(mqInput));
    allFindings.addAll(catchValidator.validate(compute));
    allFindings.addAll(httpValidator.validate(httpRequest));
    
    // Should find multiple issues
    assertTrue(allFindings.size() >= 3);
}
```

---

## CI/CD Integration

### GitHub Actions Example:
```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '8'
      - name: Run tests
        run: mvn test
      - name: Generate coverage
        run: mvn jacoco:report
```

---

## Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| Test fails with NPE | Check FlowNode initialization |
| Finding not detected | Verify node type and properties |
| Wrong severity | Check severity constants |
| Test too slow | Reduce test data size |
| Flaky test | Remove time-dependent logic |

---

**Pro Tip**: Run tests frequently during development to catch issues early!

**Last Updated**: 2026-06-18  
**Version**: 1.0.0