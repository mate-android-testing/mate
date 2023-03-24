package org.mate.exploration.rl.qlearning.qbe.qmatrix;

import org.mate.model.fsm.qbe.QBEState;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation according to
 * Y. Koroglu et al., "QBE: QLearning-Based Exploration of Android Applications,"
 * 2018 IEEE 11th International Conference on Software Testing, Verification and Validation (ICST),
 * 2018, pp. 105-115, doi: 10.1109/ICST.2018.00020.
 */
public final class QBEAbstractState implements QMatrix.AbstractStates {

    private final static List<Integer> steps = Stream.of(1, 3, 8, 15).collect(Collectors.toList());

    @Override
    public int getAbstractStateIndex(final QBEState state) {
        final int numberOfActions = state.getActions().size();
        for (int i = 0; i < steps.size(); ++i) {
            if (numberOfActions <= steps.get(i))
                return i;
        }
        return steps.size();
    }

    @Override
    public int getNumberOfAbstractStates() {
        return 5;
    }
}
