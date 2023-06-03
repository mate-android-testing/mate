package org.mate.exploration.genetic.crossover;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.util.novelty.ChromosomeNoveltyTrace;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.Edge;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;
import org.mate.utils.Tuple;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a essentially a single-point crossover function for {@link TestCase}s produced by the
 * {@link org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory}.
 */
public class SOSMTestCaseMergeCrossOverFunction implements ISOSMCrossOverFunction {

    /**
     * Provides primarily information about the current screen.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * Whether we deal with a test suite execution, i.e. whether the used chromosome factory
     * produces {@link org.mate.model.TestSuite}s or not.
     */
    private boolean isTestSuiteExecution = false;

    /**
     * The maximal number of actions per test case.
     */
    private final int maxNumEvents;

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

    /**
     * Initialises the SOSM-based test case merge crossover function.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SOSMTestCaseMergeCrossOverFunction(int maxNumEvents) {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.maxNumEvents = maxNumEvents;
    }

    /**
     * Defines whether we deal with a test suite execution or not.
     *
     * @param testSuiteExecution Indicates if we deal with a test suite execution or not.
     */
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    @Override
    public List<IChromosome<TestCase>> cross(List<IChromosome<TestCase>> parents) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Performs essentially a single-point crossover on the given parents. A random cut point is
     * chosen in the first chromosome and a merge with the second chromosome at this position is
     * tried out. If not successful (because the screen states don't match at the cut point),
     * another cut point is chosen in alternating fashion from the original point. If all cut points
     * are exhausted (no match found), the first chromosome is returned.
     *
     * @param parents The parents that undergo crossover.
     * @return Returns the generated offspring alongside with its trace.
     */
    @Override
    public List<Tuple<IChromosome<TestCase>, Trace>> crossover(List<ChromosomeNoveltyTrace> parents) {

        /*
        * TODO: Integrate the trace and its uncertainty when choosing a cut point.
        *
        * Right now, the traces and the corresponding uncertainties of the two parent chromosomes are
        * ignored. However, assuming that there are multiple cut points available for performing the
        * single-point crossover, we should favor the offspring that has the highest uncertainty.
        * Since the resulting offsprings may vary largely in size, multiplying uncertainties would
        * favor the shorter offsprings, which is not desired.
         */

        if (parents.size() == 1) {
            MATE.log_warn("SOSMTestCaseMergeCrossOverFunction not applicable on single chromosome!");
            final IChromosome<TestCase> chromosome = parents.get(0).getChromosome();
            final Trace trace = parents.get(0).getTrace();
            return Collections.singletonList(new Tuple<>(chromosome, trace));
        }

        // shorter action sequence comes first
        List<Action> l1 = parents.get(0).getChromosome().getValue().getActionSequence();
        List<Action> l2 = parents.get(1).getChromosome().getValue().getActionSequence();
        if (l2.size() < l1.size()) {
            List<Action> tmp = l1;
            l1 = l2;
            l2 = tmp;
        }

        if (l1.isEmpty()) {
            MATE.log_warn("SOSMTestCaseMergeCrossOverFunction not applicable on empty list!");
            final IChromosome<TestCase> chromosome = parents.get(0).getChromosome();
            final Trace trace = parents.get(0).getTrace();
            return Collections.singletonList(new Tuple<>(chromosome, trace));
        }

        // pick a random cut point in the first chromosome
        int choice = Randomness.getInRangeStd(l1.size());
        boolean right = choice != l1.size() - 1;
        int d = 0;

        // traverse the list from chosen start point to the right and to the left in an alternating pattern
        for (int i = 0; i < l1.size(); i++) {
            int idx = choice + d;

            if (idx >= 0 && idx < l1.size()) {
                for (Edge e1 : Registry.getUiAbstractionLayer().getEdges(l1.get(idx))) {

                    // don't consider actions that result in leaving the app
                    if (e1 != null && e1.getTarget().getPackageName().equals(Registry.getPackageName())) {
                        int cc = l2.size() / 2 + (l1.size() + 1) / 2 - idx;
                        // keep starting index within list bounds
                        cc = Math.min(Math.max(0, cc), l1.size() - 1);
                        Optional<Integer> match = findMatch(l1.get(idx), l2, cc);
                        if (match.hasValue()) {
                            MATE.log_acc("Found match: " + idx + ", " + match.getValue());
                            return Collections.singletonList(
                                    merge(l1.subList(0, idx + 1), l2.subList(match.getValue(), l2.size())));
                        }
                    }

                    if (right) {
                        if (d < 0) {
                            d = -d;
                        }
                        d = d + 1;
                        if (choice - d >= 0) {
                            right = false;
                        }
                    } else {
                        if (d > 0) {
                            d = -d;
                        } else {
                            d -= 1;
                        }
                        if (choice - d + 1 < l1.size()) {
                            right = true;
                        }
                    }
                }
            }
        }
        MATE.log_warn("No match found.");
        final IChromosome<TestCase> chromosome = parents.get(0).getChromosome();
        final Trace trace = parents.get(0).getTrace();
        return Collections.singletonList(new Tuple<>(chromosome, trace));
    }

    private Optional<Integer> findMatch(Action from, List<Action> l, int start) {

        boolean right = start == 0;
        int d = 0;

        // traverse the list from chosen start point to the left and to the right in an alternating pattern
        for (int i = 0; i < l.size(); i++) {
            int idx = start + d;

            if (idx >= 0 && idx < l.size()) {
                for (Edge e1 : sosmModel.getEdges(from)) {
                    for (Edge e2 : sosmModel.getEdges(l.get(idx))) {

                        if (e1 != null && e2 != null
                                && e1.getTarget().equals(e2.getSource())) {
                            return Optional.some(idx);
                        }

                        if (right) {
                            if (d < 0) {
                                d = -d;
                            } else {
                                d = d + 1;
                            }
                            if (start - d - 1 >= 0) {
                                right = false;
                            }
                        } else {
                            if (d > 0) {
                                d = -d;
                            }
                            d -= 1;

                            if (start - d < l.size()) {
                                right = true;
                            }
                        }
                    }
                }
            }
        }
        return Optional.none();
    }

    private Tuple<IChromosome<TestCase>, Trace> merge(List<Action> l1, List<Action> l2) {

        final List<Action> actionSequence = new ArrayList<>(l1);
        actionSequence.addAll(l2);

        uiAbstractionLayer.resetApp();

        // Record the taken transitions of the newly generated offspring.
        sosmModel.resetRecordedTransitions();

        final TestCase offspring = TestCase.newInitializedTestCase();
        final IChromosome<TestCase> chromosome = new Chromosome<>(offspring);

        try {
            for (int i = 0; i < maxNumEvents; i++) {

                Action newAction;

                if (i < actionSequence.size()) { // pick action from generated offspring

                    newAction = actionSequence.get(i);

                    // Check that the ui action is still applicable.
                    if (newAction instanceof UIAction
                            && !uiAbstractionLayer.getExecutableUIActions().contains(newAction)) {
                        MATE.log_warn("SOSMTestCaseMergeCrossOverFunction: Action (" + i + ") "
                                + newAction.toShortString() + " not applicable!");
                        break; // Fill up with random actions.
                    }
                } else {
                    newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
                }

                if (!offspring.updateTestCase(newAction, i)) {
                    MATE.log_warn("SOSMTestCaseMergeCrossOverFunction: Action ( " + i + ") "
                            + newAction.toShortString() + " crashed or left AUT.");
                    return new Tuple<>(chromosome, new Trace(sosmModel.getRecordedTransitions()));
                }
            }

            // Fill up the remaining slots with random actions.
            final int currentTestCaseSize = offspring.getActionSequence().size();

            for (int i = currentTestCaseSize; i < maxNumEvents; ++i) {
                final Action newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
                if (!offspring.updateTestCase(newAction, i)) {
                    MATE.log_warn("SOSMTestCaseMergeCrossOverFunction: Action ( " + i + ") "
                            + newAction.toShortString() + " crashed or left AUT.");
                    return new Tuple<>(chromosome, new Trace(sosmModel.getRecordedTransitions()));
                }
            }
        } finally {

            if (!isTestSuiteExecution) {
                /*
                 * If we deal with a test suite execution, the storing of coverage
                 * and fitness data is handled by the test suite mutation operator itself.
                 */
                FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
                CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
                CoverageUtils.logChromosomeCoverage(chromosome);
            }

            offspring.finish();
            MATE.log_debug("Sequence length after crossover: " + offspring.getActionSequence().size());
        }

        return new Tuple<>(chromosome, new Trace(sosmModel.getRecordedTransitions()));
    }
}
