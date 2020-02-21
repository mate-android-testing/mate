package org.mate.exploration.ant;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.ui.WidgetAction;
import org.mate.utils.Coverage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AntColony {
    private final UIAbstractionLayer uiAbstractionLayer;
    private final AntStatsLogger antStatsLogger;

    private static final int generationAmount = 5;
    private static final int generationSize = 1;
    private static final int antPathLength = 20;
    private static final double evaporationRate = 0.1;
    private double currentPheromoneStandardValue;
    private Map<WidgetAction, Double> pheromones = new HashMap<>();
    private List<IChromosome<TestCase>> testCasesList = new ArrayList<>();

    public AntColony() {
        uiAbstractionLayer = MATE.uiAbstractionLayer;
        antStatsLogger = new AntStatsLogger();
    }

    public void run() {

        //TODO start algorithm (Vorgegeben)
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        // Value to add the correct pheromone amount to unknown widget actions
        currentPheromoneStandardValue = 0.5;

        // Loop to create multiple generations of ants
        outerLoop: for (int i = 0; i < generationAmount; i++) {
            MATE.log_acc("Generation #" + (i + 1));
            antStatsLogger.write("Start of Generation #" + (i + 1));

            // Create ants and check if they reach the target line
            for (int z = 0; z < generationSize; z++){
                MATE.log_acc("Ant #" + (z + 1));
                antStatsLogger.write("Start of Ant #" + (z + 1));

                // Create a ant to traverse the graph
                IChromosome<TestCase> chromosome = new Chromosome<>(runAnt());

                Registry.getEnvironmentManager().storeCoverageData(chromosome, null);
                LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);


                // Stop algorithm if target line was reached (Fitness of testCase == 1)
                if (lineCoveredPercentageFitnessFunction.getFitness(chromosome) == 1) {
                    //TODO add necessary action for successful algorithm run (print message, etc)
                    break outerLoop;
                } else {
                    // Add testcase of current ant to list for later deposition of pheromones
                    testCasesList.add(chromosome);
                }

                antStatsLogger.write("End of Ant #" + (z + 1));
            }
            // Pheromone evaporation
            for (Map.Entry<WidgetAction, Double> entry : pheromones.entrySet()) {
                entry.setValue((1-evaporationRate)*entry.getValue());
            }
            currentPheromoneStandardValue *= (1-evaporationRate);

            // Deposit pheromones
            //TODO add loop for each element used in ant runs to deposit pheromones depending on fitness
            //(Noch nicht ganz sicher ob deposit einmal pro generation oder nach jeder ameise)

            antStatsLogger.write("End of Generation #" + (i + 1));
        }
        // Close the logger
        antStatsLogger.close();
    }

    //TODO finish ant method
    private TestCase runAnt() {
        MATE.uiAbstractionLayer.restartApp();

        //TODO set variables (start widget action, previous action, current action), start test case, etc...
        Map<WidgetAction, Double> probabilities = new HashMap<>();
        TestCase testCase = TestCase.newInitializedTestCase();

        // Start the loop to traverse the app with antPathLength-many steps
        for (int i = 0; i < antPathLength; i++) {
            // Get list possible actions to execute
            List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();

            // If there is only one possible widget action execute that one
            if (executableActions.size() == 1) {
                if (!pheromones.containsKey(executableActions.get(0))) {
                    pheromones.put(executableActions.get(0), currentPheromoneStandardValue);
                }

                // TODO use the only option and update relevant variables
                testCase.updateTestCase(executableActions.get(0), "" + i);
            } else {
                // Set pheromone values for the available widget actions without one
                for (WidgetAction action : executableActions) {
                    if (!pheromones.containsKey(action)) {
                        pheromones.put(action, currentPheromoneStandardValue);
                    }
                }

                // Calculate attractiveness for all available actions and store their probabilities
                for (WidgetAction action : executableActions) {
                    // Get pheromone value for the current action
                    double currentPheromoneValue = pheromones.get(action);

                    // Calculate attractiveness for the current action (pheromone * fitness) möglich?? falls nicht prob = pheromone
                    // Eventuell action type (??) einbeziehen in die bewertung
                    double probability = (Math.pow(currentPheromoneValue, 2));

                    // Reduce probability if the current action is the previous action (step back)
                    //TODO Check if necessary
                    /*
                    if (action.equals(previousAction)) {
                        probability /= 2;
                    }

                     */

                    // Store the calculated value in combination with the target action for the current option
                    probabilities.put(action, probability);
                }

                // Sum up all the probabilites
                double sumProbabilities = 0.0;
                for (Map.Entry<WidgetAction, Double> entry : probabilities.entrySet()) {
                    sumProbabilities += entry.getValue();
                }

                // Calculate relative probability for each option
                for (Map.Entry<WidgetAction, Double> entry : probabilities.entrySet()) {
                    entry.setValue(entry.getValue() / sumProbabilities);
                }

                // Determine the next action with roulette and make the step
                double randomValue = Math.random();
                double sum = 0.0;
                WidgetAction currentAction = null;
                for (Map.Entry<WidgetAction, Double> entry : probabilities.entrySet()) {
                    sum += entry.getValue();
                    currentAction = entry.getKey();
                    if (sum > randomValue) {
                        break;
                    }
                }
                testCase.updateTestCase(currentAction, "" + i);
            }

            // Reset used variables
            probabilities.clear();
        }
        return testCase;
    }

    /*
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
    */
}
