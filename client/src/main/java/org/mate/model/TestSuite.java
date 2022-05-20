package org.mate.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestSuite {

    private final String id;
    private List<TestCase> testCases;

    public TestSuite() {
        this(UUID.randomUUID().toString());
    }

    public TestSuite(String id) {
        this.id = id;
        testCases = new ArrayList<>();
    }

    /**
     * Returns the test suite id.
     *
     * @return Returns the test suite id.
     */
    public String getId() {
        return id;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * Checks whether any of the test cases of the test suite produced a crash.
     *
     * @return Returns {@code true} if the test suite produced a crash, otherwise {@code false}
     *          is returned.
     */
    public boolean getCrashDetected() {
        for (TestCase testCase : testCases) {
            if (testCase.hasCrashDetected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return id;
    }
}
