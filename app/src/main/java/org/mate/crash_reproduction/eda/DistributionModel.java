package org.mate.crash_reproduction.eda;

import org.mate.Registry;
import org.mate.interaction.action.ui.UIAction;

public enum DistributionModel {
    CLOSEST_ACTION {
        @Override
        public <T extends UIAction> IDistributionModel<T> get(T root) {
            return new ClosestActionDistributionModel<>(root);
        }
    },
    GUI_MODEL {
        @Override
        public <T extends UIAction> IDistributionModel<T> get(T root) {
            return new GUIModelDistributionModel<>(Registry.getUiAbstractionLayer().getGuiModel());
        }
    },
    QLEARNING {
        @Override
        public <T extends UIAction> IDistributionModel<T> get(T root) {
            return new PMBGNPRLModel<>();
        }
    },
    BEST_ACTION {
        @Override
        public <T extends UIAction> IDistributionModel<T> get(T root) {
            return new BestActionsDistributionModel<>(root);
        }
    };

    public abstract <T extends UIAction> IDistributionModel<T> get(T root);
}
