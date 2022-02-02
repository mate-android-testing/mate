package org.mate.service;

import android.content.Context;

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
    public static void run(String packageName,
                           String algorithm,
                           IRepresentationLayerInterface representationLayer,
                           Context context) {
        switch (algorithm) {
            case "AccManualExploration": {
                ExecuteMATEAccManualExploration.run(packageName, representationLayer, context);
            }
            case "EqualWeightedGE": {
                ExecuteMATEEqualWeightedGE.run(packageName, representationLayer, context);
            }
            case "GeneticAlgorithm": {
                ExecuteMATEGeneticAlgorithm.run(packageName, representationLayer, context);
            }
            case "GreyBoxFuzzing": {
                ExecuteMATEGreyBoxFuzzing.run(packageName, representationLayer, context);
            }
            case "HeuristicRandomExploration": {
                ExecuteMATEHeuristicRandomExploration.run(packageName, representationLayer,
                        context);
            }
            case "ListAnalogousGE": {
                ExecuteMATEListAnalogousGE.run(packageName, representationLayer, context);
            }
            case "MIO": {
                ExecuteMATEMIO.run(packageName, representationLayer, context);
            }
            case "MOSA": {
                ExecuteMATEMOSA.run(packageName, representationLayer, context);
            }
            case "NoveltySearch": {
                ExecuteMATENoveltySearch.run(packageName, representationLayer, context);
            }
            case "NSGAII": {
                ExecuteMATENSGAII.run(packageName, representationLayer, context);
            }
            case "OnePlusOne": {
                ExecuteMATEOnePlusOne.run(packageName, representationLayer, context);
            }
            case "PrimitiveStandardGA": {
                ExecuteMATEPrimitiveStandardGA.run(packageName, representationLayer, context);
            }
            case "RandomExploration": {
                ExecuteMATERandomExploration.run(packageName, representationLayer, context);
            }
            case "RandomExplorationIntent": {
                ExecuteMATERandomExplorationIntent.run(packageName, representationLayer, context);
            }
            case "RandomSearch": {
                ExecuteMATERandomSearch.run(packageName, representationLayer, context);
            }
            case "RandomWalk": {
                ExecuteMATERandomWalk.run(packageName, representationLayer, context);
            }
            case "ReplayRun": {
                ExecuteMATEReplayRun.run(packageName, representationLayer, context);
            }
            case "Sapienz": {
                ExecuteMATESapienz.run(packageName, representationLayer, context);
            }
            case "StandardGE": {
                ExecuteMATEStandardGE.run(packageName, representationLayer, context);
            }
            case "StandardGeneticAlgorithm": {
                ExecuteMATEStandardGeneticAlgorithm.run(packageName, representationLayer, context);
            }
        }
    }
}
