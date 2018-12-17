package org.mate.model;

import java.util.ArrayList;
import java.util.List;

public class TestSuite {
    private List<TestCase> testCases;

    public TestSuite() {
        testCases = new ArrayList<>();
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }
}
