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
                           Context context) {
        switch (algorithm) {
            case "AccManualExploration": {
                ExecuteMATEAccManualExploration.run(packageName, context);
            }
            case "EqualWeightedGE": {
                ExecuteMATEEqualWeightedGE.run(packageName, context);
            }
            case "GeneticAlgorithm": {
                ExecuteMATEGeneticAlgorithm.run(packageName, context);
            }
            case "GreyBoxFuzzing": {
                ExecuteMATEGreyBoxFuzzing.run(packageName, context);
            }
            case "HeuristicRandomExploration": {
                ExecuteMATEHeuristicRandomExploration.run(packageName, context);
            }
            case "ListAnalogousGE": {
                ExecuteMATEListAnalogousGE.run(packageName, context);
            }
            case "MIO": {
                ExecuteMATEMIO.run(packageName, context);
            }
            case "MOSA": {
                ExecuteMATEMOSA.run(packageName, context);
            }
            case "NoveltySearch": {
                ExecuteMATENoveltySearch.run(packageName, context);
            }
            case "NSGAII": {
                ExecuteMATENSGAII.run(packageName, context);
            }
            case "OnePlusOne": {
                ExecuteMATEOnePlusOne.run(packageName, context);
            }
            case "PrimitiveStandardGA": {
                ExecuteMATEPrimitiveStandardGA.run(packageName, context);
            }
            case "RandomExploration": {
                ExecuteMATERandomExploration.run(packageName, context);
            }
            case "RandomExplorationIntent": {
                ExecuteMATERandomExplorationIntent.run(packageName, context);
            }
            case "RandomSearch": {
                ExecuteMATERandomSearch.run(packageName, context);
            }
            case "RandomWalk": {
                ExecuteMATERandomWalk.run(packageName, context);
            }
            case "ReplayRun": {
                ExecuteMATEReplayRun.run(packageName, context);
            }
            case "Sapienz": {
                ExecuteMATESapienz.run(packageName, context);
            }
            case "StandardGE": {
                ExecuteMATEStandardGE.run(packageName, context);
            }
            case "StandardGeneticAlgorithm": {
                ExecuteMATEStandardGeneticAlgorithm.run(packageName, context);
            }
        }
    }
}
