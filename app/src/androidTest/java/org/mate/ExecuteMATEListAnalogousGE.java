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
import org.mate.exploration.genetic.mutation.IntegerSequenceLengthMutationFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.exploration.genetic.util.ge.AndroidListAnalogousMapping;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEListAnalogousGE {
    @Test
    public void useAppContext() {
        MATE.log_acc("Starting List Analogous GE Algorithm...");

        MATE mate = new MATE();

        List<IFitnessFunction<List<Integer>>> fitnessFunctions = new ArrayList<>();
        fitnessFunctions.add(new GenotypePhenotypeMappedFitnessFunction<>(
                new AndroidListAnalogousMapping(),
                new LineCoverageFitnessFunction<>()
        ));

        final IGeneticAlgorithm<List<Integer>> standardGE = new StandardGeneticAlgorithm<>(
                new IntegerSequenceChromosomeFactory(Properties.GE_SEQUENCE_LENGTH()),
                new FitnessProportionateSelectionFunction<>(),
                new IntegerSequencePointCrossOverFunction(),
                new IntegerSequenceLengthMutationFunction(Properties.GE_MUTATION_COUNT()),
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
