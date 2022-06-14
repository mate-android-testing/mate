package org.mate.utils.testcase.writer;

import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.utils.CodeProducer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EspressoTestCaseStringConverter extends CodeProducer {
    private String packageName;
    private String startingActivityName;
    private String testCaseName;
    private String testMethodName;

    private List<EspressoAction> actions;
    private Set<String> classImports;
    private Set<String> staticImports;
    private String espressoDependencyGroup;
    private String espressoDependencyVersion;

    /**
     * Indicates whether we are making an Espresso test for running inside the AUT's codebase,
     * or to run it with our own Espresso-test-apk-builder.
     */
    private boolean convertingForAUTsCodeBase = false;

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

        // By default, we assume that we are going to write a Espresso test case for Espresso's
        // latest version (i.e., using the AndroidX testing package).
        // All versions of this dependency are listed in the following link:
        // https://mvnrepository.com/artifact/androidx.test.espresso/espresso-core
        espressoDependencyGroup = "androidx.test.espresso";
        espressoDependencyVersion = "3.4.0";

        addDefaultImports();
    }

    public void addAction(EspressoAction espressoAction) {
        this.actions.add(espressoAction);
        this.classImports.addAll(espressoAction.getNeededClassImports());
        this.staticImports.addAll(espressoAction.getNeededStaticImports());
    }

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

    private void addLine(String line) {
        builder.append(String.format("%s\n", line));
    }

    private void addExpressionLine(String expression) {
        addLine(String.format("%s;", expression));
    }

    private void addAnnotationLine(String annotation) {
        addLine(String.format("@%s", annotation));
    }

    private void addEmptyLine() {
        addLine("");
    }

    private void writePackage() {
        if (convertingForAUTsCodeBase) {
            addExpressionLine(String.format("package %s", packageName));
        } else {
            addExpressionLine("package org.mate.espresso.tests");
        }
        addEmptyLine();
    }

    private void writeImports() {
        for (String fullyQualifiedClassName : classImports) {
            addExpressionLine(String.format("import %s",
                    normalizeImport(fullyQualifiedClassName)));
        }

        addEmptyLine();

        for (String fullyQualifiedMethodName : staticImports) {
            addExpressionLine(String.format("import static %s",
                    normalizeImport(fullyQualifiedMethodName)));
        }

        addEmptyLine();
    }

    private void writeTestClassHeader() {
        addAnnotationLine("LargeTest");
        addAnnotationLine("RunWith(AndroidJUnit4.class)");
        if (convertingForAUTsCodeBase) {
            addLine(String.format("public class %s {", testCaseName));
        } else {
            addLine(String.format("public class %s extends TestUtils {", testCaseName));
        }
        addEmptyLine();
    }

    private void writeTestActivityRule() {
        if (convertingForAUTsCodeBase) {
            addAnnotationLine("Rule");
            addExpressionLine(String.format("public ActivityTestRule<%s> mActivityTestRule = " +
                    "new ActivityTestRule<>(%s.class)", startingActivityName, startingActivityName));
        } else {
            addLine("static {");
            addExpressionLine(String.format("PACKAGE_NAME = \"%s\"", packageName));
            addExpressionLine(String.format("START_ACTIVITY_NAME = \"%s\"", startingActivityName));
            addLine("}");
        }
        addEmptyLine();
    }

    private void writeTestMethodHeader() {
        addAnnotationLine("Test");
        addLine(String.format("public void %s() {", testMethodName));
        addEmptyLine();
    }

    private void writeTestBody() {
        for (EspressoAction action : this.actions) {
            addExpressionLine(action.getCode());
        }
    }

    private void writeTestMethodFooter() {
        addLine("}");
        addEmptyLine();
    }

    private void writeTestClassFooter() {
        addLine("}");
        addEmptyLine();
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
