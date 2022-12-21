package org.mate.crash_reproduction.eda;

import org.mate.Properties;
import org.mate.crash_reproduction.eda.representation.IModelRepresentation;
import org.mate.crash_reproduction.eda.univariate.NoUpdate;
import org.mate.crash_reproduction.eda.univariate.PIPE;
import org.mate.crash_reproduction.fitness.CrashDistance;

public enum DistributionModel {
    PIPE {
        @Override
        public IDistributionModel get(IModelRepresentation modelRepresentation) {
            // default learning rate 0.01
            // default epsilon 0.000001
            // default clr 0.1
            // default pEl 0.01
            // default pMutation 0.4
            // default mutationRate 0.4
            return new PIPE(modelRepresentation, new CrashDistance(),
                    Properties.PIPE_LEARNING_RATE(),
                    Properties.PIPE_NEGATIVE_LEARNING_RATE(),
                    Properties.PIPE_EPSILON(),
                    Properties.PIPE_CLR(),
                    Properties.PIPE_PROB_ELITIST_LEARNING(),
                    Properties.PIPE_PROB_MUTATION(),
                    Properties.PIPE_MUTATION_RATE());
        }
    },
    NO_UPDATE {
        @Override
        public IDistributionModel get(IModelRepresentation modelRepresentation) {
            return new NoUpdate(modelRepresentation);
        }
    };

    public abstract IDistributionModel get(IModelRepresentation modelRepresentation);
}
