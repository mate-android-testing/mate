package org.mate.utils.testcase.writer;

import org.mate.model.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public abstract class TestCaseWriter {
    /**
     * Tracks the number of written test cases, to avoid overlapping file names.
     */
    protected static int writeCounter = 0;

    /**
     * The test case to write out.
     */
    protected TestCase testCase;

    public TestCaseWriter(TestCase testCase) throws IllegalArgumentException {
        this.testCase = testCase;

        if (!isSuitableForTestCase()) {
            throw new IllegalArgumentException("Test writer not suitable for test case");
        }
    }

    /**
     * Returns a boolean indicating whether this TestWriter is suitable or not for the provided
     * test case.
     */
    abstract boolean isSuitableForTestCase();

    /**
     * Returns the test case as a string.
     */
    public abstract String getTestCaseString();

    /**
     * Returns the name chosen for test case.
     */
    public abstract String getTestCaseName();

    /**
     * Returns the File name chosen for test case.
     */
    public abstract String getTestCaseFileName();

    /**
     * Returns the default write folder for this writer.
     */
    public abstract String getDefaultWriteFolder();

    /**
     * Writes the test case to the default folder.
     *
     * @return true if and only if it succeeded
     * @throws IOException if an error occurred while writing to disk
     */
    public boolean writeToDefaultFolder() throws IOException {
        return writeToFolder(getDefaultWriteFolder());
    }

    /**
     * Writes the test case to a specific folder.
     *
     * @param folder path in the device
     * @return true if and only if it succeeded
     * @throws IOException if an error occurred while writing to disk
     */
    public boolean writeToFolder(String folder) throws IOException {
        // make sure that output folder exists
        File outputFolder = new File(folder);
        boolean success = ensureFolderExists(outputFolder);
        if (!success) {
            throw new IOException("Unable to create output folder: " + folder);
        }

        // prepare output file path and file writer
        String testCaseFileName = getTestCaseFileName();
        File outputFile = new File(outputFolder, testCaseFileName);
        Writer fileWriter = new FileWriter(outputFile);

        // get test case content as string and write out to file
        String testCaseString = getTestCaseString();
        fileWriter.write(testCaseString);
        fileWriter.flush();

        // close file writer
        fileWriter.close();

        // Ask MATE Server to download the test case just written
        triggerMATEServerDownload();

        // increase write counter for future writes
        writeCounter++;

        return success;
    }

    /**
     * Asks MATE Server to retrieve the written test case from the emulator.
     */
    protected abstract void triggerMATEServerDownload();

    /**
     * Creates a folder if it doesn't exist.
     */
    private boolean ensureFolderExists(File folder) {
        if (!folder.exists()) {
            return folder.mkdirs();
        }

        return true;
    }
}
