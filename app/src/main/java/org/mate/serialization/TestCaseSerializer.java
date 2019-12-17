package org.mate.serialization;

import com.thoughtworks.xstream.XStream;

import org.mate.MATE;
import org.mate.model.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Provides the functionality to serialize and de-serialize a {@link org.mate.model.TestCase}.
 * This basically enables the recording and replaying of test cases.
 */
public final class TestCaseSerializer {

    private static final String TEST_CASES_DIR = "/data/data/org.mate/test-cases";

    public static void serializeTestCase(TestCase testCase) {

        // create the test-cases folder if not yet present
        File dir = new File(TEST_CASES_DIR);
        if (!dir.exists()) {
            MATE.log("Creating test-cases folder succeeded: " + dir.mkdir());
        }

        // log whether execution of test case resulted in a crash
        if (testCase.getCrashDetected()) {
            MATE.log_acc("TestCase " + testCase.getId() + " caused a crash!");
        }

        // convert test case to xml
        XStream xstream = new XStream();
        String xml = xstream.toXML(testCase);

        // the output file
        File testCaseFile = new File(dir, "TestCase" + testCase.getId() + ".xml");

        try (Writer fileWriter = new FileWriter(testCaseFile)) {

            fileWriter.write(xml);
            fileWriter.flush();

        } catch (IOException e) {
            MATE.log_acc("Serializing TestCase " + testCase.getId() + " failed!");
            e.printStackTrace();
        }
    }

    public static void deserializeTestCase() {}

}
