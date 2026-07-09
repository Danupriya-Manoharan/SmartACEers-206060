# Production Issues Test Flow

## Overview
`ProductionIssuesTest.msgflow` is a comprehensive test message flow that demonstrates **20 common production issues** that the ACE Proofchecker plugin should detect.

## Issues Demonstrated

### 1. Transaction Management Issues (Critical)

#### Issue 1: MQ Input with Transaction Mode = No
- **Node**: `MQ Input No Transaction` (FCMComposite_1_1)
- **Problem**: Messages will be lost if processing fails
- **Severity**: CRITICAL
- **Category**: Transaction Management
- **Detection**: `transactionMode="no"` on MQ Input node

#### Issue 12: MQ Input with No Backout Threshold
- **Node**: `MQ Input No Backout Threshold` (FCMComposite_1_12)
- **Problem**: Poison messages will cause infinite retry loops
- **Severity**: HIGH
- **Category**: Transaction Management
- **Detection**: `backoutThreshold="0"` on MQ Input node

#### Issue 5: MQ Output without Transaction Coordination
- **Node**: `MQ Output No Transaction` (FCMComposite_1_5)
- **Problem**: Can cause duplicate messages or data inconsistency
- **Severity**: HIGH
- **Category**: Transaction Management
- **Detection**: `transactionMode="no"` on MQ Output node

### 2. Error Handling Issues (Critical)

#### Issue 2: No Catch Terminal Connected
- **Node**: `Compute No Error Handler` (FCMComposite_1_2)
- **Problem**: Unhandled exceptions will crash the flow
- **Severity**: CRITICAL
- **Category**: Error Handling
- **Detection**: Compute node with no connection from Catch terminal

#### Issue 16: HTTP Request without Error Handling
- **Node**: `HTTP No Error Handling` (FCMComposite_1_16)
- **Problem**: HTTP failures not handled, no retry logic
- **Severity**: HIGH
- **Category**: Error Handling
- **Detection**: HTTPRequest node with no Failure terminal connection

### 3. Performance Issues

#### Issue 4: HTTP Request without Timeout
- **Node**: `HTTP Request No Timeout` (FCMComposite_1_4)
- **Problem**: Requests can hang indefinitely
- **Severity**: HIGH
- **Category**: Performance
- **Detection**: `requestTimeout="0"` on HTTPRequest node

#### Issue 13: Database with SELECT *
- **Node**: `Database SELECT ALL` (FCMComposite_1_13)
- **Problem**: Retrieves unnecessary columns, wastes memory and network
- **Severity**: MEDIUM
- **Category**: Performance
- **Detection**: SQL statement contains `SELECT *`

#### Issue 17: Database without Timeout
- **Node**: `Database No Timeout` (FCMComposite_1_17)
- **Problem**: Queries can hang indefinitely
- **Severity**: HIGH
- **Category**: Performance
- **Detection**: Database node with no timeout configured

#### Issue 11: Compute with Complex Logic
- **Node**: `Compute Complex Loop` (FCMComposite_1_11)
- **Problem**: Nested loops with XPath queries cause performance degradation
- **Severity**: MEDIUM
- **Category**: Performance
- **Detection**: Complex ESQL with nested loops (requires code analysis)

#### Issue 19: MQ Get with Infinite Wait
- **Node**: `MQ Get Infinite Wait` (FCMComposite_1_19)
- **Problem**: Flow will hang waiting for messages
- **Severity**: HIGH
- **Category**: Performance
- **Detection**: `waitInterval="-1"` on MQGet node

### 4. Security Issues (Critical)

#### Issue 8: Compute with Hardcoded Credentials
- **Node**: `Compute Hardcoded Creds` (FCMComposite_1_8)
- **Problem**: Credentials exposed in code, security breach risk
- **Severity**: CRITICAL
- **Category**: Security
- **Detection**: ESQL contains hardcoded credentials or Authorization headers

#### Issue 10: HTTP Request using HTTP instead of HTTPS
- **Node**: `HTTP Not HTTPS` (FCMComposite_1_10)
- **Problem**: Data transmitted in plain text, vulnerable to interception
- **Severity**: HIGH
- **Category**: Security
- **Detection**: `protocol="HTTP"` on HTTPRequest node

#### Issue 18: Compute with Sensitive Data Logging
- **Node**: `Compute Logs Sensitive Data` (FCMComposite_1_18)
- **Problem**: Credit card numbers or PII logged, compliance violation
- **Severity**: CRITICAL
- **Category**: Security
- **Detection**: ESQL contains logging of sensitive fields

#### Issue 20: HTTP Request with SSL Validation Disabled
- **Node**: `HTTP SSL Validation Disabled` (FCMComposite_1_20)
- **Problem**: Man-in-the-middle attacks possible
- **Severity**: CRITICAL
- **Category**: Security
- **Detection**: `validateTiming="never"` on HTTPRequest node

### 5. Configuration Issues

#### Issue 15: MQ Output with Hardcoded Queue Name
- **Node**: `MQ Output Hardcoded Queue` (FCMComposite_1_15)
- **Problem**: Queue name contains environment prefix (DEV), won't work in production
- **Severity**: HIGH
- **Category**: Configuration
- **Detection**: Queue name contains "DEV", "TEST", or "UAT" prefix

### 6. Message Flow Design Issues

#### Issue 7: Route Node without Default Route
- **Node**: `Route No Default` (FCMComposite_1_7)
- **Problem**: Messages that don't match any route will be dropped
- **Severity**: HIGH
- **Category**: Flow Design
- **Detection**: Route node with no default terminal connection

#### Issue 14: Compute with No Null Checks
- **Node**: `Compute No Null Check` (FCMComposite_1_14)
- **Problem**: Null pointer exceptions at runtime
- **Severity**: MEDIUM
- **Category**: Flow Design
- **Detection**: ESQL without null validation (requires code analysis)

### 7. Integration Issues

#### Issue 3: Database without Transaction Coordination
- **Node**: `Database Query` (FCMComposite_1_3)
- **Problem**: Database operations not coordinated with MQ transactions
- **Severity**: HIGH
- **Category**: Integration
- **Detection**: Database node in transactional flow without proper coordination

#### Issue 6: MQ Get without Backout Queue
- **Node**: `MQ Get No Backout` (FCMComposite_1_6)
- **Problem**: Failed messages have nowhere to go
- **Severity**: HIGH
- **Category**: Integration
- **Detection**: `backoutDestination=""` on MQGet node

#### Issue 9: Database without Connection Pooling
- **Node**: `Database No Pool Config` (FCMComposite_1_9)
- **Problem**: Connection overhead, performance degradation
- **Severity**: MEDIUM
- **Category**: Integration
- **Detection**: Database node without connection pool configuration

## Expected Proofcheck Results

When you run the ACE Proofchecker plugin on this flow, it should detect and report:

### Critical Issues (Must Fix):
1. ✗ MQ Input with Transaction Mode = No
2. ✗ No Catch Terminal Connected on Compute node
3. ✗ Hardcoded credentials in Compute node
4. ✗ Sensitive data logging in Compute node
5. ✗ SSL validation disabled on HTTP Request

### High Severity Issues (Should Fix):
6. ✗ HTTP Request without timeout
7. ✗ Database without timeout
8. ✗ MQ Get with infinite wait
9. ✗ HTTP using HTTP instead of HTTPS
10. ✗ Route node without default route
11. ✗ MQ Output with hardcoded queue name
12. ✗ MQ Input without backout threshold
13. ✗ MQ Get without backout queue
14. ✗ Database without transaction coordination
15. ✗ HTTP Request without error handling

### Medium Severity Issues (Good to Fix):
16. ✗ Database using SELECT *
17. ✗ Complex compute logic with nested loops
18. ✗ Compute without null checks
19. ✗ Database without connection pooling

## How to Use This Test Flow

### 1. Import into ACE Toolkit
```
File → Import → General → File System
Select: ProductionIssuesTest.msgflow
```

### 2. Run Proofcheck
```
Right-click on ProductionIssuesTest.msgflow
Select: Run Proofcheck
```

### 3. Review Results
Check the **Problems** view for validation findings. You should see approximately 20 issues reported.

### 4. Verify Detection
Each issue should be reported with:
- **Severity**: Critical, High, or Medium
- **Category**: Transaction Management, Error Handling, Security, etc.
- **Node Name**: Which node has the issue
- **Description**: What the problem is
- **Suggestion**: How to fix it

## Testing Individual Validators

You can test specific validators by creating simplified flows:

### Test Transaction Validator:
- Use only MQ Input/Output nodes
- Vary transaction modes
- Check backout configurations

### Test Security Validator:
- Use HTTP Request nodes
- Test HTTP vs HTTPS
- Test SSL validation settings

### Test Error Handling Validator:
- Use Compute nodes
- Connect/disconnect Catch terminals
- Test with various node types

### Test Performance Validator:
- Use Database nodes with different SQL
- Use HTTP Request with various timeouts
- Test MQ Get with different wait intervals

## Extending the Test Flow

To add more test cases:

1. **Add new nodes** with specific issues
2. **Update this README** with the new issue details
3. **Run Proofcheck** to verify detection
4. **Document expected results**

## Integration with CI/CD

This test flow can be used in automated testing:

```bash
# Run proofcheck from command line (if supported)
mqsiproofcheck -f ProductionIssuesTest.msgflow -o results.xml

# Check for expected number of issues
if [ $(grep -c "severity=\"CRITICAL\"" results.xml) -lt 5 ]; then
  echo "ERROR: Not all critical issues detected"
  exit 1
fi
```

## Notes

- This flow is **intentionally broken** to demonstrate issues
- **Do not deploy** this flow to any environment
- Use it only for **testing the proofcheck plugin**
- Some issues require **ESQL code analysis** which may not be fully implemented yet

## Related Documentation

- [PRODUCTION-ISSUES-CHECKLIST.md](../PRODUCTION-ISSUES-CHECKLIST.md) - Complete list of 300+ issues
- [VALIDATION_TESTING_GUIDE.md](../ace-proofchecker-plugin/VALIDATION_TESTING_GUIDE.md) - Testing guide
- [SEVERITY_MAPPING.md](../ace-proofchecker-plugin/SEVERITY_MAPPING.md) - Severity definitions

## Summary

This test flow provides a comprehensive validation suite for the ACE Proofchecker plugin, covering the most common and critical production issues across multiple categories:

- ✓ Transaction Management (3 issues)
- ✓ Error Handling (2 issues)
- ✓ Performance (5 issues)
- ✓ Security (4 issues)
- ✓ Configuration (1 issue)
- ✓ Flow Design (2 issues)
- ✓ Integration (3 issues)

**Total: 20 production issues demonstrated**