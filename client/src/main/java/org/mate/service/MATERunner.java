package org.mate.service;

import android.content.Context;

import org.mate.service.execution.ExecuteMATEAccManualExploration;
import org.mate.service.execution.ExecuteMATEAimDroid;
import org.mate.service.execution.ExecuteMATEAutoBlackTest;
import org.mate.service.execution.ExecuteMATEAutoDroid;
import org.mate.service.execution.ExecuteMATEEqualWeightedGE;
import org.mate.service.execution.ExecuteMATEGeneticAlgorithm;
import org.mate.service.execution.ExecuteMATEGreyBoxCoverageFuzzing;
import org.mate.service.execution.ExecuteMATEHeuristicRandomExploration;
import org.mate.service.execution.ExecuteMATEListAnalogousGE;
import org.mate.service.execution.ExecuteMATEMIO;
import org.mate.service.execution.ExecuteMATEMOSA;
import org.mate.service.execution.ExecuteMATENSGAII;
import org.mate.service.execution.ExecuteMATENoveltySearch;
import org.mate.service.execution.ExecuteMATEOnePlusOne;
import org.mate.service.execution.ExecuteMATEPrimitiveStandardGA;
import org.mate.service.execution.ExecuteMATERandomExploration;
import org.mate.service.execution.ExecuteMATERandomExplorationIntent;
import org.mate.service.execution.ExecuteMATERandomSearch;
import org.mate.service.execution.ExecuteMATERandomWalk;
import org.mate.service.execution.ExecuteMATEReplayRun;
import org.mate.service.execution.ExecuteMATESapienz;
import org.mate.service.execution.ExecuteMATEStandardGE;
import org.mate.service.execution.ExecuteMATEStandardGeneticAlgorithm;

/**
 * Auxiliary class for routing a MATE Client exploration to the corresponding class, based on the
 * algorithm's name.
 */
public class MATERunner {
    public static void run(String packageName,
                           String algorithm,
                           Context context) {
        switch (algorithm) {
            case "AccManualExploration": {
                ExecuteMATEAccManualExploration.run(packageName, context);
                break;
            }
            case "AimDroid": {
                ExecuteMATEAimDroid.run(packageName, context);
                break;
            }
            case "AutoBlackTest": {
                ExecuteMATEAutoBlackTest.run(packageName, context);
                break;
            }
            case "AutoDroid": {
                ExecuteMATEAutoDroid.run(packageName, context);
                break;
            }
            case "EqualWeightedGE": {
                ExecuteMATEEqualWeightedGE.run(packageName, context);
                break;
            }
            case "GeneticAlgorithm": {
                ExecuteMATEGeneticAlgorithm.run(packageName, context);
                break;
            }
            case "GreyBoxCoverageFuzzing": {
                ExecuteMATEGreyBoxCoverageFuzzing.run(packageName, context);
                break;
            }
            case "HeuristicRandomExploration": {
                ExecuteMATEHeuristicRandomExploration.run(packageName, context);
                break;
            }
            case "ListAnalogousGE": {
                ExecuteMATEListAnalogousGE.run(packageName, context);
                break;
            }
            case "MIO": {
                ExecuteMATEMIO.run(packageName, context);
                break;
            }
            case "MOSA": {
                ExecuteMATEMOSA.run(packageName, context);
                break;
            }
            case "NoveltySearch": {
                ExecuteMATENoveltySearch.run(packageName, context);
                break;
            }
            case "NSGAII": {
                ExecuteMATENSGAII.run(packageName, context);
                break;
            }
            case "OnePlusOne": {
                ExecuteMATEOnePlusOne.run(packageName, context);
                break;
            }
            case "PrimitiveStandardGA": {
                ExecuteMATEPrimitiveStandardGA.run(packageName, context);
                break;
            }
            case "RandomExploration": {
                ExecuteMATERandomExploration.run(packageName, context);
                break;
            }
            case "RandomExplorationIntent": {
                ExecuteMATERandomExplorationIntent.run(packageName, context);
                break;
            }
            case "RandomSearch": {
                ExecuteMATERandomSearch.run(packageName, context);
                break;
            }
            case "RandomWalk": {
                ExecuteMATERandomWalk.run(packageName, context);
                break;
            }
            case "ReplayRun": {
                ExecuteMATEReplayRun.run(packageName, context);
                break;
            }
            case "Sapienz": {
                ExecuteMATESapienz.run(packageName, context);
                break;
            }
            case "StandardGE": {
                ExecuteMATEStandardGE.run(packageName, context);
                break;
            }
            case "StandardGeneticAlgorithm": {
                ExecuteMATEStandardGeneticAlgorithm.run(packageName, context);
                break;
            }
            default: {
                throw new IllegalStateException(String.format("Algorithm \"%s\" is not valid",
                        algorithm));
            }
        }
    }
}
