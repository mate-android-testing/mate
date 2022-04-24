package org.mate.exploration.eda;

import org.mate.interaction.action.ui.UIAction;

public enum DistributionModel {
    CLOSEST_ACTION {
        @Override
        public <T extends UIAction> IDistributionModel<T> get(T root) {
            return new ClosestActionDistributionModel<>(root);
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
