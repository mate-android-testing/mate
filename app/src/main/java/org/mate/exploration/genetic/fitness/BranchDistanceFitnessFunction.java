package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates the fitness value for a given test case as defined in:
 * "It Does Matter How You Normalise the Branch Distance in Search Based Software Testing"
 *
 * @author Michael Auer
 */
public class BranchDistanceFitnessFunction implements IFitnessFunction<TestCase> {

    public static final String FITNESS_FUNCTION_ID = "branch_distance_fitness_function";

    // a cache that stores for each branch the set of test cases and its fitness value
    private static final Map<String, Map<IChromosome<TestCase>, Double>> cache = new HashMap<>();

    // all branches (should be a set like in the original algorithm???)
    private static List<String> branches = new ArrayList<>();

    // the current branch we want to evaluate this fitness function against
    private final String branch;

    public BranchDistanceFitnessFunction(String branch) {
        this.branch = branch;
        branches.add(branch);
    }

    /**
     * Retrieves the fitness value for a given test case. Note that
     * fitness values are pre-computed and the cache is queried for its value.
     * If the cache doesn't contain a fitness value for a given test case,
     * an {#link IllegalStateException} is thrown.
     *
     * @param chromosome The test case for which we want to retrieve its fitness value.
     * @return Returns the fitness value associated with the given test case.
     */
    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        if (!cache.get(branch).containsKey(chromosome)) {
            throw new IllegalStateException("Fitness for chromosome " + chromosome
                    + " not in cache. Must fetch fitness previously due to performance reasons");
        }
        return cache.get(branch).get(chromosome);
    }

    /**
     * Computes the fitness values for a given test case, i.e. evaluates the fitness value for
     * each single branch.
     *
     * @param chromosome The test case for which we want to evaluate its fitness values.
     */
    public static void retrieveFitnessValues(IChromosome<TestCase> chromosome) {

        // if there are no branches, there is no possibility to retrieve a fitness value
        if (branches.size() == 0) {
            return;
        }

        // init cache if not done yet
        if (cache.size() == 0) {
            for (String branch : branches) {
                cache.put(branch, new HashMap<IChromosome<TestCase>, Double>());
            }
        }

        // computes the branch distance fitness vector for a given test case
        MATE.log_acc("retrieving fitness values for chromosome " + chromosome);
        List<Double> branchDistanceVector = EnvironmentManager.getBranchDistanceVector(chromosome, branches);

        // if there is no branch distance vector available, we can abort
        if (branchDistanceVector.isEmpty()) {
            throw new IllegalStateException("No branch distance vector available! Aborting.");
        }

        // insert them into the cache
        for (int i = 0; i < branchDistanceVector.size(); i++) {
            cache.get(branches.get(i)).put(chromosome, branchDistanceVector.get(i));
        }
    }

    /**
     * Removes chromosome from cache that are no longer in use. (to avoid memory issues)
     */
    public static void cleanCache(List<Object> activeChromosomesAnon) {
        if (branches.size() == 0 || cache.size() == 0) {
            return;
        }

        List<IChromosome<TestCase>> activeChromosomes = new ArrayList<>();
        for (Object o : activeChromosomesAnon) {
            activeChromosomes.add((IChromosome<TestCase>) o);
        }

        int count = 0;
        for (String branch : branches) {
            Map<IChromosome<TestCase>, Double> branchCache =  cache.get(branch);
            for (IChromosome<TestCase> chromosome: new ArrayList<>(branchCache.keySet())) {
                if (!activeChromosomes.contains(chromosome)) {
                    branchCache.remove(chromosome);
                    count++;
                }
            }
        }
        MATE.log_acc("Cleaning cache: " + count + " inactive chromosome(s) removed");
    }
}
