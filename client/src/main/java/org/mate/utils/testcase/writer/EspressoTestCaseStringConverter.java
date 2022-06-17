package org.mate.utils.testcase.writer;

import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.utils.AbstractCodeProducer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts a TestCase composed of Espresso Actions into a String representation of a valid Java
 * file. You can get this String by calling the {@link #getCode} method.
 *
 * Its important to note that by calling the method {@link #setConvertingForAUTsCodeBase} you can
 * alter a bit the String output. If the value set is true, the converter will try to produce a
 * Java String that can be included into the AUT's codebase. Otherwise, the converter will
 * produce a Java String that is meant to be used with our own Espresso-test-apk-builder program.
 * For example, when convertingForAUTsCodeBase is true, the package name of the test case is set
 * as the AUT's package name.
 * The default for this value is false.
 */
public class EspressoTestCaseStringConverter extends AbstractCodeProducer {

    /**
     * The AUT's package name.
     */
    private final String packageName;

    /**
     * The AUT's Activity name in which this test starts.
     */
    private final String startingActivityName;

    /**
     * A class name for this test case.
     */
    private final String testCaseName;

    /**
     * A method name for this test case.
     */
    private final String testMethodName;

    /**
     * The Espresso actions to write into the final String.
     */
    private final List<EspressoAction> actions;

    /**
     * The Java classes to include in the imports of the final String.
     */
    private final Set<String> classImports;

    /**
     * The Java methods to include in the imports of the final String.
     */
    private final Set<String> staticImports;

    /**
     * The Espresso dependency prefix.
     */
    private String espressoDependencyGroup;

    /**
     * The Espresso dependency version.
     */
    private String espressoDependencyVersion;

    /**
     * Indicates whether we are making an Espresso test for running inside the AUT's codebase,
     * or to run it with our own Espresso-test-apk-builder.
     */
    private boolean convertingForAUTsCodeBase = false;

    /**
     * Internal StringBuilder to compose the final String.
     */
    private StringBuilder builder;

    public EspressoTestCaseStringConverter(String packageName,
                                           String startingActivityName,
                                           String testCaseName,
                                           String testMethodName) {
        this.packageName = packageName;
        this.startingActivityName = startingActivityName;
        this.testCaseName = testCaseName;
        this.testMethodName = testMethodName;

        this.actions = new ArrayList<>();
        this.classImports = new HashSet<>();
        this.staticImports = new HashSet<>();

        // By default, we assume that we are going to write an Espresso test case for Espresso's
        // latest version (i.e., using the AndroidX testing package).
        // All versions of this dependency are listed in the following link:
        // https://mvnrepository.com/artifact/androidx.test.espresso/espresso-core
        espressoDependencyGroup = "androidx.test.espresso";
        espressoDependencyVersion = "3.4.0";

        addDefaultImports();
    }

    /**
     * Adds an Espresso action String representation into the final test case's body.
     * The class and static imports of the Espresso action are included as well.
     * @param espressoAction the action to add.
     */
    public void addAction(EspressoAction espressoAction) {
        this.actions.add(espressoAction);
        this.classImports.addAll(espressoAction.getNeededClassImports());
        this.staticImports.addAll(espressoAction.getNeededStaticImports());
    }

    /**
     * Change whether this converter should produce a test case to be used inside AUT's codebase
     * or for our own Espresso-test-apk-builder program.
     * @param convertingForAUTsCodeBase boolean
     */
    public void setConvertingForAUTsCodeBase(boolean convertingForAUTsCodeBase) {
        this.convertingForAUTsCodeBase = convertingForAUTsCodeBase;
    }

    @Override
    public String getCode() {
        builder = new StringBuilder();

        writePackage();
        writeImports();

        writeTestClassHeader();

        writeTestActivityRule();

        writeTestMethodHeader();
        writeTestBody();
        writeTestMethodFooter();

        writeTestClassFooter();

        return builder.toString();
    }

    @Override
    public Set<String> getNeededClassImports() {
        return this.classImports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        return this.staticImports;
    }

    /**
     * Adds default imports that any Android test case must have.
     */
    private void addDefaultImports() {
        // Espresso stuff
        classImports.add("androidx.test.rule.ActivityTestRule");
        classImports.add("androidx.test.runner.AndroidJUnit4");
        classImports.add("androidx.test.filters.LargeTest");

        // JUnit stuff
        classImports.add("org.junit.Rule");
        classImports.add("org.junit.Test");
        classImports.add("org.junit.runner.RunWith");

        if (convertingForAUTsCodeBase) {
            // Add AUT's resources as a default import
            classImports.add(String.format("%s.R", packageName));
        }
    }

    /**
     * Writes a line into the current In-Progress String test case.
     * The New Line character is added after it.
     * @param line string
     */
    private void writeLine(String line) {
        builder.append(String.format("%s\n", line));
    }

    /**
     * Writes a Java expression as a new line into the current In-Progress String test case.
     * The semi-colon character is added after it.
     * @param expression string
     */
    private void writeExpressionLine(String expression) {
        writeLine(String.format("%s;", expression));
    }

    /**
     * Writes a Java annotation as a new line into the current In-Progress String test case.
     * The "@" character is added before it.
     * @param annotation string
     */
    private void writeAnnotationLine(String annotation) {
        writeLine(String.format("@%s", annotation));
    }

    /**
     * Writes an empty line into the current In-Progress String test case.
     */
    private void writeEmptyLine() {
        writeLine("");
    }

    /**
     * Writes appropriate package name header into the current In-Progress String test case.
     */
    private void writePackage() {
        if (convertingForAUTsCodeBase) {
            writeExpressionLine(String.format("package %s", packageName));
        } else {
            writeExpressionLine("package org.mate.espresso.tests");
        }
        writeEmptyLine();
    }

    /**
     * Writes class and static imports collected so far into the current In-Progress String test
     * case.
     */
    private void writeImports() {
        for (String fullyQualifiedClassName : classImports) {
            writeExpressionLine(String.format("import %s",
                    normalizeImport(fullyQualifiedClassName)));
        }

        writeEmptyLine();

        for (String fullyQualifiedMethodName : staticImports) {
            writeExpressionLine(String.format("import static %s",
                    normalizeImport(fullyQualifiedMethodName)));
        }

        writeEmptyLine();
    }

    /**
     * Writes appropriate test class header into the current In-Progress String test case.
     */
    private void writeTestClassHeader() {
        writeAnnotationLine("LargeTest");
        writeAnnotationLine("RunWith(AndroidJUnit4.class)");
        if (convertingForAUTsCodeBase) {
            writeLine(String.format("public class %s {", testCaseName));
        } else {
            writeLine(String.format("public class %s extends TestUtils {", testCaseName));
        }
        writeEmptyLine();
    }

    /**
     * Writes appropriate test activity rule into the current In-Progress String test case.
     */
    private void writeTestActivityRule() {
        if (convertingForAUTsCodeBase) {
            writeAnnotationLine("Rule");
            writeExpressionLine(String.format("public ActivityTestRule<%s> mActivityTestRule = " +
                    "new ActivityTestRule<>(%s.class)", startingActivityName, startingActivityName));
        } else {
            writeLine("static {");
            writeExpressionLine(String.format("PACKAGE_NAME = \"%s\"", packageName));
            writeExpressionLine(String.format("START_ACTIVITY_NAME = \"%s\"", startingActivityName));
            writeLine("}");
        }
        writeEmptyLine();
    }

    /**
     * Writes appropriate test method header into the current In-Progress String test case.
     */
    private void writeTestMethodHeader() {
        writeAnnotationLine("Test");
        writeLine(String.format("public void %s() {", testMethodName));
        writeEmptyLine();
    }

    /**
     * Writes the Espresso actions collected so far into the current In-Progress String test case.
     */
    private void writeTestBody() {
        for (EspressoAction action : this.actions) {
            writeExpressionLine(action.getCode());
        }
    }

    /**
     * Writes test method closing brace character into the current In-Progress String test case.
     */
    private void writeTestMethodFooter() {
        writeLine("}");
        writeEmptyLine();
    }

    /**
     * Writes test class closing brace character into the current In-Progress String test case.
     */
    private void writeTestClassFooter() {
        writeLine("}");
        writeEmptyLine();
    }

    /**
     * Converts an import into androidx or android.support as needed.
     */
    private String normalizeImport(String anImport) {
        if (anImport.contains("android.support") && espressoDependencyGroup.contains("androidx")) {
            return anImport.replace("android.support", "androidx");
        }

        if (anImport.contains("androidx") && espressoDependencyGroup.contains("android.support")) {
            return anImport.replace("androidx", "android.support");
        }

        return anImport;
    }
}
