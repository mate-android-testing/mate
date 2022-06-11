package org.mate.exploration.genetic.crossover;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Optional;
import org.mate.commons.utils.Randomness;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.Edge;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a crossover function for {@link TestCase}s produced by the
 * {@link org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory}.
 */
public class TestCaseMergeCrossOverFunction implements ICrossOverFunction<TestCase> {

    private static final double MAX_LENGTH_DEVIATION = 0.25;

    /**
     * Whether to execute the actions directly or not.
     */
    private boolean executeActions;

    /**
     * Initialises a crossover function that is used for test cases consisting of
     * {@link UIAction}, or more precisely
     * {@link WidgetAction}.
     */
    public TestCaseMergeCrossOverFunction() {
        executeActions = true;
    }

    /**
     * Sets whether the actions should be directly or not.
     *
     * @param executeActions Whether the actions should be directly executed.
     */
    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }

    /**
     * Performs a crossover on the given parents.
     *
     * @param parents The parents that undergo crossover.
     * @return Returns the generated offsprings.
     */
    @Override
    public List<IChromosome<TestCase>> cross(List<IChromosome<TestCase>> parents) {

        if (parents.size() == 1) {
            MATELog.log_warn("TestCaseMergeCrossoverFunction not applicable on single chromosome!");
            return Collections.singletonList(parents.get(0));
        }

        List<Action> l1 = parents.get(0).getValue().getActionSequence();
        List<Action> l2 = parents.get(1).getValue().getActionSequence();
        if (l2.size() < l1.size()) {
            List<Action> tmp = l1;
            l1 = l2;
            l2 = tmp;
        }

        // randomly select whether final length should be floored or ceiled
        int lengthBias = Randomness.getRnd().nextInt(2);
        int finalSize = (l1.size() + l2.size() + lengthBias) / 2;

        if (l1.isEmpty()) {
            MATELog.log_warn("TestCaseMergeCrossoverFunction not applicable on empty list!");
            return Collections.singletonList(parents.get(0));
        }

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
                            MATELog.log_acc("Found match: " + idx + ", " + match.getValue());
                            return Collections.singletonList(
                                    merge(l1.subList(0, idx + 1), l2.subList(match.getValue(), l2.size()), finalSize));
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
        MATELog.log_warn("No match found.");
        return Collections.singletonList(parents.get(0));
    }

    private Optional<Integer> findMatch(Action from, List<Action> l, int start) {
        boolean right = start == 0;
        int d = 0;

        // traverse the list from chosen start point to the left and to the right in an alternating pattern
        for (int i = 0; i < l.size(); i++) {
            int idx = start + d;

            if (idx >= 0 && idx < l.size()) {
                for (Edge e1 : Registry.getUiAbstractionLayer().getEdges(from)) {
                    for (Edge e2 : Registry.getUiAbstractionLayer().getEdges(l.get(idx))) {

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

    private IChromosome<TestCase> merge(List<Action> l1, List<Action> l2, int finalSize) {
        List<Action> all = new ArrayList<>(l1);
        all.addAll(l2);

        int lowerChangeBound = (int) Math.floor(finalSize * (1 - MAX_LENGTH_DEVIATION));
        int upperChangeBound = (int) Math.ceil(finalSize * (1 + MAX_LENGTH_DEVIATION));

        // keep final size within max deviation
        finalSize = Math.min(Math.max(all.size(), lowerChangeBound), upperChangeBound);

        TestCase testCase = TestCase.newDummy();
        testCase.setDesiredSize(Optional.some(finalSize));
        testCase.getActionSequence().addAll(all);

        if (executeActions) {
            TestCase executedTestCase = TestCase.fromDummy(testCase);
            Chromosome<TestCase> chromosome = new Chromosome<>(executedTestCase);

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel
                        = (SurrogateModel) Registry.getUiAbstractionLayer().getGuiModel();
                surrogateModel.updateTestCase(executedTestCase);
            }

            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);

            executedTestCase.finish();

            return chromosome;
        }

        return new Chromosome<>(testCase);
    }
}
