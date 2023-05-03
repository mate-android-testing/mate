package org.mate.exploration.genetic.mutation;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.Tuple;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides a cut point mutation function for {@link TestCase}s.
 */
public class SOSMCutPointMutationFunction implements ISOSMMutationFunction {

    /**
     * Provides primarily information about the current screen.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The maximal number of actions per test case.
     */
    private final int maxNumEvents;

    /**
     * Whether we deal with a test suite execution, i.e. whether the used chromosome factory
     * produces {@link org.mate.model.TestSuite}s or not.
     */
    private boolean isTestSuiteExecution = false;

    private final SOSMModel sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

    /**
     * Initialises the cut point mutation function.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SOSMCutPointMutationFunction(int maxNumEvents) {
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

    // TODO: Adjust documentation.
    /**
     * Performs a cut point mutation. First, the given test case is split at a chosen cut point.
     * Then, the mutated test case is filled with the original actions up to the cut point and
     * from the cut point onwards with random actions.
     *
     * @param chromosome The chromosome to be mutated.
     * @param trace
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome, Trace trace) {

        uiAbstractionLayer.resetApp();

        int cutPoint = chooseCutPoint(trace);

        final TestCase testCase = chromosome.getValue();
        final TestCase mutant = TestCase.newInitializedTestCase();
        final IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                Action newAction;
                if (i < cutPoint) {
                    newAction = testCase.getActionSequence().get(i);
                } else {
                    newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
                }
                if ((newAction instanceof UIAction
                        && !uiAbstractionLayer.getExecutableUIActions().contains(newAction))
                        || !mutant.updateTestCase(newAction, i)) {
                    break;
                }
            }
        } finally {

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel
                        = (SurrogateModel) Registry.getUiAbstractionLayer().getGuiModel();
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
        }

        return mutatedChromosome;
    }

    /**
     * Chooses a random cut point in the action sequence of the given test case.
     *
     * @return Returns the selected cut point.
     */
    private int chooseCutPoint(final Trace trace) {

        final List<Double> uncertainties = sosmModel.getCoarsenedBinomialOpinionsFor(trace).stream()
                .map(BinomialOpinion::getUncertainty)
                .collect(Collectors.toList());

        if (uncertainties.isEmpty()) {
            return 0;
        }

        final List<Tuple<Double, Integer>> candidates = IntStream.range(0, uncertainties.size())
                .mapToObj(i -> new Tuple<>(uncertainties.get(i), i))
                .collect(Collectors.toList());

        Randomness.shuffleList(candidates);
        Collections.sort(candidates, Comparator.comparingDouble(Tuple::getX));

        int sum = (candidates.size() + 1) * candidates.size() / 2; // = 1 + ... + candidates.size()

        final int rnd = Randomness.getRnd().nextInt(sum);
        Tuple<Double, Integer> selected = null;
        int start = 0;
        int rank = 1;
        for (final Tuple<Double, Integer> candidate : candidates) {
            final int end = start + rank;

            if (rnd < end) {
                selected = candidate;
                break;
            } else {
                start = end;
                ++rank;
            }
        }

        assert selected != null;
        return selected.getY();
    }
}

