# Testing Guide for ACE ProofChecker Validators

This guide explains how to test all the validators in the ACE ProofChecker plugin.

## Table of Contents
1. [Setup](#setup)
2. [Running Tests](#running-tests)
3. [Test Scenarios by Validator](#test-scenarios-by-validator)
4. [Manual Testing](#manual-testing)
5. [Integration Testing](#integration-testing)

---

## Setup

### Prerequisites
- JUnit 4.x
- Java 8 or higher
- Eclipse IDE with ACE Toolkit (for integration testing)

### Test Structure
```
ace-proofchecker-plugin/
├── src/
│   └── com/smartaceers/proofchecker/validators/
│       ├── DatabaseConnectionValidator.java
│       ├── HTTPRestValidator.java
│       ├── PerformanceValidator.java
│       ├── SecurityValidator.java
│       ├── NamingConventionValidator.java
│       └── BestPracticesValidator.java
└── test/
    └── com/smartaceers/proofchecker/validators/
        ├── ValidatorTestSuite.java
        ├── DatabaseConnectionValidatorTest.java
        ├── HTTPRestValidatorTest.java
        ├── PerformanceValidatorTest.java
        ├── SecurityValidatorTest.java
        ├── NamingConventionValidatorTest.java
        └── BestPracticesValidatorTest.java
```

---

## Running Tests

### Run All Tests
```bash
# Using Maven
mvn test

# Using Gradle
gradle test

# In Eclipse
Right-click on ValidatorTestSuite.java → Run As → JUnit Test
```

### Run Individual Test Class
```bash
# Using Maven
mvn test -Dtest=DatabaseConnectionValidatorTest

# In Eclipse
Right-click on test class → Run As → JUnit Test
```

### Run Specific Test Method
```bash
# Using Maven
mvn test -Dtest=DatabaseConnectionValidatorTest#testConnectionPoolingDisabled

# In Eclipse
Right-click on test method → Run As → JUnit Test
```

---

## Test Scenarios by Validator

### 1. DatabaseConnectionValidator

#### Test Scenarios:
| Scenario | Expected Result | Severity |
|----------|----------------|----------|
| Connection pooling disabled | Finding: Enable connection pooling | HIGH |
| Missing data source | Finding: Configure data source | CRITICAL |
| Timeout < 5 seconds | Finding: Increase timeout | MEDIUM |
| Timeout > 300 seconds | Finding: Decrease timeout | MEDIUM |
| Invalid timeout value | Finding: Fix timeout value | HIGH |
| Missing timeout | Finding: Configure timeout | LOW |
| Unconnected catch terminal | Finding: Connect catch terminal | HIGH |
| Transaction mode disabled | Finding: Enable transaction mode | HIGH |

#### Example Test:
```java
@Test
public void testConnectionPoolingDisabled() {
    FlowNode node = new FlowNode("TestDB", "DatabaseInput", "node1");
    node.setProperty("useConnectionPooling", "false");
    
    List<Finding> findings = validator.validate(node);
    
    assertFalse(findings.isEmpty());
    assertEquals(Severity.HIGH, findings.get(0).getSeverity());
}
```

---

### 2. HTTPRestValidator

#### Test Scenarios:
| Scenario | Expected Result | Severity |
|----------|----------------|----------|
| Timeout < 5 seconds | Finding: Increase timeout | MEDIUM |
| Timeout > 300 seconds | Finding: Decrease timeout | MEDIUM |
| Missing timeout | Finding: Configure timeout | MEDIUM |
| Unconnected catch terminal | Finding: Connect error handling | HIGH |
| Missing authentication | Finding: Configure auth | LOW |
| Basic Auth over HTTP | Finding: Use HTTPS | CRITICAL |
| Missing retry logic | Finding: Implement retry | LOW |
| Excessive retries (>10) | Finding: Reduce retries | MEDIUM |

#### Manual Test Cases:
```xml
<!-- Test Case 1: HTTP Request with low timeout -->
<HTTPRequest name="LowTimeout">
    <url>https://api.example.com</url>
    <timeout>2</timeout>
</HTTPRequest>

<!-- Test Case 2: HTTP Request without authentication -->
<HTTPRequest name="NoAuth">
    <url>https://api.example.com</url>
    <timeout>30</timeout>
</HTTPRequest>

<!-- Test Case 3: Basic Auth over HTTP (CRITICAL) -->
<HTTPRequest name="InsecureAuth">
    <url>http://api.example.com</url>
    <authenticationType>Basic</authenticationType>
</HTTPRequest>
```

---

### 3. PerformanceValidator

#### Test Scenarios:
| Scenario | Expected Result | Severity |
|----------|----------------|----------|
| Blocking operation in loop | Finding: Avoid blocking in loops | HIGH |
| Nested loops | Finding: Optimize loop structure | MEDIUM |
| String concatenation in loop | Finding: Use efficient methods | MEDIUM |
| SELECT * usage | Finding: Specify columns | LOW |
| Large message (>10MB) | Finding: Use streaming | HIGH |
| Message copying enabled | Finding: Avoid full copy | MEDIUM |
| XSLT transformation | Finding: Consider alternatives | LOW |

#### Manual Test Cases:
```esql
-- Test Case 1: Blocking operation in loop
DECLARE i INTEGER 1;
WHILE i <= 100 DO
    -- Database call inside loop (BAD)
    CALL DatabaseRetrieve(...);
    SET i = i + 1;
END WHILE;

-- Test Case 2: String concatenation in loop
DECLARE result CHARACTER '';
DECLARE i INTEGER 1;
WHILE i <= 1000 DO
    SET result = result || 'text'; -- Inefficient
    SET i = i + 1;
END WHILE;
```

---

### 4. SecurityValidator

#### Test Scenarios:
| Scenario | Expected Result | Severity |
|----------|----------------|----------|
| Hardcoded password | Finding: Use environment variables | CRITICAL |
| Credential in URL | Finding: Use headers | CRITICAL |
| HTTP instead of HTTPS | Finding: Use HTTPS | HIGH |
| Weak SSL (TLS 1.0/1.1) | Finding: Use TLS 1.2+ | HIGH |
| Unencrypted file | Finding: Enable encryption | MEDIUM |
| Sensitive data in logs | Finding: Mask sensitive data | HIGH |
| Basic Auth over HTTP | Finding: Use HTTPS | CRITICAL |
| Insecure FTP | Finding: Use SFTP | HIGH |

#### Manual Test Cases:
```xml
<!-- Test Case 1: Hardcoded password (CRITICAL) -->
<HTTPRequest name="HardcodedCreds">
    <username>admin</username>
    <password>myPassword123</password>
</HTTPRequest>

<!-- Test Case 2: Credential in URL (CRITICAL) -->
<HTTPRequest name="CredsInURL">
    <url>http://api.example.com?apikey=abc123</url>
</HTTPRequest>

<!-- Test Case 3: HTTP instead of HTTPS (HIGH) -->
<HTTPRequest name="InsecureHTTP">
    <url>http://api.example.com/data</url>
</HTTPRequest>
```

---

### 5. NamingConventionValidator

#### Test Scenarios:
| Scenario | Expected Result | Severity |
|----------|----------------|----------|
| Empty node name | Finding: Provide name | MEDIUM |
| Non-descriptive name (node1) | Finding: Use descriptive name | MEDIUM |
| Name same as type | Finding: Add context | LOW |
| Numbered suffix (Process1) | Finding: Use descriptive name | LOW |
| Wrong naming style | Finding: Follow convention | LOW |
| Name too short (<3 chars) | Finding: Use longer name | LOW |
| Name too long (>50 chars) | Finding: Use shorter name | LOW |
| Problematic prefix (temp) | Finding: Remove prefix | MEDIUM |
| Spaces in name | Finding: Remove spaces | MEDIUM |
| Special characters | Finding: Use alphanumeric | MEDIUM |

#### Manual Test Cases:
```xml
<!-- Test Case 1: Non-descriptive name -->
<Compute name="node1"/>

<!-- Test Case 2: Name with spaces -->
<Compute name="Process Customer Data"/>

<!-- Test Case 3: Problematic prefix -->
<Compute name="tempProcessing"/>

<!-- Test Case 4: Good name -->
<Compute name="ValidateCustomerData"/>
```

---

### 6. BestPracticesValidator

#### Test Scenarios:
| Scenario | Expected Result | Severity |
|----------|----------------|----------|
| Complex node (>200 lines) | Finding: Break into subflows | MEDIUM |
| Deep nesting (>4 levels) | Finding: Reduce nesting | MEDIUM |
| Missing subflow reference | Finding: Configure subflow | CRITICAL |
| No error handling | Finding: Add error handling | HIGH |
| Missing logging | Finding: Add logging | LOW |
| Excessive logging (DEBUG) | Finding: Use appropriate level | LOW |
| Error swallowing | Finding: Log errors | MEDIUM |
| Missing documentation | Finding: Add documentation | LOW |
| Manual transaction without cleanup | Finding: Ensure cleanup | LOW |
| File not closed | Finding: Close files | MEDIUM |

#### Manual Test Cases:
```esql
-- Test Case 1: Deep nesting (BAD)
IF condition1 THEN
    IF condition2 THEN
        IF condition3 THEN
            IF condition4 THEN
                IF condition5 THEN
                    -- Too deep!
                END IF;
            END IF;
        END IF;
    END IF;
END IF;

-- Test Case 2: Better approach (GOOD)
IF NOT condition1 THEN RETURN; END IF;
IF NOT condition2 THEN RETURN; END IF;
IF NOT condition3 THEN RETURN; END IF;
-- Process...
```

---

## Manual Testing

### Creating Test Message Flows

1. **Create a test message flow** in ACE Toolkit
2. **Add nodes** with various configurations
3. **Run the validator** on the flow
4. **Verify findings** match expected results

### Example Test Flow:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ecore:EPackage xmi:version="2.0">
    <nodes xmi:type="ComIbmMQInput.msgnode">
        <name>MQInput</name>
        <transactionMode>No</transactionMode>
    </nodes>
    <nodes xmi:type="ComIbmCompute.msgnode">
        <name>Compute1</name>
        <!-- No catch terminal connected -->
    </nodes>
    <nodes xmi:type="ComIbmHTTPRequest.msgnode">
        <name>HTTPRequest</name>
        <url>http://api.example.com</url>
        <timeout>2</timeout>
    </nodes>
</ecore:EPackage>
```

---

## Integration Testing

### Test with Real ACE Flows

1. **Export existing ACE flows** to test
2. **Run validators** on production-like flows
3. **Verify findings** are actionable
4. **Measure performance** on large flows

### Performance Benchmarks:
- Small flow (10 nodes): < 100ms
- Medium flow (50 nodes): < 500ms
- Large flow (200 nodes): < 2 seconds

### Integration Test Checklist:
- [ ] Test with MQ Input nodes
- [ ] Test with HTTP/REST nodes
- [ ] Test with Database nodes
- [ ] Test with Compute nodes
- [ ] Test with error handling flows
- [ ] Test with subflows
- [ ] Test with complex transformations
- [ ] Test with security configurations

---

## Continuous Integration

### Jenkins/CI Pipeline:
```groovy
pipeline {
    stages {
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Integration Tests') {
            steps {
                sh 'mvn verify'
            }
        }
        stage('Coverage Report') {
            steps {
                jacoco()
            }
        }
    }
}
```

### Coverage Goals:
- Line Coverage: > 80%
- Branch Coverage: > 70%
- Method Coverage: > 90%

---

## Troubleshooting

### Common Issues:

1. **Tests fail with NullPointerException**
   - Ensure FlowNode is properly initialized
   - Check that properties are set before validation

2. **Findings not detected**
   - Verify node type matches validator's applicable types
   - Check property names match expected values

3. **False positives**
   - Review validation logic
   - Add test cases for edge cases

---

## Test Data

### Sample FlowNode Creation:
```java
// Create a database node with issues
FlowNode dbNode = new FlowNode("MyDatabase", "DatabaseInput", "node1");
dbNode.setProperty("useConnectionPooling", "false");
dbNode.setProperty("timeout", "2");
dbNode.setLineNumber(10);

// Create an HTTP node with security issues
FlowNode httpNode = new FlowNode("MyHTTP", "HTTPRequest", "node2");
httpNode.setProperty("url", "http://api.example.com");
httpNode.setProperty("password", "hardcoded123");
httpNode.setLineNumber(20);

// Create a compute node with performance issues
FlowNode computeNode = new FlowNode("MyCompute", "Compute", "node3");
computeNode.setProperty("code", generateLargeCode(300)); // 300 lines
computeNode.setLineNumber(30);
```

---

## Best Practices for Writing Tests

1. **Test one thing at a time**
2. **Use descriptive test names**
3. **Follow AAA pattern** (Arrange, Act, Assert)
4. **Test both positive and negative cases**
5. **Use helper methods** to reduce duplication
6. **Mock external dependencies**
7. **Keep tests fast** (< 1 second each)
8. **Make tests independent**

---

## Reporting Issues

When reporting test failures:
1. Include test name and class
2. Provide stack trace
3. Describe expected vs actual behavior
4. Include test data used
5. Note environment details

---

## Additional Resources

- [JUnit Documentation](https://junit.org/junit4/)
- [ACE Toolkit Documentation](https://www.ibm.com/docs/en/app-connect/)
- [Testing Best Practices](https://martinfowler.com/articles/practical-test-pyramid.html)

---

**Last Updated**: 2026-06-18  
**Version**: 1.0.0  
**Author**: SmartACEers Team