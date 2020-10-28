package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.model.TestCase;

public class BranchDistanceFitnessFunction implements IFitnessFunction<TestCase> {

    public static final String FITNESS_FUNCTION_ID = "branch_distance_fitness_function";

    /*
    * FIXME: Caching the fitness values would be only valid for a single iteration, e.g.
    *  in iteration 1 both log_fitness() and evolve() require the fitness value. Thus, we
    *  would require some clear cache mechanism (static or part of interface). However,
    *  caching only makes sense for branch distance and line fitness.
     */

    /**
     * Retrieves the branch distance value for the given chromosome.
     * As a side effect terminates execution if target vertex is reached.
     *
     * @param chromosome The chromosome for which the fitness value should be retrieved.
     * @return Returns the fitness value (branch distance) for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {

        double branchDistance = Registry.getEnvironmentManager().getBranchDistance(chromosome.toString());
        MATE.log("Branch Distance for chromosome: " + chromosome + ": " + branchDistance);

        // we can end execution if we covered the target vertex
        if (branchDistance == 1.0) {
            MATE.log("Covered target vertex. Abort execution!");
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return branchDistance;
    }
}
