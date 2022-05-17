package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a fitness metric based on 'branch coverage' for multi-objective algorithms. This
 * requires that the AUT has been instrumented with the branch coverage module. A fitness
 * value of '1' indicates that the branch has been covered, '0' indicates non-covered.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class BranchMultiObjectiveFitnessFunction<T> implements IFitnessFunction<T> {

    // a cache that stores for each branch the set of test cases and its fitness value
    private static final Map<String, Map<IChromosome, Double>> cache = new HashMap<>();

    // all branches (shared by instances)
    private static final List<String> branches = new ArrayList<>();

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
        cache.put(branch, new HashMap<>());
    }

    /**
     * Computes the branch fitness value for the given chromosome. A cache is employed to make
     * subsequent requests faster. A fitness value of 1 indicates that the branch has been covered,
     * 0 indicates non-covered.
     *
     * @param chromosome The chromosome for which we want to retrieve its fitness value.
     * @return Returns the fitness value for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {

        if (!cache.get(branch).containsKey(chromosome)) {

            // retrieves the fitness value for every single branch
            List<Double> branchFitnessVector = FitnessUtils.getFitness(chromosome, branches);

            // update the cache
            for (int i = 0; i < branchFitnessVector.size(); i++) {
                cache.get(branches.get(i)).put(chromosome, branchFitnessVector.get(i));
            }
        }

        return cache.get(branch).get(chromosome);
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the branch coverage.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the value 0 if the branch was not covered by
     * the chromosome, otherwise the value 1 is returned.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }

    /**
     * Removes chromosomes from the cache that are no longer in use in order to avoid memory issues.
     *
     * @param chromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> chromosomes) {

        if (branches.size() == 0 || cache.size() == 0) {
            return;
        }

        List<IChromosome<T>> activeChromosomes = new ArrayList<>(chromosomes);

        int count = 0;
        for (String branch : branches) {
            Map<IChromosome, Double> branchCache = cache.get(branch);
            for (IChromosome chromosome : new ArrayList<>(branchCache.keySet())) {
                if (!activeChromosomes.contains(chromosome)) {
                    branchCache.remove(chromosome);
                    count++;
                }
            }
        }
        MATE.log_acc("Cleaning cache: " + count + " inactive chromosome removed.");
    }
}
