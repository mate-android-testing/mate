package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome_factory.IntegerSequenceChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.IntegerSequencePointCrossOverFunction;
import org.mate.exploration.genetic.fitness.GenotypePhenotypeMappedFitnessFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoverageFitnessFunction;
import org.mate.exploration.genetic.mutation.IntegerSequencePointMutationFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.exploration.genetic.util.ge.AndroidListBasedEqualWeightedDecisionBiasedMapping;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.mate.Properties.GE_TEST_CASE_ENDING_BIAS_PER_TEN_THOUSAND;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEEqualWeightedGE {

    @Test
    public void useAppContext() throws Exception {
        MATE.log_acc("Starting Standard GE Algorithm...");

        MATE mate = new MATE();

        List<IFitnessFunction<List<Integer>>> fitnessFunctions = new ArrayList<>();
        fitnessFunctions.add(new GenotypePhenotypeMappedFitnessFunction<>(
                new AndroidListBasedEqualWeightedDecisionBiasedMapping(GE_TEST_CASE_ENDING_BIAS_PER_TEN_THOUSAND()),
                new LineCoverageFitnessFunction<TestCase>()
        ));

        final IGeneticAlgorithm<List<Integer>> standardGE = new StandardGeneticAlgorithm<>(
                new IntegerSequenceChromosomeFactory(Properties.GE_SEQUENCE_LENGTH()),
                new FitnessProportionateSelectionFunction<List<Integer>>(),
                new IntegerSequencePointCrossOverFunction(),
                new IntegerSequencePointMutationFunction(),
                fitnessFunctions,
                new NeverTerminationCondition(),
                Properties.POPULATION_SIZE(),
                Properties.POPULATION_SIZE() * 2,
                Properties.P_CROSSOVER(),
                Properties.P_MUTATE()
        );

        mate.testApp(standardGE);
    }
}
