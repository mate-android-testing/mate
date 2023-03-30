package org.mate.exploration.rl.qlearning.qbe.chromosome_factory;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.rl.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.interaction.action.Action;
import org.mate.model.fsm.qbe.QBEModel;

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
        return explorationStrategy.chooseAction(model.getCurrentState()).get();
    }
}
