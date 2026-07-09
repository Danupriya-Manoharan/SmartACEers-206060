package com.smartaceers.proofchecker.results;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import com.smartaceers.proofchecker.core.ValidationContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects and organizes validation results.
 * Provides methods to filter, sort, and summarize findings.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class ValidationResultsCollector {
    
    private final List<Finding> allFindings;
    private final Map<String, List<Finding>> findingsByCategory;
    private final Map<Severity, List<Finding>> findingsBySeverity;
    private final String flowFilePath;
    
    /**
     * Creates a new results collector from a validation context.
     * 
     * @param context Validation context containing findings
     */
    public ValidationResultsCollector(ValidationContext context) {
        this.flowFilePath = context.getFlowFilePath();
        this.allFindings = new ArrayList<>(context.getFindings());
        this.findingsByCategory = new HashMap<>();
        this.findingsBySeverity = new HashMap<>();
        
        organizeFindings();
    }
    
    /**
     * Creates a new results collector with a list of findings.
     * 
     * @param flowFilePath Path to the flow file
     * @param findings List of findings
     */
    public ValidationResultsCollector(String flowFilePath, List<Finding> findings) {
        this.flowFilePath = flowFilePath;
        this.allFindings = new ArrayList<>(findings);
        this.findingsByCategory = new HashMap<>();
        this.findingsBySeverity = new HashMap<>();
        
        organizeFindings();
    }
    
    /**
     * Organizes findings by category and severity.
     */
    private void organizeFindings() {
        for (Finding finding : allFindings) {
            // Organize by category
            String category = finding.getCategory();
            findingsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(finding);
            
            // Organize by severity
            Severity severity = finding.getSeverity();
            findingsBySeverity.computeIfAbsent(severity, k -> new ArrayList<>()).add(finding);
        }
    }
    
    /**
     * Gets all findings.
     * 
     * @return List of all findings
     */
    public List<Finding> getAllFindings() {
        return new ArrayList<>(allFindings);
    }
    
    /**
     * Gets findings by category.
     * 
     * @param category Category name
     * @return List of findings in the category
     */
    public List<Finding> getFindingsByCategory(String category) {
        return new ArrayList<>(findingsByCategory.getOrDefault(category, Collections.emptyList()));
    }
    
    /**
     * Gets findings by severity.
     * 
     * @param severity Severity level
     * @return List of findings with the specified severity
     */
    public List<Finding> getFindingsBySeverity(Severity severity) {
        return new ArrayList<>(findingsBySeverity.getOrDefault(severity, Collections.emptyList()));
    }
    
    /**
     * Gets all categories that have findings.
     * 
     * @return List of category names
     */
    public List<String> getCategories() {
        return new ArrayList<>(findingsByCategory.keySet());
    }
    
    /**
     * Gets the total number of findings.
     * 
     * @return Total findings count
     */
    public int getTotalCount() {
        return allFindings.size();
    }
    
    /**
     * Gets the count of critical findings.
     * Mirrors the "error" marker bucket: CRITICAL and HIGH severities.
     *
     * @return Critical findings count
     */
    public int getCriticalCount() {
        return countBySeverity(Severity.CRITICAL, Severity.HIGH);
    }

    /**
     * Gets the count of warning findings.
     * Mirrors the "warning" marker bucket: MEDIUM (and the deprecated WARNING).
     *
     * @return Warning findings count
     */
    public int getWarningCount() {
        return countBySeverity(Severity.MEDIUM, Severity.WARNING);
    }

    /**
     * Gets the count of info findings.
     * Mirrors the "info" marker bucket: LOW and INFO severities.
     *
     * @return Info findings count
     */
    public int getInfoCount() {
        return countBySeverity(Severity.LOW, Severity.INFO);
    }

    /**
     * Counts findings across one or more severities.
     *
     * @param severities Severities to include in the count
     * @return Total number of findings with any of the given severities
     */
    private int countBySeverity(Severity... severities) {
        int count = 0;
        for (Severity severity : severities) {
            count += findingsBySeverity.getOrDefault(severity, Collections.emptyList()).size();
        }
        return count;
    }
    
    /**
     * Checks if there are any critical findings.
     * 
     * @return true if there are critical findings
     */
    public boolean hasCriticalFindings() {
        return getCriticalCount() > 0;
    }
    
    /**
     * Checks if there are any findings at all.
     * 
     * @return true if there are findings
     */
    public boolean hasFindings() {
        return !allFindings.isEmpty();
    }
    
    /**
     * Gets findings sorted by severity (critical first).
     * 
     * @return Sorted list of findings
     */
    public List<Finding> getFindingsSortedBySeverity() {
        List<Finding> sorted = new ArrayList<>(allFindings);
        sorted.sort(Comparator.comparing(Finding::getSeverity, 
            Comparator.comparingInt(s -> -s.getPriority())));
        return sorted;
    }
    
    /**
     * Gets findings sorted by line number.
     * 
     * @return Sorted list of findings
     */
    public List<Finding> getFindingsSortedByLineNumber() {
        List<Finding> sorted = new ArrayList<>(allFindings);
        sorted.sort(Comparator.comparingInt(Finding::getLineNumber));
        return sorted;
    }
    
    /**
     * Gets findings sorted by node name.
     * 
     * @return Sorted list of findings
     */
    public List<Finding> getFindingsSortedByNodeName() {
        List<Finding> sorted = new ArrayList<>(allFindings);
        sorted.sort(Comparator.comparing(Finding::getNodeName));
        return sorted;
    }
    
    /**
     * Gets a summary of the validation results.
     * 
     * @return Summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Results for: ").append(flowFilePath).append("\n");
        sb.append("Total Findings: ").append(getTotalCount()).append("\n");
        sb.append("  Critical: ").append(getCriticalCount()).append("\n");
        sb.append("  Warnings: ").append(getWarningCount()).append("\n");
        sb.append("  Info: ").append(getInfoCount()).append("\n");
        
        if (!findingsByCategory.isEmpty()) {
            sb.append("\nFindings by Category:\n");
            for (String category : getCategories()) {
                int count = getFindingsByCategory(category).size();
                sb.append("  ").append(category).append(": ").append(count).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Gets a detailed report of all findings.
     * 
     * @return Detailed report string
     */
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        
        if (hasFindings()) {
            sb.append("\nDetailed Findings:\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            List<Finding> sorted = getFindingsSortedBySeverity();
            for (int i = 0; i < sorted.size(); i++) {
                Finding finding = sorted.get(i);
                sb.append(String.format("%d. %s\n", i + 1, finding.getDetailedMessage()));
                sb.append("-".repeat(80)).append("\n\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Filters findings by a rule ID.
     * 
     * @param ruleId Rule ID to filter by
     * @return List of findings matching the rule ID
     */
    public List<Finding> getFindingsByRuleId(String ruleId) {
        List<Finding> result = new ArrayList<>();
        for (Finding finding : allFindings) {
            if (finding.getRuleId().equals(ruleId)) {
                result.add(finding);
            }
        }
        return result;
    }
    
    /**
     * Filters findings by node name.
     * 
     * @param nodeName Node name to filter by
     * @return List of findings for the specified node
     */
    public List<Finding> getFindingsByNodeName(String nodeName) {
        List<Finding> result = new ArrayList<>();
        for (Finding finding : allFindings) {
            if (finding.getNodeName().equals(nodeName)) {
                result.add(finding);
            }
        }
        return result;
    }
    
    /**
     * Gets the flow file path.
     * 
     * @return Flow file path
     */
    public String getFlowFilePath() {
        return flowFilePath;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationResults[file=%s, findings=%d, critical=%d]",
                flowFilePath, getTotalCount(), getCriticalCount());
    }
}

// Made with Bob
