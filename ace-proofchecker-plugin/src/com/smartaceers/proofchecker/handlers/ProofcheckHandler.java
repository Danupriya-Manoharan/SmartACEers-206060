package com.smartaceers.proofchecker.handlers;

import com.smartaceers.proofchecker.core.ValidationContext;
import com.smartaceers.proofchecker.core.ValidationEngine;
import com.smartaceers.proofchecker.results.ProblemMarkerCreator;
import com.smartaceers.proofchecker.results.ValidationResultsCollector;
import com.smartaceers.proofchecker.utils.ValidationLogger;
import com.smartaceers.proofchecker.validators.BestPracticesValidator;
import com.smartaceers.proofchecker.validators.CatchTerminalValidator;
import com.smartaceers.proofchecker.validators.DatabaseConnectionValidator;
import com.smartaceers.proofchecker.validators.HTTPRestValidator;
import com.smartaceers.proofchecker.validators.MQTransactionValidator;
import com.smartaceers.proofchecker.validators.PerformanceValidator;
import com.smartaceers.proofchecker.validators.SecurityValidator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for the "Run Proofcheck" command.
 * Executes validation on selected message flow files and displays results.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class ProofcheckHandler extends AbstractHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ProofcheckHandler.class.getName());
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        
        LOGGER.info("ProofcheckHandler invoked. Selection type: " +
                   (selection != null ? selection.getClass().getName() : "null"));
        
        if (selection == null || selection.isEmpty()) {
            showError(shell, "Please select a message flow file (.msgflow)");
            return null;
        }
        
        IFile file = null;
        
        // Try to get IFile from selection
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();
            
            LOGGER.info("First element type: " +
                       (firstElement != null ? firstElement.getClass().getName() : "null"));
            
            // Direct IFile
            if (firstElement instanceof IFile) {
                file = (IFile) firstElement;
            }
            // Try to adapt to IFile (for ACE Toolkit custom objects)
            else if (firstElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) firstElement;
                file = adaptable.getAdapter(IFile.class);
                
                // Try Platform adapter if direct adaptation fails
                if (file == null) {
                    file = Platform.getAdapterManager().getAdapter(firstElement, IFile.class);
                }
            }
        }
        
        // Validate we got a file
        if (file == null) {
            LOGGER.warning("Could not extract IFile from selection");
            showError(shell, "Please select a message flow file (.msgflow)");
            return null;
        }
        
        LOGGER.info("File selected: " + file.getName() + " (extension: " +
                   file.getFileExtension() + ")");
        
        // Validate file extension
        if (!"msgflow".equalsIgnoreCase(file.getFileExtension())) {
            showError(shell, "Selected file is not a message flow file (.msgflow)\n" +
                           "Selected: " + file.getName());
            return null;
        }
        
        // Run validation in a background job
        runValidation(file, shell);
        
        return null;
    }
    
    /**
     * Runs validation in a background job.
     * 
     * @param file File to validate
     * @param shell Shell for displaying messages
     */
    private void runValidation(final IFile file, final Shell shell) {
        Job validationJob = new Job("ACE Proofcheck: " + file.getName()) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    monitor.beginTask("Validating message flow...", 100);

                    // Sync the workspace resource with the filesystem first.
                    // After the user edits and saves the flow in the ACE editor, the
                    // IFile can be out of sync with the file on disk. If it is, marker
                    // delete/create operations fail with a "resource out of sync"
                    // CoreException - which leaves stale markers from the previous run
                    // and surfaces as an error. Refreshing also ensures we parse the
                    // latest saved content.
                    monitor.subTask("Refreshing message flow...");
                    try {
                        file.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                    } catch (CoreException e) {
                        LOGGER.log(Level.WARNING,
                                "Could not refresh file before validation: " + file.getName(), e);
                    }

                    // Get file path
                    String filePath = file.getLocation().toOSString();
                    String fileName = file.getName();
                    LOGGER.info("Starting validation for: " + filePath);
                    
                    // Initialize log file
                    String logFile = ValidationLogger.initializeLogFile(fileName);
                    ValidationLogger.log("Log file created at: " + logFile);
                    ValidationLogger.log("");
                    
                    monitor.worked(10);
                    
                    // Create validation engine
                    ValidationEngine engine = new ValidationEngine();
                    
                    ValidationLogger.log("[ProofcheckHandler] Registering validators...");
                    
                    // Register all validators
                    engine.registerValidator(new MQTransactionValidator());
                    ValidationLogger.log("  ✓ MQTransactionValidator registered");
                    
                    engine.registerValidator(new CatchTerminalValidator());
                    ValidationLogger.log("  ✓ CatchTerminalValidator registered");
                    
                    engine.registerValidator(new SecurityValidator());
                    ValidationLogger.log("  ✓ SecurityValidator registered");
                    
                    engine.registerValidator(new DatabaseConnectionValidator());
                    ValidationLogger.log("  ✓ DatabaseConnectionValidator registered");
                    
                    engine.registerValidator(new HTTPRestValidator());
                    ValidationLogger.log("  ✓ HTTPRestValidator registered");
                    
                    engine.registerValidator(new PerformanceValidator());
                    ValidationLogger.log("  ✓ PerformanceValidator registered");

                    engine.registerValidator(new BestPracticesValidator());
                    ValidationLogger.log("  ✓ BestPracticesValidator registered");

                    ValidationLogger.log("");
                    ValidationLogger.log("[ProofcheckHandler] All 7 validators registered successfully");
                    ValidationLogger.log("");
                    
                    monitor.worked(10);
                    
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    
                    // Clear old markers FIRST
                    monitor.subTask("Clearing old markers...");
                    ProblemMarkerCreator.clearMarkers(file);
                    
                    monitor.worked(5);
                    
                    // Run validation
                    monitor.subTask("Parsing message flow...");
                    ValidationLogger.log("[ProofcheckHandler] Starting validation engine...");
                    ValidationLogger.log("");
                    ValidationContext context = engine.validate(filePath);
                    ValidationLogger.log("");
                    ValidationLogger.log("[ProofcheckHandler] Validation engine completed");
                    
                    monitor.worked(45);
                    
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    
                    // Create problem markers
                    monitor.subTask("Creating problem markers...");
                    ProblemMarkerCreator markerCreator = new ProblemMarkerCreator();
                    markerCreator.createMarkers(file, context.getFindings());
                    
                    monitor.worked(20);
                    
                    // Collect results
                    ValidationResultsCollector results = new ValidationResultsCollector(context);
                    
                    monitor.worked(10);
                    
                    // Show completion message
                    final String summary = results.getSummary();
                    LOGGER.info("Validation completed: " + summary);
                    
                    ValidationLogger.log("");
                    ValidationLogger.logHeader("ACE PROOFCHECK VALIDATION COMPLETED");
                    ValidationLogger.log("Summary: " + summary);
                    ValidationLogger.log("Log file: " + ValidationLogger.getCurrentLogFile());
                    ValidationLogger.log("================================================================================");
                    
                    // Cleanup old logs
                    ValidationLogger.cleanupOldLogs();
                    
                    // Close log file
                    ValidationLogger.closeLogFile();
                    
                    shell.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            showCompletionMessage(shell, results);
                        }
                    });
                    
                    monitor.done();
                    return Status.OK_STATUS;
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Validation failed", e);
                    ValidationLogger.logError("Validation failed", e);
                    ValidationLogger.closeLogFile();
                    
                    shell.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            showError(shell, "Validation failed: " + e.getMessage());
                        }
                    });
                    
                    return new Status(IStatus.ERROR, "com.smartaceers.proofchecker", 
                            "Validation failed", e);
                }
            }
        };
        
        validationJob.setUser(true);
        validationJob.schedule();
    }
    
    /**
     * Shows a completion message with validation results.
     * 
     * @param shell Shell for displaying the message
     * @param results Validation results
     */
    private void showCompletionMessage(Shell shell, ValidationResultsCollector results) {
        String title = "ACE Proofcheck Complete";
        String message;
        int messageType;
        
        if (!results.hasFindings()) {
            message = "No issues found! The message flow passed all validation checks.";
            messageType = MessageDialog.INFORMATION;
        } else if (results.hasCriticalFindings()) {
            message = String.format(
                "Validation completed with %d critical issue(s) and %d warning(s).\n\n" +
                "Please check the Problems view for details.",
                results.getCriticalCount(),
                results.getWarningCount()
            );
            messageType = MessageDialog.ERROR;
        } else {
            message = String.format(
                "Validation completed with %d warning(s).\n\n" +
                "Please check the Problems view for details.",
                results.getWarningCount()
            );
            messageType = MessageDialog.WARNING;
        }
        
        MessageDialog.open(messageType, shell, title, message, 0);
    }
    
    /**
     * Shows an error message.
     * 
     * @param shell Shell for displaying the message
     * @param message Error message
     */
    private void showError(Shell shell, String message) {
        MessageDialog.openError(shell, "ACE Proofcheck Error", message);
        LOGGER.warning("Error: " + message);
    }
    
    /**
     * Shows an information message.
     * 
     * @param shell Shell for displaying the message
     * @param message Information message
     */
    private void showInfo(Shell shell, String message) {
        MessageDialog.openInformation(shell, "ACE Proofcheck", message);
    }
}

// Made with Bob
