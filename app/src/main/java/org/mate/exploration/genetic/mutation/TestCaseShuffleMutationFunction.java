package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.state.IScreenState;
import org.mate.state.equivalence.IStateEquivalence;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.mate.state.equivalence.StateEquivalenceFactory.getStateEquivalenceCheck;

/**
 * Provides a shuffle mutation function for {@link TestCase}s.
 */
public class TestCaseShuffleMutationFunction implements IMutationFunction<TestCase> {

    /**
     * Provides primarily information about the current screen.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The gui model constructed so far.
     */
    private final IGUIModel guiModel = Registry.getUiAbstractionLayer().getGuiModel();

    /**
     * Provides the logic to shuffle transitions.
     */
    private final ShufflePath shufflePath = new ShufflePath();

    /**
     * The maximal number of actions per test case.
     */
    private final int maxNumEvents;

    /**
     * Whether we deal with a test suite execution, i.e. whether the used chromosome factory
     * produces {@link org.mate.model.TestSuite}s or not.
     */
    private boolean isTestSuiteExecution = false;

    /**
     * Initialises the test case shuffle mutation function.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public TestCaseShuffleMutationFunction(int maxNumEvents) {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.maxNumEvents = maxNumEvents;
    }

    // TODO: might be replaceable with chromosome factory property in the future
    /**
     * Defines whether we deal with a test suite execution or not.
     *
     * @param testSuiteExecution Indicates if we deal with a test suite execution or not.
     */
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    /**
     * Mutates the test case by shuffling the path described by the action and state sequence.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome) {

        final TestCase testCase = chromosome.getValue();
        MATE.log_debug("Sequence length before mutation: " + testCase.getActionSequence().size());

        final List<Path> path = pathFromTestCase(chromosome.getValue());

        if (path.isEmpty()) {
            return executeActions(Collections.emptyList());
        }

        final List<Path> shuffledPath = shufflePath.shuffle(path, path.get(0).getSource());

        final List<Action> actions = shuffledPath.stream()
                .map(Path::getAction)
                .collect(Collectors.toList());

        return executeActions(actions);
    }

    /**
     * Computes the path (sequence) from the given test case.
     *
     * @param testCase The given test case.
     * @return Returns the path (sequence) for the given test case.
     */
    private List<Path> pathFromTestCase(final TestCase testCase) {

        final List<IScreenState> screenStates = testCase.getStateSequence()
                .stream()
                .map(guiModel::getScreenStateById)
                .collect(Collectors.toList());

        final List<Action> actions = testCase.getActionSequence();

        if (actions.size() + 1 != screenStates.size()) {
            throw new IllegalStateException(
                    "There should be one more screen state than action in a test case.");
        }

        final List<Path> path = new ArrayList<>(actions.size());

        for (int i = 0; i < actions.size(); ++i) {
            path.add(new Path(screenStates.get(i), actions.get(i), screenStates.get(i + 1)));
        }

        MATE.log_acc("Original path: " + path.stream()
                .map(Path::toShortString)
                .collect(Collectors.joining(" -> ")));

        if (!shufflePath.getStats().isEmpty()) {
            MATE.log_debug(shufflePath.getStats().toString());
            shufflePath.getStats().clear();
        }

        return path;
    }

    /**
     * Executes the given actions and wraps them in a test case chromosome.
     *
     * @param actions The list of actions to be executed.
     * @return Returns the test case containing the executed actions.
     */
    private IChromosome<TestCase> executeActions(final List<Action> actions) {

        uiAbstractionLayer.resetApp();

        final TestCase mutant = TestCase.newInitializedTestCase();
        final IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        try {

            for (int i = 0; i < actions.size(); i++) {

                final Action action = actions.get(i);

                // Check that the ui action is applicable in the current state.
                if (action instanceof UIAction
                        && !uiAbstractionLayer.getExecutableUIActions().contains(action)) {
                    MATE.log_warn("TestCaseShuffleMutationFunction: Action (" + i + ") "
                            + action.toShortString() + " not applicable!");
                    break; // Fill up with random actions.
                } else if (!mutant.updateTestCase(action, i)) {
                    MATE.log_warn("TestCaseShuffleMutationFunction: Action ( " + i + ") "
                            + action.toShortString() + " crashed or left AUT.");
                    return mutatedChromosome;
                }
            }

            // Fill up the remaining slots with random actions.
            final int currentTestCaseSize = mutant.getActionSequence().size();

            for (int i = currentTestCaseSize; i < maxNumEvents; ++i) {
                final Action newAction = selectRandomAction();
                if (!mutant.updateTestCase(newAction, i)) {
                    MATE.log_warn("TestCaseShuffleMutationFunction: Action ( " + i + ") "
                            + newAction.toShortString() + " crashed or left AUT.");
                    return mutatedChromosome;
                }
            }
        } finally {

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel = (SurrogateModel) guiModel;
                surrogateModel.updateTestCase(mutant);
            }

            if (!isTestSuiteExecution) {
                /*
                 * If we deal with a test suite execution, the storing of coverage
                 * and fitness data is handled by the test suite mutation operator itself.
                 */
                FitnessUtils.storeTestCaseChromosomeFitness(mutatedChromosome);
                CoverageUtils.storeTestCaseChromosomeCoverage(mutatedChromosome);
                CoverageUtils.logChromosomeCoverage(mutatedChromosome);
            }

            mutant.finish();
            MATE.log_debug("Sequence length after mutation: " + mutant.getActionSequence().size());
        }

        return mutatedChromosome;
    }

    /**
     * Selects a random action applicable in the current state. Respects the intent probability.
     *
     * @return Returns the selected action.
     */
    private Action selectRandomAction() {

        final double random = Randomness.getRnd().nextDouble();

        if (Properties.USE_INTENT_ACTIONS() && random < Properties.RELATIVE_INTENT_AMOUNT()) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableIntentActions());
        } else {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableUIActions());
        }
    }

    /**
     * Describes a path from a source to a target node with a given action.
     */
    private static final class Path {

        /**
         * The source node.
         */
        private final IScreenState source;

        /**
         * The action that led from the source to the target node.
         */
        private final Action action;

        /**
         * The target node.
         */
        private final IScreenState target;

        /**
         * Constructs a new path.
         *
         * @param source The source node of the path.
         * @param action The action describing the path.
         * @param target The target node of the path.
         */
        public Path(final IScreenState source, final Action action, final IScreenState target) {
            this.source = requireNonNull(source);
            this.action = requireNonNull(action);
            this.target = requireNonNull(target);
        }

        /**
         * Returns the source node.
         *
         * @return Returns the source node.
         */
        public IScreenState getSource() {
            return source;
        }

        /**
         * Returns the action that describes the path.
         *
         * @return Returns the action that describes the path.
         */
        public Action getAction() {
            return action;
        }

        /**
         * Returns the target node.
         *
         * @return Returns the target node.
         */
        public IScreenState getTarget() {
            return target;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Path that = (Path) o;
            return this.source.equals(that.source)
                    && this.action.equals(that.action)
                    && this.target.equals(that.target);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + action.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }

        /**
         * Provides a short textual representation of the path.
         *
         * @return Returns a short string representation of the path.
         */
        public String toShortString() {
            return String.format("{source=%s, action=%s, target=%s}",
                    source.getId(), action.toShortString(), target.getId());
        }

        /**
         * Provides a textual representation of the path.
         *
         * @return Returns a string representation of the path.
         */
        @Override
        public String toString() {
            return String.format("Path{source=%s, action=%s, target=%s}",
                    source, action, target);
        }
    }

    /**
     * Records stats about the shuffling procedure.
     */
    private static final class ShuffleStat {

        /**
         * The path length of the original path.
         */
        private final int inputLength;

        /**
         * Stores the produced permutation.
         */
        private final List<Integer> outputPermutation;

        private ShuffleStat(final int inputLength, final List<Integer> outputPermutation) {
            MATE.log_debug("New stats: input length: " + inputLength
                    + ", permutation: " + outputPermutation);
            this.inputLength = inputLength;
            this.outputPermutation = requireNonNull(outputPermutation);
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final ShuffleStat that = (ShuffleStat) o;
            return inputLength == that.inputLength && outputPermutation.equals(that.outputPermutation);
        }

        @Override
        public int hashCode() {
            int result = inputLength;
            result = 31 * result + outputPermutation.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format(Locale.getDefault(),
                    "ShuffleStat{inputLength=%d, outputLength=%d, outputPermutation=%s}",
                    inputLength, outputPermutation.size(), outputPermutation);
        }
    }

    /**
     * Provides the logic to shuffle paths.
     */
    private static final class ShufflePath {

        /**
         * The used state equivalence function.
         */
        private final IStateEquivalence stateEquivalence
                = getStateEquivalenceCheck(Properties.STATE_EQUIVALENCE_LEVEL());

        /**
         * Stores which paths have been already used (tried out).
         */
        private final BitSet usedPaths = new BitSet();

        /**
         * The original path (sequence).
         */
        private Path[] originalPath = null;

        /**
         * Stores the final shuffled path (sequence) that is returned.
         */
        private Path[] result = null;

        /**
         * Stores stats about the shuffling procedure.
         */
        private final List<ShuffleStat> stats = new ArrayList<>();

        /**
         * Stores the current permutation, i.e. the path indices corresponding to the result.
         */
        private Integer[] permutation;

        /**
         * Builds an implicit path from the given source node.
         *
         * @param source The source node at which the path should start.
         * @return Returns the maximal path length or {@code -1} the shuffled transition sequence
         *          is the same as the original transition sequence.
         * @throws MaxLenPathException If the maximal path length has been reached, i.e. if the
         *          shuffled transition sequence length is as long as the original transition
         *          sequence and those two sequences are not identical.
         */
        private int buildPath(final IScreenState source) {

            MATE.log_debug("Building path from source state: " + source);

            // Tracks how many paths have been already tried out.
            final int numberOfUsedPaths = usedPaths.cardinality();

            MATE.log_debug("Number of used paths: " + numberOfUsedPaths);

            // Corresponds to the length of the original path.
            final int numberOfPossiblePaths = originalPath.length;

            MATE.log_debug("Original path length: " + numberOfPossiblePaths);

            // Check if we reached the maximal path length.
            if (numberOfUsedPaths == numberOfPossiblePaths) {
                if (Arrays.equals(originalPath, result)) {
                    // Discovered path is identical to original path, use random sequence instead.
                    MATE.log_debug("Path is identical to original path!");
                    return -1;
                } else {
                    // Abort recursion and use discovered path.
                    MATE.log_debug("Discovered distinct path of maximal length!");
                    throw new MaxLenPathException();
                }
            }

            // Find the indices of paths that can be used next, i.e. those that start in the
            // given source node and haven't been used yet.
            final Integer[] availablePaths = IntStream.range(0, numberOfPossiblePaths)
                    .filter(i -> !usedPaths.get(i)
                            && stateEquivalence.checkEquivalence(originalPath[i].getSource(), source))
                    .boxed()
                    .toArray(Integer[]::new);

            MATE.log_debug("Number of available paths: " + availablePaths.length);
            MATE.log_debug("Available paths: " + Arrays.toString(availablePaths));

            if (availablePaths.length == 0) {
                // We have either exhausted all transition possibilities or there are no transitions
                // starting at the given source node.
                return 0;
            }

            // Among all possible paths, we want to choose one uniformly at random.
            Randomness.shuffleArray(availablePaths);

            // Recursively find the longest remaining path and backtrack if necessary.
            int longestPathContinuation = -1;
            int longestPathIndex = -1;

            for (int i = 0; i < availablePaths.length; ++i) {

                // Try out the next possible path combination.
                final int pathIndex = availablePaths[i];
                final Path path = originalPath[pathIndex];
                result[numberOfUsedPaths] = path;
                MATE.log_debug("Current path: " + Arrays.stream(result)
                        .filter(Objects::nonNull)
                        .map(Path::toShortString)
                        .collect(Collectors.joining(" -> ")));

                permutation[numberOfUsedPaths] = pathIndex;

                // Memorize that we have used the given path already.
                usedPaths.set(pathIndex);

                // Recursively find a path starting from the target node.
                final int len = buildPath(path.getTarget());

                // Allow path to be re-used in a different path combination.
                usedPaths.clear(pathIndex);

                // Memorize if we find a solution that consists of a path that is longer than all
                // previous found solutions.
                if (len > longestPathContinuation) {
                    longestPathContinuation = len;
                    longestPathIndex = i;
                }
            }

            if (longestPathIndex >= 0) {
                final Path chosen = originalPath[availablePaths[longestPathIndex]];
                result[numberOfUsedPaths] = chosen;
                MATE.log_debug("Current path: " + Arrays.stream(result)
                        .filter(Objects::nonNull)
                        .map(Path::toShortString)
                        .collect(Collectors.joining(" -> ")));
                return longestPathContinuation + 1;
            } else {
                // identical path than original one
                return 0;
            }
        }

        /**
         * Computes a shuffled path with a maximal possible length starting at the given root node.
         *
         * @param root The root node denoting the start point of the path.
         * @return Returns the longest possible path that could be shuffled together.
         */
        private List<Path> shufflePath(final IScreenState root) {
            List<Path> path;
            try {
                final int size = buildPath(root);
                path = size > 0
                        // Use the discovered path.
                        ? Collections.unmodifiableList(Arrays.asList(result).subList(0, size))
                        // Same path as original path, use a random sequence instead.
                        : Collections.emptyList();
            } catch (final MaxLenPathException ignored) {
                // We have discovered a path of maximal length.
                path = Collections.unmodifiableList(Arrays.asList(result)
                        .subList(0, this.originalPath.length));
                usedPaths.clear();
            }

            this.originalPath = null;
            this.result = null;
            return path;
        }

        /**
         * Returns the shuffle stats.
         *
         * @return Returns the shuffle stats.
         */
        public List<ShuffleStat> getStats() {
            return stats;
        }

        /**
         * Shuffles the given path.
         *
         * @param path The path to be shuffled.
         * @param root The initial node from which the path starts.
         * @return Returns the shuffled path or an empty path if no distinct path could be found.
         */
        public List<Path> shuffle(final List<Path> path, final IScreenState root) {

            this.originalPath = path.toArray(new Path[0]);
            this.result = new Path[path.size()];
            permutation = new Integer[path.size()];
            final List<Path> shuffledPath = shufflePath(root);
            stats.add(new ShuffleStat(path.size(),
                    Collections.unmodifiableList(Arrays.asList(permutation)
                            .subList(0, shuffledPath.size()))));
            permutation = null;
            return shuffledPath;
        }

        /**
         * Indicates that we exhausted the maximal path length.
         */
        private static final class MaxLenPathException extends RuntimeException {
        }
    }
}
