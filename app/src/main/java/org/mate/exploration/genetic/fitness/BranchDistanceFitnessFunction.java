package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

import java.util.HashMap;
import java.util.Map;

public class BranchDistanceFitnessFunction<T> implements IFitnessFunction<T> {

    public static final String FITNESS_FUNCTION_ID = "branch_distance_fitness_function";

    private Map<IChromosome<T>, Double> cache = new HashMap<>();

    /**
     * Retrieves the branch distance value for the given chromosome.
     * As a side effect terminates execution if target vertex is reached.
     *
     * @param chromosome The chromosome for which the fitness value should be retrieved.
     * @return Returns the fitness value (branch distance) for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double branchDistance;

        if (cache.containsKey(chromosome)) {
            MATE.log_acc("Accessing cache for retrieving fitness value!");
            branchDistance = cache.get(chromosome);
        } else {
            branchDistance = FitnessUtils.getFitness(chromosome);
        }
        
        /*
        * TODO: This is a side effect, which is triggered multiple times, e.g. by logFitness().
        *  Additionally, the decision when a 'target' is satisfied might depend on the
        *  algorithm in use. Thus, a better option is to move this functionality. However, be
        *  aware that the initial population can also satisfy this condition.
         */
        // we can end execution if we covered the target vertex
        if (branchDistance == 1.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        // cache fitness value for subsequent requests
        cache.put(chromosome, branchDistance);

        return branchDistance;
    }
}
