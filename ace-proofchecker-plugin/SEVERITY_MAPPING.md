# Severity Level Mapping

## Updated Severity Enum

The `Severity.java` enum has been updated to support all severity levels used by the validators.

### Available Severity Levels

| Severity | Priority | Eclipse Marker | Description |
|----------|----------|----------------|-------------|
| CRITICAL | 4 | ERROR (2) | Must fix - causes production issues, data loss, security breaches |
| HIGH | 3 | ERROR (2) | Should fix soon - significant issues, error handling gaps |
| MEDIUM | 2 | WARNING (1) | Should fix - performance issues, maintainability concerns |
| LOW | 1 | INFO (0) | Consider fixing - minor issues, suggestions |
| WARNING | 2 | WARNING (1) | Deprecated - use MEDIUM instead |
| INFO | 0 | INFO (0) | Informational only |

---

## Validator Severity Usage

### DatabaseConnectionValidator
- **CRITICAL**: Missing data source
- **HIGH**: Connection pooling disabled, unconnected catch terminal, transaction mode disabled
- **MEDIUM**: Timeout too low/high, invalid timeout
- **LOW**: Missing timeout

### HTTPRestValidator
- **CRITICAL**: Basic Auth over HTTP, credential in URL
- **HIGH**: Unencrypted HTTP, weak SSL protocol
- **MEDIUM**: Timeout issues, unconnected catch terminal, excessive retries
- **LOW**: Missing authentication, missing retry logic, missing timeout

### PerformanceValidator
- **HIGH**: Blocking operations in loops, large messages (>10MB)
- **MEDIUM**: Nested loops, string concatenation in loops, message copying, moderate messages (>5MB)
- **LOW**: SELECT * usage, XPath navigation, missing documentation

### SecurityValidator
- **CRITICAL**: Hardcoded passwords, credentials in URL, Basic Auth over HTTP
- **HIGH**: Unencrypted HTTP, weak SSL, insecure FTP, sensitive data logging
- **MEDIUM**: Unencrypted files, sensitive data handling
- **LOW**: Missing authentication

### NamingConventionValidator
- **MEDIUM**: Empty name, non-descriptive names, problematic prefixes, spaces, special characters
- **LOW**: Type-only names, numbered suffixes, style mismatches, length issues

### BestPracticesValidator
- **CRITICAL**: Missing subflow reference
- **HIGH**: No error handling, unconnected catch terminals
- **MEDIUM**: Complex nodes (>200 lines), deep nesting, error swallowing, file not closed
- **LOW**: Missing logging, excessive logging, missing documentation, manual transactions

---

## Eclipse Marker Mapping

The severity levels map to Eclipse markers as follows:

```java
CRITICAL, HIGH    → IMarker.SEVERITY_ERROR (red X)
MEDIUM, WARNING   → IMarker.SEVERITY_WARNING (yellow warning)
LOW, INFO         → IMarker.SEVERITY_INFO (blue info)
```

---

## Migration Notes

If you have existing code using the old severity levels:
- `WARNING` is deprecated, use `MEDIUM` instead
- All validators now use: CRITICAL, HIGH, MEDIUM, LOW
- Tests updated to use new severity levels

---

**Last Updated**: 2026-06-18  
**Version**: 1.1.0