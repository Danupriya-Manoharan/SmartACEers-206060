package com.smartaceers.proofchecker.results;

import com.smartaceers.proofchecker.core.Finding;
import com.smartaceers.proofchecker.core.Severity;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates Eclipse problem markers from validation findings.
 * Integrates validation results with Eclipse's Problems view.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class ProblemMarkerCreator {
    
    private static final Logger LOGGER = Logger.getLogger(ProblemMarkerCreator.class.getName());
    
    // Marker type for ACE proofchecker findings
    public static final String MARKER_TYPE = "com.smartaceers.proofchecker.problem";
    
    // Marker attributes
    private static final String ATTR_RULE_ID = "ruleId";
    private static final String ATTR_CATEGORY = "category";
    private static final String ATTR_NODE_NAME = "nodeName";
    private static final String ATTR_NODE_TYPE = "nodeType";
    private static final String ATTR_SUGGESTION = "suggestion";
    
    /**
     * Creates problem markers for all findings in a file.
     * 
     * @param file The file to create markers for
     * @param findings List of findings
     * @throws CoreException if marker creation fails
     */
    public void createMarkers(IFile file, List<Finding> findings) throws CoreException {
        if (file == null || findings == null) {
            return;
        }
        
        // Clear existing markers using instance method
        clearMarkersInstance(file);
        
        // Create new markers
        for (Finding finding : findings) {
            createMarker(file, finding);
        }
        
        LOGGER.info(String.format("Created %d markers for %s", findings.size(), file.getName()));
    }
    
    /**
     * Creates a single problem marker for a finding.
     * 
     * @param file The file to create the marker for
     * @param finding The finding to create a marker for
     * @throws CoreException if marker creation fails
     */
    public void createMarker(IFile file, Finding finding) throws CoreException {
        if (file == null || finding == null) {
            return;
        }
        
        try {
            IMarker marker = file.createMarker(MARKER_TYPE);
            
            // Set standard marker attributes
            marker.setAttribute(IMarker.MESSAGE, finding.getFormattedMessage());
            marker.setAttribute(IMarker.SEVERITY, finding.getSeverity().toMarkerSeverity());
            marker.setAttribute(IMarker.PRIORITY, getPriority(finding.getSeverity()));
            
            // Set line number if available
            int lineNumber = finding.getLineNumber();
            if (lineNumber > 0) {
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            }
            
            // Set custom attributes
            marker.setAttribute(ATTR_RULE_ID, finding.getRuleId());
            marker.setAttribute(ATTR_CATEGORY, finding.getCategory());
            marker.setAttribute(ATTR_NODE_NAME, finding.getNodeName());
            marker.setAttribute(ATTR_NODE_TYPE, finding.getNodeType());
            
            if (finding.getSuggestion() != null) {
                marker.setAttribute(ATTR_SUGGESTION, finding.getSuggestion());
            }
            
            // Set location description
            String location = String.format("Node: %s (Type: %s)", 
                    finding.getNodeName(), finding.getNodeType());
            marker.setAttribute(IMarker.LOCATION, location);
            
            LOGGER.fine("Created marker: " + finding.getFormattedMessage());
            
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Failed to create marker for finding: " + finding, e);
            throw e;
        }
    }
    
    /**
     * Clears all proofchecker markers from a resource (static method).
     * This is the recommended way to clear markers before adding new ones.
     *
     * @param resource The resource to clear markers from
     */
    public static void clearMarkers(IResource resource) {
        try {
            resource.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to clear markers from: " + resource.getName(), e);
        }
    }
    
    /**
     * Clears all proofchecker markers from a file (instance method).
     *
     * @param file The file to clear markers from
     * @throws CoreException if marker deletion fails
     */
    public void clearMarkersInstance(IFile file) throws CoreException {
        if (file == null || !file.exists()) {
            return;
        }
        
        try {
            file.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
            LOGGER.fine("Cleared markers from: " + file.getName());
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to clear markers from: " + file.getName(), e);
            throw e;
        }
    }
    
    /**
     * Clears all proofchecker markers from a resource and its children.
     * 
     * @param resource The resource to clear markers from
     * @throws CoreException if marker deletion fails
     */
    public void clearMarkersRecursive(IResource resource) throws CoreException {
        if (resource == null || !resource.exists()) {
            return;
        }
        
        try {
            resource.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
            LOGGER.fine("Cleared markers recursively from: " + resource.getName());
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to clear markers from: " + resource.getName(), e);
            throw e;
        }
    }
    
    /**
     * Gets all proofchecker markers from a file.
     * 
     * @param file The file to get markers from
     * @return Array of markers
     * @throws CoreException if marker retrieval fails
     */
    public IMarker[] getMarkers(IFile file) throws CoreException {
        if (file == null || !file.exists()) {
            return new IMarker[0];
        }
        
        return file.findMarkers(MARKER_TYPE, true, IResource.DEPTH_ZERO);
    }
    
    /**
     * Gets the count of markers in a file.
     * 
     * @param file The file to count markers for
     * @return Number of markers
     * @throws CoreException if marker retrieval fails
     */
    public int getMarkerCount(IFile file) throws CoreException {
        return getMarkers(file).length;
    }
    
    /**
     * Gets the count of critical markers in a file.
     * 
     * @param file The file to count markers for
     * @return Number of critical markers
     * @throws CoreException if marker retrieval fails
     */
    public int getCriticalMarkerCount(IFile file) throws CoreException {
        IMarker[] markers = getMarkers(file);
        int count = 0;
        
        for (IMarker marker : markers) {
            int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            if (severity == IMarker.SEVERITY_ERROR) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Converts severity to marker priority.
     * 
     * @param severity Severity level
     * @return Marker priority constant
     */
    private int getPriority(Severity severity) {
        switch (severity) {
            case CRITICAL:
                return IMarker.PRIORITY_HIGH;
            case WARNING:
                return IMarker.PRIORITY_NORMAL;
            case INFO:
                return IMarker.PRIORITY_LOW;
            default:
                return IMarker.PRIORITY_NORMAL;
        }
    }
    
    /**
     * Gets the rule ID from a marker.
     * 
     * @param marker The marker
     * @return Rule ID, or null if not found
     */
    public String getRuleId(IMarker marker) {
        try {
            return (String) marker.getAttribute(ATTR_RULE_ID);
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to get rule ID from marker", e);
            return null;
        }
    }
    
    /**
     * Gets the category from a marker.
     * 
     * @param marker The marker
     * @return Category, or null if not found
     */
    public String getCategory(IMarker marker) {
        try {
            return (String) marker.getAttribute(ATTR_CATEGORY);
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to get category from marker", e);
            return null;
        }
    }
    
    /**
     * Gets the node name from a marker.
     * 
     * @param marker The marker
     * @return Node name, or null if not found
     */
    public String getNodeName(IMarker marker) {
        try {
            return (String) marker.getAttribute(ATTR_NODE_NAME);
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to get node name from marker", e);
            return null;
        }
    }
    
    /**
     * Gets the suggestion from a marker.
     * 
     * @param marker The marker
     * @return Suggestion, or null if not found
     */
    public String getSuggestion(IMarker marker) {
        try {
            return (String) marker.getAttribute(ATTR_SUGGESTION);
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Failed to get suggestion from marker", e);
            return null;
        }
    }
    
    /**
     * Checks if a marker is a proofchecker marker.
     * 
     * @param marker The marker to check
     * @return true if it's a proofchecker marker
     */
    public boolean isProofcheckerMarker(IMarker marker) {
        try {
            return MARKER_TYPE.equals(marker.getType());
        } catch (CoreException e) {
            return false;
        }
    }
}

// Made with Bob
