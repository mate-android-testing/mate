package org.mate.crash_reproduction.eda;

import org.mate.crash_reproduction.eda.multivariate.BOA;
import org.mate.crash_reproduction.eda.tree_based.DependencyTree;
import org.mate.crash_reproduction.eda.univariate.CGA;
import org.mate.crash_reproduction.eda.univariate.PIPE;
import org.mate.crash_reproduction.eda.univariate.UMDA;
import org.mate.crash_reproduction.fitness.CrashDistance;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.interaction.action.ui.UIAction;

import java.util.Collections;

public enum DistributionModel {
    PIPE {
        @Override
        public <T extends UIAction> IDistributionModel get() {
            // default learning rate 0.01
            // default epsilon 0.000001
            // default clr 0.1
            // default pEl 0.01
            // default pMutation 0.4
            // default mutationRate 0.4
            return new PIPE(new CrashDistance(), 0.6, 0.01, 0.000001, 0.1, 0.01, 0.4, 0.4);
        }
    },
    CGA {
        @Override
        public <T extends UIAction> IDistributionModel get() {
            return new CGA(new CrashDistance());
        }
    },
    BOA {
        @Override
        public <T extends UIAction> IDistributionModel get() {
            return new BOA(5, Collections.singletonList(new CrashDistance()), new FitnessSelectionFunction<>());
        }
    },
    DEPENDENCY_TREE {
        @Override
        public <T extends UIAction> IDistributionModel get() {
            return new DependencyTree(Collections.singletonList(new CrashDistance()), new FitnessSelectionFunction<>(), 1000, 0.99);
        }
    },
    UMDA {
        @Override
        public <T extends UIAction> IDistributionModel get() {
            return new UMDA(Collections.singletonList(new CrashDistance()), new FitnessSelectionFunction<>());
        }
    };

    public abstract <T extends UIAction> IDistributionModel get();
}
