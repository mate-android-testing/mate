package org.mate.service;

import org.mate.service.execution.ExecuteMATEAccManualExploration;
import org.mate.service.execution.ExecuteMATEEqualWeightedGE;
import org.mate.service.execution.ExecuteMATEGeneticAlgorithm;
import org.mate.service.execution.ExecuteMATEGreyBoxFuzzing;
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

public class MATERunner {
    public static void run(String packageName, String algorithm) {
        switch (algorithm) {
            case "AccManualExploration": {
                ExecuteMATEAccManualExploration.run(packageName);
            }
            case "EqualWeightedGE": {
                ExecuteMATEEqualWeightedGE.run(packageName);
            }
            case "GeneticAlgorithm": {
                ExecuteMATEGeneticAlgorithm.run(packageName);
            }
            case "GreyBoxFuzzing": {
                ExecuteMATEGreyBoxFuzzing.run(packageName);
            }
            case "HeuristicRandomExploration": {
                ExecuteMATEHeuristicRandomExploration.run(packageName);
            }
            case "ListAnalogousGE": {
                ExecuteMATEListAnalogousGE.run(packageName);
            }
            case "MIO": {
                ExecuteMATEMIO.run(packageName);
            }
            case "MOSA": {
                ExecuteMATEMOSA.run(packageName);
            }
            case "NoveltySearch": {
                ExecuteMATENoveltySearch.run(packageName);
            }
            case "NSGAII": {
                ExecuteMATENSGAII.run(packageName);
            }
            case "OnePlusOne": {
                ExecuteMATEOnePlusOne.run(packageName);
            }
            case "PrimitiveStandardGA": {
                ExecuteMATEPrimitiveStandardGA.run(packageName);
            }
            case "RandomExploration": {
                ExecuteMATERandomExploration.run(packageName);
            }
            case "RandomExplorationIntent": {
                ExecuteMATERandomExplorationIntent.run(packageName);
            }
            case "RandomSearch": {
                ExecuteMATERandomSearch.run(packageName);
            }
            case "RandomWalk": {
                ExecuteMATERandomWalk.run(packageName);
            }
            case "ReplayRun": {
                ExecuteMATEReplayRun.run(packageName);
            }
            case "Sapienz": {
                ExecuteMATESapienz.run(packageName);
            }
            case "StandardGE": {
                ExecuteMATEStandardGE.run(packageName);
            }
            case "StandardGeneticAlgorithm": {
                ExecuteMATEStandardGeneticAlgorithm.run(packageName);
            }
        }
    }
}
