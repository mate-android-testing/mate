package org.mate.exploration.genetic.termination;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;

public class TargetLineCoveredTerminationCondition implements ITerminationCondition {
    public static final String TERMINATION_CONDITION_ID = "target_line_covered_termination_condition";

    public static TargetLineCoveredTerminationCondition INSTANCE = null;
    private LineCoveredPercentageFitnessFunction lineCoveredPercentageFitnessFunction;
    private IGeneticAlgorithm<TestCase> geneticAlgorithm;

    public TargetLineCoveredTerminationCondition() {
        INSTANCE = this;
        lineCoveredPercentageFitnessFunction = new LineCoveredPercentageFitnessFunction(Properties.TARGET_LINE());
    }

    public void setGeneticAlgorithm(IGeneticAlgorithm<TestCase> geneticAlgorithm) {
        this.geneticAlgorithm = geneticAlgorithm;
    }

    @Override
    public boolean isMet() {
        for (IChromosome<TestCase> chromosome : geneticAlgorithm.getCurrentPopulation()) {
            if (lineCoveredPercentageFitnessFunction.getFitness(chromosome) == 1.0) {
                return true;
            }
        }
        return false;
    }
}