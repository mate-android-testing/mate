package org.mate.exploration.ant;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.ui.WidgetAction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

public class AntColony {
    private final UIAbstractionLayer uiAbstractionLayer;
    private final AntStatsLogger antStatsLogger;

    public AntColony() {
        uiAbstractionLayer = MATE.uiAbstractionLayer;
        antStatsLogger = new AntStatsLogger();
    }

    public void run() {

        //TODO start algorithm
        String targetLine = Properties.TARGET_LINE();
        LineCoveredPercentageFitnessFunction lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        //Liste an momentan möglichen Aktionen
        List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();

        //Aktion ausführen
        TestCase testCase = TestCase.newInitializedTestCase();
        testCase.updateTestCase(executableActions.get(0), "0");

        //Daten in Datei schreiben
        antStatsLogger.write("asdf\n");
        //... am Schluss zu machen
        antStatsLogger.close();

    }
}
