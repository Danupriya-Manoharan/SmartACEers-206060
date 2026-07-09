# SmartACEers Message Flow Proofchecker - Architecture

## System Architecture Overview

This document provides detailed architectural diagrams and design decisions for the ACE Toolkit Message Flow Proofchecker.

## High-Level Architecture

```mermaid
graph TB
    subgraph "ACE Toolkit Environment"
        A[ACE Toolkit IDE]
        B[Message Flow Editor]
        C[.msgflow Files]
    end
    
    subgraph "Proofchecker Plugin"
        D[Eclipse Plugin Handler]
        E[Message Flow Parser]
        F[Validation Engine]
        G[Rule Registry]
        H[Results Collector]
    end
    
    subgraph "Validators"
        I[MQ Transaction Validator]
        J[Catch Terminal Validator]
    end
    
    subgraph "Output"
        K[Eclipse Problems View]
        L[Problem Markers]
    end
    
    A --> D
    B --> C
    C --> E
    D --> E
    E --> F
    F --> G
    G --> I
    G --> J
    I --> H
    J --> H
    H --> L
    L --> K
```

## Component Architecture

```mermaid
graph LR
    subgraph "Core Components"
        A[ValidationEngine]
        B[ValidationContext]
        C[Finding]
        D[Severity]
    end
    
    subgraph "Parser Components"
        E[MessageFlowParser]
        F[FlowNode]
        G[FlowConnection]
        H[NodeProperty]
    end
    
    subgraph "Validator Components"
        I[IValidator Interface]
        J[MQTransactionValidator]
        K[CatchTerminalValidator]
    end
    
    subgraph "Results Components"
        L[ValidationResultsCollector]
        M[ProblemMarkerCreator]
    end
    
    E --> F
    E --> G
    F --> H
    A --> B
    A --> I
    I --> J
    I --> K
    J --> C
    K --> C
    C --> D
    A --> L
    L --> M
```

## Data Flow Diagram

```mermaid
sequenceDiagram
    participant User
    participant Eclipse
    participant Handler
    participant Parser
    participant Engine
    participant Validator
    participant Results
    
    User->>Eclipse: Right-click on .msgflow
    User->>Eclipse: Select "Run Proofcheck"
    Eclipse->>Handler: Invoke command
    Handler->>Parser: Parse message flow
    Parser->>Parser: Read XML structure
    Parser-->>Handler: Return FlowModel
    Handler->>Engine: Validate(FlowModel)
    Engine->>Validator: validate(node)
    Validator->>Validator: Check rules
    Validator-->>Engine: Return findings
    Engine->>Results: Collect findings
    Results->>Eclipse: Create problem markers
    Eclipse->>User: Display in Problems view
```

## Class Diagram

```mermaid
classDiagram
    class IValidator {
        <<interface>>
        +validate(FlowNode) List~Finding~
        +getValidatorId() String
        +getValidatorName() String
    }
    
    class ValidationEngine {
        -validators List~IValidator~
        -context ValidationContext
        +registerValidator(IValidator)
        +validate(MessageFlow) List~Finding~
        +executeValidators() List~Finding~
    }
    
    class Finding {
        -ruleId String
        -severity Severity
        -message String
        -suggestion String
        -node FlowNode
        -lineNumber int
        +getFinding() String
    }
    
    class Severity {
        <<enumeration>>
        CRITICAL
        WARNING
    }
    
    class MessageFlowParser {
        +parse(File) MessageFlow
        +parseNode(Element) FlowNode
        +parseConnections(Element) List~FlowConnection~
    }
    
    class FlowNode {
        -id String
        -type String
        -name String
        -properties Map
        -terminals List~Terminal~
        +getProperty(String) String
        +hasTerminal(String) boolean
    }
    
    class MQTransactionValidator {
        +validate(FlowNode) List~Finding~
        +checkTransactionMode(FlowNode) Finding
    }
    
    class CatchTerminalValidator {
        +validate(FlowNode) List~Finding~
        +checkCatchConnection(FlowNode) Finding
    }
    
    IValidator <|.. MQTransactionValidator
    IValidator <|.. CatchTerminalValidator
    ValidationEngine --> IValidator
    ValidationEngine --> Finding
    Finding --> Severity
    MessageFlowParser --> FlowNode
    MQTransactionValidator --> Finding
    CatchTerminalValidator --> Finding
```

## Validation Flow

```mermaid
flowchart TD
    A[Start: User triggers proofcheck] --> B[Load message flow file]
    B --> C[Parse XML structure]
    C --> D[Build flow model]
    D --> E[Initialize validation engine]
    E --> F[Get registered validators]
    F --> G{More validators?}
    G -->|Yes| H[Get next validator]
    H --> I{Validator applies to node?}
    I -->|Yes| J[Execute validation]
    I -->|No| G
    J --> K[Collect findings]
    K --> G
    G -->|No| L[Aggregate all findings]
    L --> M{Any findings?}
    M -->|Yes| N[Create problem markers]
    M -->|No| O[Show success message]
    N --> P[Display in Problems view]
    O --> Q[End]
    P --> Q
```

## Plugin Integration Architecture

```mermaid
graph TB
    subgraph "Eclipse Platform"
        A[Eclipse Workbench]
        B[Resource Manager]
        C[Problems View]
        D[Markers API]
    end
    
    subgraph "ACE Toolkit"
        E[ACE Perspective]
        F[Message Flow Editor]
        G[ACE APIs]
    end
    
    subgraph "Proofchecker Plugin"
        H[Plugin Activator]
        I[Command Handler]
        J[Menu Contribution]
        K[Validation Service]
    end
    
    A --> E
    E --> F
    F --> G
    H --> I
    I --> J
    J --> K
    K --> G
    K --> B
    K --> D
    D --> C
```

## Validator Extension Architecture

```mermaid
graph LR
    subgraph "Current MVP"
        A[IValidator Interface]
        B[MQ Transaction Validator]
        C[Catch Terminal Validator]
    end
    
    subgraph "Future Extensions"
        D[Security Validator]
        E[Performance Validator]
        F[Logging Validator]
        G[Custom Validator]
    end
    
    A --> B
    A --> C
    A -.-> D
    A -.-> E
    A -.-> F
    A -.-> G
    
    style D stroke-dasharray: 5 5
    style E stroke-dasharray: 5 5
    style F stroke-dasharray: 5 5
    style G stroke-dasharray: 5 5
```

## Design Patterns Used

### 1. Strategy Pattern
- **Purpose**: Encapsulate validation algorithms
- **Implementation**: `IValidator` interface with multiple implementations
- **Benefit**: Easy to add new validators without modifying engine

### 2. Registry Pattern
- **Purpose**: Manage validator instances
- **Implementation**: `RuleRegistry` maintains validator collection
- **Benefit**: Dynamic validator loading and management

### 3. Builder Pattern
- **Purpose**: Construct complex Finding objects
- **Implementation**: `Finding.Builder` class
- **Benefit**: Flexible finding creation with optional parameters

### 4. Observer Pattern
- **Purpose**: Notify UI of validation results
- **Implementation**: Eclipse Markers API
- **Benefit**: Loose coupling between validation and display

## Key Design Decisions

### Decision 1: Eclipse Problems View vs Custom UI
**Choice**: Eclipse Problems View  
**Rationale**: 
- Familiar to developers
- Standard Eclipse integration
- No custom UI maintenance
- Consistent with other tools

### Decision 2: XML Parsing vs ACE API
**Choice**: ACE Toolkit APIs (with XML fallback)  
**Rationale**:
- Official API support
- Better compatibility
- Access to metadata
- Future-proof

### Decision 3: Synchronous vs Asynchronous Validation
**Choice**: Synchronous for MVP  
**Rationale**:
- Simpler implementation
- Adequate for small flows
- Can optimize later
- Easier debugging

### Decision 4: Rule Configuration
**Choice**: Hardcoded for MVP, configurable later  
**Rationale**:
- Faster MVP delivery
- Two rules don't need configuration
- Can add XML/JSON config in Phase 2

## Performance Considerations

### Optimization Strategies
1. **Lazy Loading**: Load validators only when needed
2. **Caching**: Cache parsed flow models
3. **Parallel Validation**: Execute independent validators concurrently (future)
4. **Incremental Validation**: Validate only changed nodes (future)

### Expected Performance
- **Small flows** (<20 nodes): <1 second
- **Medium flows** (20-50 nodes): 1-2 seconds
- **Large flows** (>50 nodes): 2-5 seconds

## Security Considerations

### Data Privacy
- No external network calls
- No data collection or telemetry
- All processing local to IDE

### Code Security
- Input validation on all parsed data
- Safe XML parsing (prevent XXE attacks)
- No dynamic code execution

## Extensibility Points

### 1. New Validators
```java
public class CustomValidator implements IValidator {
    @Override
    public List<Finding> validate(FlowNode node) {
        // Custom validation logic
    }
}
```

### 2. Custom Severity Levels
```java
public enum Severity {
    CRITICAL, WARNING, INFO, SUGGESTION
}
```

### 3. Custom Result Handlers
```java
public interface IResultHandler {
    void handleResults(List<Finding> findings);
}
```

## Testing Architecture

```mermaid
graph TB
    subgraph "Unit Tests"
        A[Validator Tests]
        B[Parser Tests]
        C[Engine Tests]
    end
    
    subgraph "Integration Tests"
        D[Eclipse Integration Tests]
        E[End-to-End Tests]
    end
    
    subgraph "Test Data"
        F[Sample Message Flows]
        G[Mock Objects]
    end
    
    A --> G
    B --> F
    C --> G
    D --> F
    E --> F
```

## Deployment Architecture

```mermaid
graph LR
    A[Source Code] --> B[Maven Build]
    B --> C[JAR Package]
    C --> D[Eclipse Plugin]
    D --> E[Update Site]
    D --> F[Manual Install]
    E --> G[ACE Toolkit]
    F --> G
```

## Future Architecture Enhancements

### Phase 2: Configuration System
```mermaid
graph LR
    A[User Preferences] --> B[Configuration Manager]
    B --> C[Rule Registry]
    C --> D[Validators]
    B --> E[Severity Mapper]
    E --> D
```

### Phase 3: Quick Fix System
```mermaid
graph LR
    A[Finding] --> B[Quick Fix Provider]
    B --> C[Fix Action]
    C --> D[Flow Modifier]
    D --> E[Updated Flow]
```

### Phase 4: CI/CD Integration
```mermaid
graph LR
    A[Git Commit] --> B[CI Pipeline]
    B --> C[Proofchecker CLI]
    C --> D[Validation Report]
    D --> E{Pass?}
    E -->|Yes| F[Deploy]
    E -->|No| G[Block & Notify]
```

## Conclusion

This architecture provides:
- ✅ Clean separation of concerns
- ✅ Easy extensibility for new validators
- ✅ Standard Eclipse integration
- ✅ Testable components
- ✅ Performance optimization opportunities
- ✅ Clear upgrade path for future features

---

**Document Version**: 1.0  
**Last Updated**: 2026-06-11  
**Status**: Architecture Design Complete