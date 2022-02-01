package org.mate.service;

import org.mate.commons.IRepresentationLayerInterface;
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
    public static void run(String packageName, String algorithm, IRepresentationLayerInterface representationLayer) {
        switch (algorithm) {
            case "AccManualExploration": {
                ExecuteMATEAccManualExploration.run(packageName, representationLayer);
            }
            case "EqualWeightedGE": {
                ExecuteMATEEqualWeightedGE.run(packageName, representationLayer);
            }
            case "GeneticAlgorithm": {
                ExecuteMATEGeneticAlgorithm.run(packageName, representationLayer);
            }
            case "GreyBoxFuzzing": {
                ExecuteMATEGreyBoxFuzzing.run(packageName, representationLayer);
            }
            case "HeuristicRandomExploration": {
                ExecuteMATEHeuristicRandomExploration.run(packageName, representationLayer);
            }
            case "ListAnalogousGE": {
                ExecuteMATEListAnalogousGE.run(packageName, representationLayer);
            }
            case "MIO": {
                ExecuteMATEMIO.run(packageName, representationLayer);
            }
            case "MOSA": {
                ExecuteMATEMOSA.run(packageName, representationLayer);
            }
            case "NoveltySearch": {
                ExecuteMATENoveltySearch.run(packageName, representationLayer);
            }
            case "NSGAII": {
                ExecuteMATENSGAII.run(packageName, representationLayer);
            }
            case "OnePlusOne": {
                ExecuteMATEOnePlusOne.run(packageName, representationLayer);
            }
            case "PrimitiveStandardGA": {
                ExecuteMATEPrimitiveStandardGA.run(packageName, representationLayer);
            }
            case "RandomExploration": {
                ExecuteMATERandomExploration.run(packageName, representationLayer);
            }
            case "RandomExplorationIntent": {
                ExecuteMATERandomExplorationIntent.run(packageName, representationLayer);
            }
            case "RandomSearch": {
                ExecuteMATERandomSearch.run(packageName, representationLayer);
            }
            case "RandomWalk": {
                ExecuteMATERandomWalk.run(packageName, representationLayer);
            }
            case "ReplayRun": {
                ExecuteMATEReplayRun.run(packageName, representationLayer);
            }
            case "Sapienz": {
                ExecuteMATESapienz.run(packageName, representationLayer);
            }
            case "StandardGE": {
                ExecuteMATEStandardGE.run(packageName, representationLayer);
            }
            case "StandardGeneticAlgorithm": {
                ExecuteMATEStandardGeneticAlgorithm.run(packageName, representationLayer);
            }
        }
    }
}
