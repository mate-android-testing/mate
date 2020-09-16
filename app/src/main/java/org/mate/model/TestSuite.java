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

    @Override
    public String toString() {
        return id;
    }
}
