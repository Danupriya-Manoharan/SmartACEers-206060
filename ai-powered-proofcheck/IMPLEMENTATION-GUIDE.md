# AI-Powered Proofcheck - Implementation Guide

## Quick Start: Python Proof of Concept

This guide shows how to quickly build an AI-powered ACE proofchecker using Python.

## Prerequisites

```bash
pip install openai anthropic google-generativeai
```

## Option 1: Simple Python Script

### File: `ai_proofcheck.py`

```python
#!/usr/bin/env python3
"""
AI-Powered ACE Message Flow Proofchecker
Analyzes .msgflow files using AI to detect issues
"""

import json
import sys
from pathlib import Path
from typing import List, Dict
import anthropic  # or openai, google.generativeai

class AIProofchecker:
    def __init__(self, api_key: str, provider: str = "claude"):
        """
        Initialize AI proofchecker
        
        Args:
            api_key: API key for AI provider
            provider: "claude", "openai", or "gemini"
        """
        self.provider = provider
        if provider == "claude":
            self.client = anthropic.Anthropic(api_key=api_key)
        # Add other providers as needed
    
    def analyze_msgflow(self, msgflow_path: str) -> List[Dict]:
        """
        Analyze a message flow file
        
        Args:
            msgflow_path: Path to .msgflow file
            
        Returns:
            List of findings
        """
        # Read msgflow XML
        with open(msgflow_path, 'r') as f:
            xml_content = f.read()
        
        # Build prompt
        prompt = self._build_prompt(xml_content)
        
        # Call AI
        response = self._call_ai(prompt)
        
        # Parse response
        findings = self._parse_response(response)
        
        return findings
    
    def _build_prompt(self, xml_content: str) -> str:
        """Build AI prompt for analysis"""
        return f"""You are an expert IBM ACE (App Connect Enterprise) developer reviewing message flows for production readiness.

Analyze this message flow XML and identify issues in these categories:
1. Message Loss Prevention (transaction modes, error handling)
2. Performance Issues (blocking operations, inefficient patterns)
3. Security Concerns (sensitive data, authentication)
4. Best Practices (naming, structure, maintainability)

Message Flow XML:
```xml
{xml_content}
```

For each issue found, provide:
- severity: "CRITICAL", "HIGH", "MEDIUM", or "LOW"
- category: One of the categories above
- node_name: Name of the node with the issue
- node_type: Type of node (e.g., "MQInput", "Compute")
- property: Specific property with issue (if applicable)
- message: Clear explanation of the issue
- recommendation: How to fix it
- impact: What could happen if not fixed

Respond with a JSON array of findings. If no issues found, return empty array [].

Example response format:
[
  {{
    "severity": "CRITICAL",
    "category": "Message Loss Prevention",
    "node_name": "MQ Input",
    "node_type": "MQInput",
    "property": "transactionMode",
    "message": "Transaction mode is disabled",
    "recommendation": "Enable transaction mode to prevent message loss",
    "impact": "Messages will be lost if processing fails"
  }}
]
"""
    
    def _call_ai(self, prompt: str) -> str:
        """Call AI API"""
        if self.provider == "claude":
            message = self.client.messages.create(
                model="claude-3-5-sonnet-20241022",
                max_tokens=4096,
                messages=[
                    {"role": "user", "content": prompt}
                ]
            )
            return message.content[0].text
        # Add other providers
        return ""
    
    def _parse_response(self, response: str) -> List[Dict]:
        """Parse AI response to findings"""
        try:
            # Extract JSON from response
            start = response.find('[')
            end = response.rfind(']') + 1
            if start >= 0 and end > start:
                json_str = response[start:end]
                findings = json.loads(json_str)
                return findings
            return []
        except Exception as e:
            print(f"Error parsing AI response: {e}")
            return []
    
    def format_findings(self, findings: List[Dict]) -> str:
        """Format findings for display"""
        if not findings:
            return "✅ No issues found! Message flow passed all checks."
        
        output = f"\n🔍 Found {len(findings)} issue(s):\n\n"
        
        for i, finding in enumerate(findings, 1):
            severity_emoji = {
                "CRITICAL": "🔴",
                "HIGH": "🟠",
                "MEDIUM": "🟡",
                "LOW": "🔵"
            }.get(finding.get("severity", "LOW"), "⚪")
            
            output += f"{severity_emoji} Issue #{i}: {finding.get('severity', 'UNKNOWN')}\n"
            output += f"   Category: {finding.get('category', 'Unknown')}\n"
            output += f"   Node: {finding.get('node_name', 'Unknown')} ({finding.get('node_type', 'Unknown')})\n"
            if finding.get('property'):
                output += f"   Property: {finding.get('property')}\n"
            output += f"   Issue: {finding.get('message', 'No description')}\n"
            output += f"   Fix: {finding.get('recommendation', 'No recommendation')}\n"
            output += f"   Impact: {finding.get('impact', 'Unknown impact')}\n\n"
        
        return output


def main():
    """Main entry point"""
    if len(sys.argv) < 2:
        print("Usage: python ai_proofcheck.py <msgflow_file>")
        sys.exit(1)
    
    msgflow_path = sys.argv[1]
    
    if not Path(msgflow_path).exists():
        print(f"Error: File not found: {msgflow_path}")
        sys.exit(1)
    
    # Get API key from environment or config
    import os
    api_key = os.getenv("ANTHROPIC_API_KEY")  # or OPENAI_API_KEY
    
    if not api_key:
        print("Error: Set ANTHROPIC_API_KEY environment variable")
        sys.exit(1)
    
    # Create proofchecker
    checker = AIProofchecker(api_key, provider="claude")
    
    print(f"🔍 Analyzing {msgflow_path}...")
    
    # Analyze
    findings = checker.analyze_msgflow(msgflow_path)
    
    # Display results
    print(checker.format_findings(findings))
    
    # Save results
    output_file = Path(msgflow_path).with_suffix('.proofcheck.json')
    with open(output_file, 'w') as f:
        json.dump(findings, f, indent=2)
    
    print(f"\n💾 Results saved to: {output_file}")


if __name__ == "__main__":
    main()
```

## Usage

### 1. Set up API key

```bash
# For Claude
export ANTHROPIC_API_KEY="your-api-key-here"

# For OpenAI
export OPENAI_API_KEY="your-api-key-here"

# For Gemini
export GOOGLE_API_KEY="your-api-key-here"
```

### 2. Run analysis

```bash
python ai_proofcheck.py path/to/your/flow.msgflow
```

### 3. View results

```bash
# Console output shows findings
# JSON file saved as flow.proofcheck.json
```

## Option 2: VS Code Extension

### File: `extension.js`

```javascript
const vscode = require('vscode');
const Anthropic = require('@anthropic-ai/sdk');
const fs = require('fs');

async function analyzeMessageFlow(document) {
    const apiKey = vscode.workspace.getConfiguration('aceProofcheck').get('apiKey');
    
    if (!apiKey) {
        vscode.window.showErrorMessage('Please set ACE Proofcheck API key in settings');
        return;
    }
    
    const client = new Anthropic({ apiKey });
    
    // Read msgflow content
    const xmlContent = document.getText();
    
    // Build prompt (same as Python version)
    const prompt = buildPrompt(xmlContent);
    
    // Show progress
    await vscode.window.withProgress({
        location: vscode.ProgressLocation.Notification,
        title: "AI Proofcheck analyzing...",
        cancellable: false
    }, async (progress) => {
        // Call AI
        const message = await client.messages.create({
            model: "claude-3-5-sonnet-20241022",
            max_tokens: 4096,
            messages: [{ role: "user", content: prompt }]
        });
        
        // Parse findings
        const findings = parseFindings(message.content[0].text);
        
        // Show results
        showFindings(findings, document);
    });
}

function activate(context) {
    let disposable = vscode.commands.registerCommand(
        'ace-proofcheck.analyze',
        async () => {
            const editor = vscode.window.activeTextEditor;
            if (editor && editor.document.fileName.endsWith('.msgflow')) {
                await analyzeMessageFlow(editor.document);
            } else {
                vscode.window.showErrorMessage('Please open a .msgflow file');
            }
        }
    );
    
    context.subscriptions.push(disposable);
}

module.exports = { activate };
```

## Option 3: REST API Service

### File: `api_server.py`

```python
from fastapi import FastAPI, UploadFile, File
from fastapi.responses import JSONResponse
import anthropic
import os

app = FastAPI(title="ACE Proofcheck API")

@app.post("/analyze")
async def analyze_msgflow(file: UploadFile = File(...)):
    """Analyze uploaded msgflow file"""
    
    # Read file
    content = await file.read()
    xml_content = content.decode('utf-8')
    
    # Analyze with AI
    checker = AIProofchecker(
        api_key=os.getenv("ANTHROPIC_API_KEY"),
        provider="claude"
    )
    
    findings = checker.analyze_msgflow_content(xml_content)
    
    return JSONResponse({
        "filename": file.filename,
        "findings": findings,
        "count": len(findings)
    })

@app.get("/health")
async def health():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

### Usage:

```bash
# Start server
python api_server.py

# Call from Java plugin
curl -X POST http://localhost:8000/analyze \
  -F "file=@test.msgflow"
```

## Integration with Java Plugin

### Add AI Validator to existing plugin:

```java
package com.smartaceers.proofchecker.validators;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.Gson;

public class AIValidator implements IValidator {
    private static final String API_ENDPOINT = "http://localhost:8000/analyze";
    private HttpClient httpClient;
    private Gson gson;
    
    public AIValidator() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    @Override
    public List<Finding> validate(FlowNode node) {
        // This validator works at flow level, not node level
        return new ArrayList<>();
    }
    
    public List<Finding> validateFlow(String msgflowPath) {
        try {
            // Read file
            String xmlContent = Files.readString(Path.of(msgflowPath));
            
            // Call AI API
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    gson.toJson(Map.of("xml", xmlContent))
                ))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            
            // Parse response
            AIResponse aiResponse = gson.fromJson(
                response.body(),
                AIResponse.class
            );
            
            // Convert to Findings
            return convertToFindings(aiResponse.findings);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "AI validation failed", e);
            return new ArrayList<>();
        }
    }
}
```

## Cost Optimization Tips

### 1. Caching

```python
import hashlib
import json
from pathlib import Path

class CachedAIProofchecker(AIProofchecker):
    def __init__(self, *args, cache_dir=".proofcheck_cache", **kwargs):
        super().__init__(*args, **kwargs)
        self.cache_dir = Path(cache_dir)
        self.cache_dir.mkdir(exist_ok=True)
    
    def analyze_msgflow(self, msgflow_path: str) -> List[Dict]:
        # Calculate file hash
        with open(msgflow_path, 'rb') as f:
            file_hash = hashlib.md5(f.read()).hexdigest()
        
        cache_file = self.cache_dir / f"{file_hash}.json"
        
        # Check cache
        if cache_file.exists():
            with open(cache_file, 'r') as f:
                return json.load(f)
        
        # Analyze with AI
        findings = super().analyze_msgflow(msgflow_path)
        
        # Save to cache
        with open(cache_file, 'w') as f:
            json.dump(findings, f)
        
        return findings
```

### 2. Batch Processing

```python
def analyze_multiple_flows(self, msgflow_paths: List[str]) -> Dict[str, List[Dict]]:
    """Analyze multiple flows in one API call"""
    
    # Combine all flows into one prompt
    combined_prompt = self._build_batch_prompt(msgflow_paths)
    
    # Single API call
    response = self._call_ai(combined_prompt)
    
    # Parse results for each flow
    return self._parse_batch_response(response, msgflow_paths)
```

## Testing

### Test file: `test_ai_proofcheck.py`

```python
import pytest
from ai_proofcheck import AIProofchecker

def test_transaction_mode_detection():
    """Test that AI detects transaction mode issues"""
    
    xml = """<?xml version="1.0" encoding="UTF-8"?>
    <ecore:EPackage xmlns:ComIbmMQInput.msgnode="ComIbmMQInput.msgnode">
      <nodes xmi:type="ComIbmMQInput.msgnode:FCMComposite_1" 
             transactionMode="no" 
             queueName="TEST.QUEUE"/>
    </ecore:EPackage>"""
    
    checker = AIProofchecker(api_key="test-key")
    findings = checker.analyze_msgflow_content(xml)
    
    # Should find transaction mode issue
    assert len(findings) > 0
    assert any(f['severity'] == 'CRITICAL' for f in findings)
    assert any('transaction' in f['message'].lower() for f in findings)
```

## Deployment Options

### Docker Container

```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY ai_proofcheck.py .

ENV ANTHROPIC_API_KEY=""

ENTRYPOINT ["python", "ai_proofcheck.py"]
```

### Build and run:

```bash
docker build -t ace-ai-proofcheck .
docker run -e ANTHROPIC_API_KEY=$ANTHROPIC_API_KEY \
  -v $(pwd):/flows \
  ace-ai-proofcheck /flows/test.msgflow
```

## Next Steps

1. **Try the Python script** with your actual .msgflow files
2. **Measure accuracy** compared to Java validators
3. **Calculate costs** based on your usage
4. **Choose integration approach** (standalone, API, or plugin)
5. **Implement caching** to reduce costs
6. **Add to CI/CD pipeline** for automated checks

## Support

For questions or issues:
- Check the feasibility analysis document
- Review AI provider documentation
- Test with sample flows first
- Monitor API costs closely

---

**Author:** SmartACEers Team  
**Date:** 2026-06-12  
**Version:** 1.0