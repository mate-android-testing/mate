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

public class QBEChromosomeFactory extends AndroidRandomChromosomeFactory {

    private final QBEModel model;

    private final ExplorationStrategy explorationStrategy;

    public QBEChromosomeFactory(int maxNumEvents, ExplorationStrategy explorationStrategy) {
        super(true, maxNumEvents);
        assert Properties.QBE_MODEL();
        this.explorationStrategy = explorationStrategy;
        this.model = (QBEModel) uiAbstractionLayer.getGuiModel();
    }

    @Override
    protected Action selectAction() {
        UIAction chosen = (UIAction) explorationStrategy.chooseAction(model.getCurrentState()).get();
        List<UIAction> applicableActions = Registry.getUiAbstractionLayer().getExecutableUIActions();
        if (applicableActions.contains(chosen))
            return chosen;

        MATE.log_acc(String.format("Chosen action %s not applicable to current state.", chosen));
        return Randomness.randomElement(applicableActions);
    }
}
