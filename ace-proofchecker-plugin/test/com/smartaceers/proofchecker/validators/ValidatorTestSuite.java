package com.smartaceers.proofchecker.validators;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for all validators.
 * Run this to execute all validator tests at once.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
@RunWith(Suite.class)
@SuiteClasses({
    DatabaseConnectionValidatorTest.class,
    HTTPRestValidatorTest.class,
    PerformanceValidatorTest.class,
    SecurityValidatorTest.class,
    NamingConventionValidatorTest.class,
    BestPracticesValidatorTest.class
})
public class ValidatorTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}

// Made with Bob