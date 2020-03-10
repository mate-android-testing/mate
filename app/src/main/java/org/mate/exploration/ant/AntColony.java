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
import org.mate.ui.Action;
import org.mate.ui.Widget;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AntColony {
    private final UIAbstractionLayer uiAbstractionLayer;
    private final AntStatsLogger antStatsLogger;

    private double currentPheromoneStandardValue;
    private Map<WidgetAction, Double> pheromones = new HashMap<>();
    private List<IChromosome<TestCase>> testCasesList = new ArrayList<>();

    // Parameters to customize the ACO algorithm
    private static final int generationAmount = 10;
    private static final int generationSize = 2;
    private static final int antPathLength = 4;
    private static final double evaporationRate = 0.1;
    // TODO Choose appropriate deposit amount
    private static final double standardDepositAmount = 1.0;

    /* Parameter to change the process of calculating the action probability
    *  True:    Calculate the probability for a action by taking the pheromone value and
    *           multiplying it with the weight of the action (depending on the action type)
    *  False:   Only use the pheromone value of the action as the probability
    */
    private static final boolean includeActionTypeInTransition = false;

    /* Parameter to change the process of depositing pheromones.
    *  True:    Rank all ants in a generation according to the fitness value of their testcase.
    *           Best ants get to deposit most pheromones, decreasing downwards and worst ants don´t
    *           deposit any pheromones
    *  False:   Only the ant with the best fitness value in a generation gets to deposit pheromones
    */
    private static final boolean depositPheromonesWithRanking = true;

    public AntColony() {
        uiAbstractionLayer = MATE.uiAbstractionLayer;
        antStatsLogger = new AntStatsLogger();
    }

    public void run() {
        // Get the target line for ACO to generate a test for and initialise the fitness function
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        // Value to add the correct pheromone amount to unknown widget actions
        currentPheromoneStandardValue = 1.0;

        // Loop to create multiple generations of ants
        outerLoop: for (int i = 0; i < generationAmount; i++) {
            MATE.log_acc("Generation #" + (i + 1));
            antStatsLogger.write("Start of Generation #" + (i + 1));

            // Create ants and check if they reach the target line
            for (int z = 0; z < generationSize; z++){
                MATE.log_acc("Ant #" + (z + 1));
                antStatsLogger.write("Start of Ant #" + (z + 1));

                // Create an ant to traverse the app and wrap the generated testcase in a chromosome
                IChromosome<TestCase> chromosome = new Chromosome<>(runAnt());

                // Necessary lines to calculate the fitness value for the stored chromosome
                Registry.getEnvironmentManager().storeCoverageData(chromosome, null);
                LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);

                // DEBUG
                System.out.println(lineCoveredPercentageFitnessFunction.getFitness(chromosome));

                // Stop algorithm if target line was reached
                if (lineCoveredPercentageFitnessFunction.getFitness(chromosome) == 1) {
                    //TODO add necessary action for successful algorithm run (write log)
                    MATE.log_acc("ACO finished successfully");
                    break outerLoop;
                } else {
                    // Add testcase of current ant to list for later depositing of pheromones
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
            if (depositPheromonesWithRanking) {
                // Map test cases to their fitness values in a sorted treemap
                Map<Double, List<TestCase>> sortedFitnessValues = new TreeMap<>();
                for (int y = 0; y < testCasesList.size(); y++) {
                    // Retrieve fitness value for current ant
                    double fitnessValue =
                            lineCoveredPercentageFitnessFunction.getFitness(testCasesList.get(y));

                    // Check if previous ant had same fitness value
                    if (sortedFitnessValues.containsKey(fitnessValue)) {
                        // Add current testcase to the list of testcases with that fitness value
                        sortedFitnessValues.get(fitnessValue).add(testCasesList.get(y).getValue());
                    } else {
                        // Create a new map entry and add the current testcase
                        List<TestCase> tempList = new ArrayList<>();
                        sortedFitnessValues.put(fitnessValue, tempList);
                        sortedFitnessValues.get(fitnessValue).add(testCasesList.get(y).getValue());
                    }
                }

                // Deposit pheromones for the better half of testcases. Amount steadily decreases
                double depositAmount = standardDepositAmount;
                double reductionAmount = 1 / (testCasesList.size() / 2.0);
                int antsAllowedToDeposit = (int) Math.ceil(testCasesList.size() / 2.0);
                for (Map.Entry<Double, List<TestCase>> entry : sortedFitnessValues.entrySet()) {
                    // Check if all relevant ants have already deposited their pheromones
                    if(antsAllowedToDeposit > 0) {
                        for (TestCase currentTestCase : entry.getValue()) {
                            // Store the used actions of the current testcase without duplicates
                            List<WidgetAction> actionList = new ArrayList<>();
                            List<WidgetAction> eventSequence = (List<WidgetAction>)(List<?>)
                                    currentTestCase.getEventSequence();
                            for (int y = 0; y < eventSequence.size(); y++) {
                                if (!actionList.contains(eventSequence.get(y))) {
                                    actionList.add(eventSequence.get(y));
                                }
                            }

                            // Deposit pheromones for all actions used in the current testcase
                            for (int y = 0; y < actionList.size(); y++) {
                                double newValue = pheromones.get(actionList.get(y)) + depositAmount;
                                pheromones.put(actionList.get(y), newValue);
                            }

                            // Updating of iteration variables
                            depositAmount -= reductionAmount;
                            antsAllowedToDeposit--;
                        }
                    } else {
                        break;
                    }
                }

            } else {
                // Set the cache to the testcase of the first ant in the generation
                IChromosome<TestCase> bestTestCase = testCasesList.get(0);

                // Compare each of the test cases of all ants in the generation with the current
                // best one and store the best of both in the cache
                for (int y = 1; y < testCasesList.size(); y++) {
                    if (lineCoveredPercentageFitnessFunction.getFitness(testCasesList.get(y)) >
                            lineCoveredPercentageFitnessFunction.getFitness(bestTestCase)) {
                        bestTestCase = testCasesList.get(y);
                    }
                }

                // Store the used actions of the best testcase without duplicates
                List<WidgetAction> actionList = new ArrayList<>();
                List<WidgetAction> eventSequence =
                        (List<WidgetAction>)(List<?>) bestTestCase.getValue().getEventSequence();
                for (int y = 0; y < eventSequence.size(); y++) {
                    if (!actionList.contains(eventSequence.get(y))) {
                        actionList.add(eventSequence.get(y));
                    }
                }

                // Deposit pheromones for all actions used in the best testcase
                for (int y = 0; y < actionList.size(); y++) {
                    double newValue = pheromones.get(actionList.get(y)) + standardDepositAmount;
                    pheromones.put(actionList.get(y), newValue);
                }
            }

            antStatsLogger.write("End of Generation #" + (i + 1));
        }
        // Close the logger
        antStatsLogger.close();
    }

    /**
     * Method to create ants that run through the app to create a testcase
     * @return the generated testcase
     */
    private TestCase runAnt() {
        // Reset the current App to guarantee standardized testing starting at the same state
        MATE.uiAbstractionLayer.resetApp();

        // Initialise probabilities and create a new testcase for the current ant
        Map<WidgetAction, Double> probabilities = new HashMap<>();
        TestCase testCase = TestCase.newInitializedTestCase();

        // Start the loop to traverse the app with antPathLength-many steps
        for (int i = 0; i < antPathLength; i++) {
            // Get list possible actions to execute
            List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();
            boolean prematureShutdown;

            // If there is no executable action stop MATE and throw exception
            if (executableActions.size() == 0) {
                throw new IllegalStateException("No possible Transitions available! App contains" +
                        "a dead end.");

            // If there is only one possible widget action execute that one
            } else if (executableActions.size() == 1) {
                // Set pheromone value for the action if it does not already have one
                if (!pheromones.containsKey(executableActions.get(0))) {
                    pheromones.put(executableActions.get(0), currentPheromoneStandardValue);
                }

                // Execute the widget action and update the testcase
                prematureShutdown = !testCase.updateTestCase(executableActions.get(0), "" + i);

            } else {
                // Set pheromone values for the available widget actions without one
                for (WidgetAction action : executableActions) {
                    if (!pheromones.containsKey(action)) {
                        pheromones.put(action, currentPheromoneStandardValue);
                    }
                }

                // Store probabilities for each action with or without factoring in the action type
                if(includeActionTypeInTransition) {
                    // Calculate attractiveness for all available actions and store the results
                    for (WidgetAction action : executableActions) {
                        // Get pheromone value for the current action
                        double pheromoneValue = pheromones.get(action);

                        // Get the weight for the current action type
                        double actionTypeWeight = getActionTypeWeight(action);

                        // Calculate attractiveness for the current action (pheromone * action type)
                        double probability = (pheromoneValue*actionTypeWeight);

                        // Store the calculated value for the current action
                        probabilities.put(action, probability);
                    }
                } else {
                    // Store pheromone value of all actions as their probability
                    for (WidgetAction action : executableActions) {
                        probabilities.put(action, pheromones.get(action));
                    }
                }

                // Sum up all the probabilities
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
                // Execute the widget action and update the testcase
                prematureShutdown = !testCase.updateTestCase(currentAction, "" + i);
            }

            // TODO Comment
            if (prematureShutdown) {
                break;
            }


            // Reset used variables
            probabilities.clear();
        }
        return testCase;
    }

    /**
     * Determine the weight for an action depending on the type of the action
     * @param action the action to get a weight value for
     * @return the determined value
     */
    private Double getActionTypeWeight (WidgetAction action) {
        double eventTypeWeight;
        switch (action.getActionType()) {
            case SWIPE_UP:
            case SWIPE_DOWN:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
            case BACK:
                eventTypeWeight = 0.5;
                break;
            case MENU:
                eventTypeWeight = 2;
                break;
            default:
                eventTypeWeight = 1;
                break;
        }
        return eventTypeWeight;
    }
}
