package org.mate.crash_reproduction.eda.univariate;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.crash_reproduction.eda.IDistributionModel;
import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.crash_reproduction.eda.representation.ModelRepresentationIterator;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.ChromosomeUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

public abstract class RepresentationBasedModel implements IDistributionModel {
    protected final IModelRepresentation modelRepresentation;

    protected RepresentationBasedModel(IModelRepresentation modelRepresentation) {
        this.modelRepresentation = modelRepresentation;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        Registry.getUiAbstractionLayer().resetApp();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            ModelRepresentationIterator iterator = modelRepresentation.getIterator();
            afterChromosomeChanged(chromosome);

            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {
                Action action = Randomness.randomIndexWithProbabilities(iterator.getActionProbabilities());

                if (action instanceof WidgetAction
                        && !Registry.getUiAbstractionLayer().getExecutableActions().contains(action)) {
                    MATE.log_warn("Model suggests action that is not applicable in current state! Ending testcase");
                    return chromosome;
                }

                boolean stop = !testCase.updateTestCase(action, actionsCount);
                afterChromosomeChanged(chromosome);

                IScreenState state = Registry.getUiAbstractionLayer().getLastScreenState();
                iterator.updatePosition(testCase, action, state);

                if (stop) {
                    return chromosome;
                }
            }
        } finally {
            Registry.getEnvironmentManager().storeFitnessData(chromosome,
                    ChromosomeUtils.getActionEntityId(chromosome),
                    Properties.FITNESS_FUNCTION());
            CoverageUtils.storeActionCoverageData(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);
            testCase.finish();
        }
        return chromosome;
    }

    protected void afterChromosomeChanged(Chromosome<TestCase> chromosome) {

    }

    @Override
    public String toString() {
        return modelRepresentation.toString();
    }
}
