# How to Test Each Validation - Step-by-Step Guide

## Testing Approach

There are **3 ways** to test the validators:

1. **Unit Tests** (Automated) - Test individual validators with mock data
2. **Integration Tests** (Automated) - Test with real .msgflow files
3. **Manual Testing** (In ACE Toolkit) - Create flows and run the plugin

---

## 1. Unit Testing (Recommended for Development)

### Setup
```bash
# Navigate to test directory
cd ace-proofchecker-plugin/test

# Run all tests
# In Eclipse: Right-click on ValidatorTestSuite.java → Run As → JUnit Plug-in Test
```

### Test Each Validator Individually

#### A. **MQTransactionValidator Test**
```java
// File: MQTransactionValidatorTest.java
@Test
public void testTransactionModeDisabled() {
    // Create mock MQInput node
    FlowNode node = new FlowNode("TestMQInput", "MQInput", "node1");
    node.setProperty("transactionMode", "No");
    
    // Run validator
    MQTransactionValidator validator = new MQTransactionValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find 1 CRITICAL issue
    assertEquals(1, findings.size());
    assertEquals(Severity.CRITICAL, findings.get(0).getSeverity());
}

@Test
public void testTransactionModeEnabled() {
    FlowNode node = new FlowNode("TestMQInput", "MQInput", "node1");
    node.setProperty("transactionMode", "Yes");
    
    MQTransactionValidator validator = new MQTransactionValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find NO issues
    assertEquals(0, findings.size());
}
```

#### B. **SecurityValidator Test**
```java
@Test
public void testHardcodedPassword() {
    FlowNode node = new FlowNode("TestNode", "Compute", "node1");
    node.setProperty("password", "myPassword123");
    
    SecurityValidator validator = new SecurityValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find CRITICAL issue
    assertTrue(findings.size() > 0);
    assertEquals(Severity.CRITICAL, findings.get(0).getSeverity());
}

@Test
public void testHttpInsteadOfHttps() {
    FlowNode node = new FlowNode("TestHTTP", "HTTPRequest", "node1");
    node.setProperty("url", "http://api.example.com");
    
    SecurityValidator validator = new SecurityValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find HIGH severity issue
    assertTrue(findings.stream()
        .anyMatch(f -> f.getSeverity() == Severity.HIGH));
}
```

#### C. **CatchTerminalValidator Test**
```java
@Test
public void testUnconnectedCatchTerminal() {
    FlowNode node = new FlowNode("TestMQInput", "MQInput", "node1");
    // Add catch terminal but don't connect it
    node.addTerminal("catch");
    
    CatchTerminalValidator validator = new CatchTerminalValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find CRITICAL issue
    assertEquals(1, findings.size());
    assertEquals(Severity.CRITICAL, findings.get(0).getSeverity());
}

@Test
public void testConnectedCatchTerminal() {
    FlowNode node = new FlowNode("TestMQInput", "MQInput", "node1");
    node.addTerminal("catch");
    
    // Simulate connection
    FlowConnection conn = new FlowConnection("TestMQInput", "catch", "ErrorHandler", "in");
    node.addOutgoingConnection(conn);
    
    CatchTerminalValidator validator = new CatchTerminalValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find NO issues
    assertEquals(0, findings.size());
}
```

#### D. **NamingConventionValidator Test**
```java
@Test
public void testNonDescriptiveName() {
    FlowNode node = new FlowNode("Compute1", "Compute", "node1");
    
    NamingConventionValidator validator = new NamingConventionValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find naming issue
    assertTrue(findings.size() > 0);
}

@Test
public void testDescriptiveName() {
    FlowNode node = new FlowNode("ValidateCustomerData", "Compute", "node1");
    
    NamingConventionValidator validator = new NamingConventionValidator();
    List<Finding> findings = validator.validate(node);
    
    // Assert: Should find NO issues (or only style issues)
    assertTrue(findings.stream()
        .noneMatch(f -> f.getRuleId().contains("non.descriptive")));
}
```

---

## 2. Integration Testing (Test with Real .msgflow Files)

### Create Test Message Flows

#### Test Flow 1: MQInput with Issues
```xml
<!-- File: test-flows/mqinput-bad.msgflow -->
<!-- Create in ACE Toolkit with: -->
1. MQInput node named "MQInput1"
2. Set Transaction Mode = "No"
3. Don't connect catch terminal
4. Save as mqinput-bad.msgflow
```

#### Test Flow 2: HTTP with Security Issues
```xml
<!-- File: test-flows/http-insecure.msgflow -->
<!-- Create in ACE Toolkit with: -->
1. HTTPRequest node
2. Set URL = "http://api.example.com" (not HTTPS)
3. Set password property = "hardcoded123"
4. Save as http-insecure.msgflow
```

### Run Integration Tests
```java
@Test
public void testRealMsgFlowFile() throws Exception {
    String flowPath = "test-flows/mqinput-bad.msgflow";
    
    MessageFlowParser parser = new MessageFlowParser();
    List<FlowNode> nodes = parser.parse(flowPath);
    
    ValidationEngine engine = new ValidationEngine();
    engine.registerValidator(new MQTransactionValidator());
    engine.registerValidator(new CatchTerminalValidator());
    
    ValidationContext context = engine.validate(flowPath);
    
    // Assert: Should find multiple issues
    assertTrue(context.getFindings().size() > 0);
    assertTrue(context.hasCriticalFindings());
}
```

---

## 3. Manual Testing (In ACE Toolkit)

### Prerequisites
1. Install ACE Toolkit
2. Install the Proofchecker Plugin
3. Create test workspace

### Test Scenarios

#### Scenario 1: Test MQInput Validation
```
Steps:
1. Create new message flow
2. Add MQInput node
3. Name it "MQInput1" (non-descriptive)
4. Set Transaction Mode = "No"
5. Don't connect catch terminal
6. Save flow
7. Right-click flow → Run Proofcheck

Expected Results:
✓ CRITICAL: Transaction mode disabled
✓ CRITICAL: Catch terminal not connected
✓ MEDIUM: Non-descriptive name
```

#### Scenario 2: Test HTTP Security
```
Steps:
1. Create new message flow
2. Add HTTPRequest node
3. Set URL = "http://api.example.com"
4. Add property: password = "test123"
5. Set timeout = 2 seconds
6. Don't connect catch terminal
7. Save and run Proofcheck

Expected Results:
✓ CRITICAL: Hardcoded password
✓ HIGH: HTTP instead of HTTPS
✓ MEDIUM: Timeout too low
✓ HIGH: Catch terminal not connected
```

#### Scenario 3: Test Database Connection
```
Steps:
1. Add DatabaseRetrieve node
2. Leave data source empty
3. Set timeout = 500 seconds
4. Disable connection pooling
5. Don't connect catch terminal
6. Save and run Proofcheck

Expected Results:
✓ CRITICAL: No data source
✓ HIGH: Connection pooling disabled
✓ MEDIUM: Timeout too high
✓ HIGH: Catch terminal not connected
```

#### Scenario 4: Test Performance Issues
```
Steps:
1. Add ForEach loop node
2. Inside loop, add DatabaseRetrieve node
3. Inside loop, add HTTPRequest node
4. Add Compute node with 250 lines of code
5. Save and run Proofcheck

Expected Results:
✓ HIGH: Blocking database operation in loop
✓ HIGH: Blocking HTTP operation in loop
✓ MEDIUM: Complex node (too many lines)
```

#### Scenario 5: Test Naming Conventions
```
Steps:
1. Add Compute node named "Compute"
2. Add Compute node named "temp1"
3. Add Compute node named "Process 1" (with space)
4. Add Compute node named "x"
5. Save and run Proofcheck

Expected Results:
✓ LOW: Name same as type
✓ MEDIUM: Problematic prefix "temp"
✓ MEDIUM: Spaces in name
✓ LOW: Name too short
```

---

## 4. Automated Test Suite Execution

### Run All Tests
```bash
# In Eclipse
1. Open ValidatorTestSuite.java
2. Right-click → Run As → JUnit Plug-in Test
3. View results in JUnit view

# Expected Output:
✓ MQTransactionValidatorTest: 5 tests passed
✓ CatchTerminalValidatorTest: 4 tests passed
✓ SecurityValidatorTest: 8 tests passed
✓ DatabaseConnectionValidatorTest: 6 tests passed
✓ NamingConventionValidatorTest: 7 tests passed
✓ PerformanceValidatorTest: 5 tests passed
✓ HTTPRestValidatorTest: 6 tests passed
✓ BestPracticesValidatorTest: 8 tests passed

Total: 49 tests, 0 failures
```

---

## 5. Verification Checklist

After running tests, verify:

- [ ] All unit tests pass
- [ ] Integration tests with real .msgflow files work
- [ ] Manual testing in ACE Toolkit shows findings in Problems view
- [ ] Markers appear on correct line numbers
- [ ] Severity levels are correct (CRITICAL, HIGH, MEDIUM, LOW)
- [ ] Suggestions are helpful and actionable
- [ ] No false positives
- [ ] No false negatives

---

## Quick Test Commands

```bash
# Run specific validator test
mvn test -Dtest=MQTransactionValidatorTest

# Run all validator tests
mvn test -Dtest=ValidatorTestSuite

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## Test Data Location

```
ace-proofchecker-plugin/
├── test/
│   ├── com/smartaceers/proofchecker/validators/
│   │   ├── MQTransactionValidatorTest.java
│   │   ├── SecurityValidatorTest.java
│   │   ├── CatchTerminalValidatorTest.java
│   │   └── ValidatorTestSuite.java
│   ├── test-flows/
│   │   ├── mqinput-bad.msgflow
│   │   ├── http-insecure.msgflow
│   │   ├── database-issues.msgflow
│   │   └── performance-problems.msgflow
│   ├── TESTING_GUIDE.md
│   └── QUICK_TEST_REFERENCE.md
```

---

## 6. BestPracticesValidator — Full Test Plan

The `BestPracticesValidator` runs on **every** node (`appliesTo` returns `true`)
and checks flow structure, error propagation, documentation, and resource
management.

> **Note:** `validateLogging(...)` is currently commented out in `validate()`,
> so the `best.practices.missing.logging` and `best.practices.excessive.logging`
> rules **do not run**. The scenarios below reflect what actually executes.

### 6.1 Is the validator even executing?

`BestPracticesValidator` is the only validator that dumps **all node properties**
and a **per-node findings summary** to the log. Use that as the execution proof.

**Steps:**

1. Run Proofcheck on any `.msgflow` (right-click → **Run Proofcheck**).
2. Open the newest log: `~/.ace-proofcheck-logs/validation_<flow>_<timestamp>.log`.
3. Confirm these markers exist — they are printed **only** by this validator:
   - Registration: `✓ BestPracticesValidator registered`
   - Per node: `Node Properties for: <nodeName>` (from `listNodeProperties`)
   - Per node: `>>> No findings for node: <nodeName>` **or**
     `Findings for Node: <nodeName>` (from `listFindingsWithSuggestions`)

If you see `Node Properties for:` lines, the validator ran on those nodes.

```bash
# Quick check from a terminal (newest log)
LOG=$(ls -t ~/.ace-proofcheck-logs/validation_*.log | head -1)
grep -c "Node Properties for:" "$LOG"     # > 0 means it executed
grep -nE "Findings for Node:|No findings for node:" "$LOG"
```

### 6.2 Every scenario it checks

| # | Rule ID | Triggers when… | Node type | Severity |
|---|---------|----------------|-----------|----------|
| 1 | `best.practices.complex.node` | `code` property > 200 lines | Compute / JavaCompute | MEDIUM |
| 2 | `best.practices.deep.nesting` | `code` nesting depth > 4 | Compute / JavaCompute | MEDIUM |
| 3 | `best.practices.missing.subflow` | `subflowName` empty/absent | Subflow | CRITICAL |
| 4 | `best.practices.error.swallowing` | `catchAction` = `ignore`/`suppress` | TryCatch | MEDIUM |
| 5 | `best.practices.missing.documentation` | no `description` **and** no `comments` | Compute / JavaCompute | LOW |
| 6 | `best.practices.manual.transaction` | `autoCommit` = `false` | DatabaseRetrieve / DatabaseInput | LOW |
| 7 | `best.practices.resource.leak` | `closeFile` not `true`/`Yes` | FileRead / FileWrite | MEDIUM |
| 8 | `best.practices.nested.if` | **external** `.esql`/`.java` source has IF nesting depth ≥ 4 | Compute (ESQL) / JavaCompute (Java) | HIGH |

> Catch-terminal connection checks are **not** part of BestPracticesValidator.
> They are handled only by `CatchTerminalValidator`, limited to MQInput /
> HTTPInput / FileInput nodes. Failure-terminal checks were removed entirely.

> **Rule 9 — external source nesting (real flows).** Scenarios 1, 2, and the code
> part of 6 read an inline `code` property, which a real ACE `.msgflow` does
> **not** contain (the Compute node only references an external `.esql` module via
> `computeExpression="esql://…"`, and JavaCompute references a `.java` class). To
> handle real flows, rule 9 loads the external source from the flow's directory
> and flags the node when the IF-nesting depth reaches 4. It resolves the `.esql`
> module by name (from `computeExpression`, the node name, or the single `.esql`
> file present) and the `.java` file by the node's `javaClass`. Drop a file such
> as `BestPracticeValidator_Compute.esql` next to the flow to exercise it.
> `missing.documentation` still fires on a real Compute node because it only needs
> `description`/`comments` to be absent.

### 6.3 ESQL to trigger the code-based rules

Put this ESQL in the Compute node's module (or set it as the `code` property in a
unit test).

**Deep nesting (> 4) → `best.practices.deep.nesting`:**

```sql
CREATE COMPUTE MODULE Transform_Compute
    CREATE FUNCTION Main() RETURNS BOOLEAN
    BEGIN
        IF InputRoot.XMLNSC.Order.Type = 'A' THEN
            IF InputRoot.XMLNSC.Order.Region = 'EU' THEN
                IF InputRoot.XMLNSC.Order.Priority = 'High' THEN
                    IF InputRoot.XMLNSC.Order.Amount > 1000 THEN
                        IF InputRoot.XMLNSC.Order.Currency = 'USD' THEN
                            SET OutputRoot.XMLNSC.Result = 'deep';
                        END IF;
                    END IF;
                END IF;
            END IF;
        END IF;
        RETURN TRUE;
    END;
END MODULE;
```

The nesting counter increments on `IF/WHILE/FOR/LOOP/BEGIN` and decrements on
`END IF/END WHILE/END FOR/END LOOP/END;`. Five nested `IF`s → depth 5 → fires.

**Overly complex node (> 200 lines) → `best.practices.complex.node`:**
any module whose body exceeds 200 lines (e.g. 200+ `SET …;` statements).

**No documentation → `best.practices.missing.documentation`:**
a Compute/JavaCompute node with **no** `description` and **no** `comments`
property set (the default for most hand-built nodes).

**Steps (manual, in ACE Toolkit):**
1. Create a flow with a Compute node using the deep-nesting ESQL above.
2. For scenarios 1/2 to register, set the node's `code` property (see caveat) or
   run the unit tests in §6.4.
3. Right-click the `.msgflow` → **Run Proofcheck**.
4. Check the **Problems** view and the log for the rule IDs above.

### 6.4 Java unit tests — all scenarios

Save as `test/com/smartaceers/proofchecker/validators/BestPracticesValidatorTest.java`
and run: right-click → **Run As → JUnit Plug-in Test** (run this class directly;
the shared `ValidatorTestSuite` references other test classes that may not exist).

```java
package com.smartaceers.proofchecker.validators;

import static org.junit.Assert.*;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.parser.FlowNode;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class BestPracticesValidatorTest {

    private BestPracticesValidator validator;

    @Before
    public void setUp() {
        validator = new BestPracticesValidator();
    }

    @Test
    public void testMetadata() {
        assertEquals("best.practices", validator.getValidatorId());
        assertEquals("Best Practices Validator", validator.getValidatorName());
        assertTrue(validator.isEnabled());
        assertTrue(validator.appliesTo("Compute")); // applies to all types
    }

    // 1. complex.node (> 200 lines)
    @Test
    public void testComplexNode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            code.append("SET OutputRoot.XMLNSC.F").append(i).append(" = ").append(i).append(";\n");
        }
        FlowNode node = new FlowNode("BigCompute", "Compute", "n1");
        node.setProperty("code", code.toString());
        node.setProperty("description", "documented"); // isolate from missing.documentation

        assertNotNull(findByRuleId(validator.validate(node), "best.practices.complex.node"));
    }

    // 2. deep.nesting (> 4)
    @Test
    public void testDeepNesting() {
        String code =
            "IF a = 1 THEN\n" +
            "  IF b = 2 THEN\n" +
            "    IF c = 3 THEN\n" +
            "      IF d = 4 THEN\n" +
            "        IF e = 5 THEN\n" +
            "          SET x = 1;\n" +
            "        END IF;\n" +
            "      END IF;\n" +
            "    END IF;\n" +
            "  END IF;\n" +
            "END IF;\n";
        FlowNode node = new FlowNode("NestedCompute", "Compute", "n2");
        node.setProperty("code", code);
        node.setProperty("description", "documented");

        Finding f = findByRuleId(validator.validate(node), "best.practices.deep.nesting");
        assertNotNull("Should flag deep nesting", f);
        assertEquals(Severity.MEDIUM, f.getSeverity());
    }

    // 3. missing.subflow (CRITICAL)
    @Test
    public void testMissingSubflowReference() {
        FlowNode node = new FlowNode("CallSub", "Subflow", "n3"); // no subflowName
        Finding f = findByRuleId(validator.validate(node), "best.practices.missing.subflow");
        assertNotNull(f);
        assertEquals(Severity.CRITICAL, f.getSeverity());
    }

    // 4. error.swallowing (MEDIUM)
    @Test
    public void testErrorSwallowing() {
        FlowNode node = new FlowNode("Guard", "TryCatch", "n5");
        node.setProperty("catchAction", "ignore");
        Finding f = findByRuleId(validator.validate(node), "best.practices.error.swallowing");
        assertNotNull(f);
        assertEquals(Severity.MEDIUM, f.getSeverity());
    }

    // 6. missing.documentation (LOW)
    @Test
    public void testMissingDocumentation() {
        FlowNode node = new FlowNode("Plain", "Compute", "n6");
        node.setProperty("code", "SET x = 1;"); // short code, no description/comments
        Finding f = findByRuleId(validator.validate(node), "best.practices.missing.documentation");
        assertNotNull(f);
        assertEquals(Severity.LOW, f.getSeverity());
    }

    // 7. manual.transaction (LOW)
    @Test
    public void testManualTransaction() {
        FlowNode node = new FlowNode("DbRead", "DatabaseRetrieve", "n7");
        node.setProperty("autoCommit", "false");
        Finding f = findByRuleId(validator.validate(node), "best.practices.manual.transaction");
        assertNotNull(f);
        assertEquals(Severity.LOW, f.getSeverity());
    }

    // 8. resource.leak (MEDIUM)
    @Test
    public void testFileResourceLeak() {
        FlowNode node = new FlowNode("Writer", "FileWrite", "n8"); // closeFile not set
        Finding f = findByRuleId(validator.validate(node), "best.practices.resource.leak");
        assertNotNull(f);
        assertEquals(Severity.MEDIUM, f.getSeverity());
    }

    // 9. clean node — no false positives
    @Test
    public void testCleanNodeHasNoFindings() {
        FlowNode node = new FlowNode("GoodCompute", "Compute", "n9");
        node.setProperty("code", "SET x = 1;");
        node.setProperty("description", "Maps order to canonical model");
        assertTrue(validator.validate(node).isEmpty());
    }

    private Finding findByRuleId(List<Finding> findings, String ruleId) {
        for (Finding f : findings) {
            if (f.getRuleId().equals(ruleId)) {
                return f;
            }
        }
        return null;
    }
}
```

---

## Complete Validation Coverage

### All Validators and Their Tests

| Validator | Test File | Test Count | Coverage |
|-----------|-----------|------------|----------|
| MQTransactionValidator | MQTransactionValidatorTest.java | 5 | Transaction mode checks |
| CatchTerminalValidator | CatchTerminalValidatorTest.java | 4 | Catch terminal connection |
| SecurityValidator | SecurityValidatorTest.java | 8 | Credentials, encryption, protocols |
| DatabaseConnectionValidator | DatabaseConnectionValidatorTest.java | 6 | Pooling, timeouts, data sources |
| HTTPRestValidator | HTTPRestValidatorTest.java | 6 | Timeouts, auth, retry logic |
| PerformanceValidator | PerformanceValidatorTest.java | 5 | Loops, message size, patterns |
| NamingConventionValidator | NamingConventionValidatorTest.java | 7 | Names, styles, conventions |
| BestPracticesValidator | BestPracticesValidatorTest.java | 8 | Error handling, logging, docs |

---

## Troubleshooting

### Common Issues

1. **Tests fail with "Git not installed"**
   - This is expected if Git is not available
   - Tests should still run for validators

2. **Cannot find .msgflow files**
   - Ensure test-flows directory exists
   - Check file paths are relative to project root

3. **Markers not appearing in ACE Toolkit**
   - Verify plugin is installed correctly
   - Check plugin.xml marker extension is registered
   - Restart ACE Toolkit

4. **False positives/negatives**
   - Check terminal discovery is working (connections parsed correctly)
   - Verify node properties are being read
   - Review parser logs for errors

---

Follow these steps to thoroughly test all validations!