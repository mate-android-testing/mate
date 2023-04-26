package org.mate.exploration.rl.qlearning.qbe.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.rl.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.fsm.qbe.QBEModel;
import org.mate.utils.Randomness;

import java.util.List;

/**
 * The chromosome factory used in QBE.
 */
public class QBEChromosomeFactory extends AndroidRandomChromosomeFactory {

    private final QBEModel model;

    private final ExplorationStrategy explorationStrategy;

    public QBEChromosomeFactory(int maxNumEvents, ExplorationStrategy explorationStrategy) {
        super(true, maxNumEvents);
        assert Properties.QBE_MODEL();
        this.explorationStrategy = explorationStrategy;
        this.model = (QBEModel) uiAbstractionLayer.getGuiModel();
    }

    /**
     * Chooses the action to be executed next depending on the configured exploration strategy.
     *
     * @return Returns the action that is executed next.
     */
    @Override
    protected Action selectAction() {

        final UIAction chosen = explorationStrategy.chooseAction(model.getCurrentState()).get();
        List<UIAction> applicableActions = Registry.getUiAbstractionLayer().getExecutableUIActions();

        if (applicableActions.contains(chosen)) {
            return chosen;
        }

        MATE.log_acc(String.format("Chosen action %s not applicable in current state.", chosen));
        return Randomness.randomElement(applicableActions);
    }
}
