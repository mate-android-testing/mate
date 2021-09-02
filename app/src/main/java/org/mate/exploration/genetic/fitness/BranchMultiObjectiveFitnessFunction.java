package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a fitness metric based on branch coverage for multi-objective algorithms. This
 * requires that the AUT has been instrumented with the branch coverage module. A fitness
 * value of '1' indicates that the branch has been covered, '0' indicates non-covered.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BranchMultiObjectiveFitnessFunction<T> implements IFitnessFunction<T> {

    // a cache that stores for each branch the set of test cases and its fitness value
    private static final Map<String, Map<IChromosome, Double>> cache = new HashMap<>();

    // all branches (shared by instances)
    private static List<String> branches = new ArrayList<>();

    // the current branch we want to evaluate this fitness function against
    private final String branch;

    /**
     * Initialises the fitness function with the given branch as target.
     *
     * @param branch The target branch.
     */
    public BranchMultiObjectiveFitnessFunction(String branch) {
        this.branch = branch;
        branches.add(branch);
        cache.put(branch, new HashMap<IChromosome, Double>());
    }

    /**
     * Retrieves the branch fitness value for the given chromosome.
     * A cache is employed to make subsequent requests faster. A fitness value of '1' indicates
     * that the branch has been covered, '0' indicates non-covered.
     *
     * @param chromosome The chromosome for which we want to retrieve its fitness value.
     * @return Returns the fitness value for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        double branchFitnessValue;

        if (cache.get(branch).containsKey(chromosome)) {
            branchFitnessValue = cache.get(branch).get(chromosome);
        } else {
            // retrieves the fitness value for every single branch
            List<Double> branchFitnessVector = FitnessUtils.getFitness(chromosome, branches);

            // insert them into the cache
            for (int i = 0; i < branchFitnessVector.size(); i++) {
                cache.get(branches.get(i)).put(chromosome, branchFitnessVector.get(i));
            }

            branchFitnessValue = cache.get(branch).get(chromosome);
        }

        return branchFitnessValue;
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }
}
