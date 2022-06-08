package org.mate.utils.testcase.writer;

import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.model.TestCase;

import java.io.File;

public class EspressoTestCaseWriter extends TestCaseWriter {

    public EspressoTestCaseWriter(TestCase testCase) throws IllegalArgumentException {
        super(testCase);
    }

    @Override
    boolean isSuitableForTestCase() {
        // The Espresso test case writer is suitable for a test case if the Actions in the event
        // sequence are all instances of EspressoAction.

        for (Action action : testCase.getEventSequence()) {
            if (!(action instanceof EspressoAction)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getTestCaseString() {
        EspressoTestCaseStringConverter converter =
                new EspressoTestCaseStringConverter(
                        Registry.getPackageName(),
                        testCase.getActivitySequence().get(0),
                        getTestCaseName(),
                        "testMethod");

        for (Action action : testCase.getEventSequence()) {
            EspressoAction espressoAction = (EspressoAction) action;
            converter.addAction(espressoAction);
        }

        return converter.getCode();
    }

    @Override
    public String getTestCaseName() {
        return String.format("EspressoTestCase_%d", this.writeCounter);
    }

    @Override
    public String getTestCaseFileName() {
        return String.format("%s.java", this.getTestCaseName());
    }

    @Override
    public String getDefaultWriteFolder() {
        File filesDir = Registry.getContext().getFilesDir();
        File espressoTestCasesFolder = new File(filesDir, "espresso-test-cases");
        return espressoTestCasesFolder.getAbsolutePath();
    }
}
