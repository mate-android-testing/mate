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
import java.util.HashMap;
import java.util.List;

public class AntColony {
    private final UIAbstractionLayer uiAbstractionLayer;
    private final AntStatsLogger antStatsLogger;

    private static final int generationAmount = 5;
    private static final int generationSize = 1;
    private static final int antPathLength = 20;
    private static final double evaporationRate = 0.1;
    //TODO change depending of the datatype for interactionelements
    private static HashMap<Integer, Double> pheromones = new HashMap<>();
    private double currentPheromoneStandardValue;
    private Boolean targetReached;

    //TODO Set start and end for the algorithm

    public AntColony() {
        uiAbstractionLayer = MATE.uiAbstractionLayer;
        antStatsLogger = new AntStatsLogger();
    }

    public void run() {

        //TODO start algorithm (Vorgegeben)
        String targetLine = Properties.TARGET_LINE();
        LineCoveredPercentageFitnessFunction lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);


        // Value to add the correct pheromone amount to unknown widget actions
        currentPheromoneStandardValue = 0.5;

        // Loop to create multiple generations of ants
        outerLoop: for (int i = 0; i < generationAmount; i++) {
            // Create ants and update pheromone values accordingly
            for (int z = 0; z < generationSize; z++){
                // Create a ant to traverse the graph
                runAnt();

                // Stop algorithm if target was reached
                if (targetReached) {
                    //TODO add necessary action for successful run (print message, etc)
                    break outerLoop;
                }

                // Pheromone evaporation
                /*
                pheromones.entrySet().forEach(entry->{
                    entry.setValue((1-evaporationRate)*entry.getValue());
                });
                */

                // Deposit pheromones
                //TODO add loop for each element used in ant runs to deposit pheromones
            }
        }



        //TODO delete examples after using them (Vorgegeben)
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

    //TODO finish ant method
    private void runAnt() {
        //TODO set current and previous WA and other variables
        HashMap<WidgetAction, Double> probabilities = new HashMap<>();

        // Start the loop to traverse the app with antPathLength-many steps
        for (int i = 0; i < antPathLength; i++) {
            // Get possible options to move through the app
            List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();

            // If there is only one possible widget action execute that one
            if (executableActions.size() == 1) {
                // TODO set pheromone value for the viable option (if not already existing)

                // TODO use the only option and update relevant variables
            } else {
                // Set pheromone values for the available widget actions without one
                //TODO set pheromone values if not yet existing)

                // Calculate attractiveness for all available actions and store their probabilities
                //TODO add iteration over all executableActions
                while(targetReached) {
                    // Get pheromone value for the current action
                    double currentPheromoneValue = pheromones.get(currentWidgetAction);

                    // Calculate attractiveness for the current action
                    double probability = (Math.pow(pheromone));

                    // Reduce probability if the current action is the previous action (step back)
                    if () {
                        probability /= 2;
                    }

                    // Store the calculated value in combination with the target action for the current option
                    probabilities.put(, probability);
                }

                // Sum up all the probabilites
                double sumProbabilities = probabilities.values().stream().mapToDouble(v -> v).sum();

                // Calculate relative probability for each option
                //TODO iterate over probabilities (und teile jede durch die gesamtwahrscheinlichkeit)

                // Determine the next action with roulette and make the step
            }

            // Check if target was reached in this step and stop the ant if that is the case


            // Reset used variables if the ant continues to traverse the app
            probabilities.clear();
        }
    }
}
