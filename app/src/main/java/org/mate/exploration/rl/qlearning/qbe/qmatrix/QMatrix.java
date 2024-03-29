package org.mate.exploration.rl.qlearning.qbe.qmatrix;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.interaction.action.ui.UIAction;
import org.mate.model.fsm.qbe.QBEState;

import java.util.Arrays;
import java.util.Objects;

/**
 * Defines a transition prioritization matrix shortly denoted q-matrix.
 */
public final class QMatrix {

    private final AbstractStates abstractStates;
    private final AbstractActions abstractActions;
    private final int numberOfAbstractStates;
    private final int numberOfAbstractActions;
    private final double[] matrix;

    public QMatrix(final AbstractStates abstractStates,
                   final AbstractActions abstractActions) {

        this.abstractStates = Objects.requireNonNull(abstractStates);
        this.abstractActions = Objects.requireNonNull(abstractActions);
        numberOfAbstractStates = abstractStates.getNumberOfAbstractStates();
        numberOfAbstractActions = abstractActions.getNumberOfAbstractActions();

        if (numberOfAbstractStates <= 0 || numberOfAbstractActions <= 0) {
            throw new IllegalArgumentException(
                    "Number of abstract state and number of abstract actions need to be at least 1");
        }

        matrix = new double[numberOfAbstractStates * numberOfAbstractActions];
    }

    public QMatrix(final AbstractStates abstractStates, final AbstractActions abstractActions,
                   final double[] values) {
        this.abstractStates = Objects.requireNonNull(abstractStates);
        this.abstractActions = Objects.requireNonNull(abstractActions);
        numberOfAbstractStates = abstractStates.getNumberOfAbstractStates();
        numberOfAbstractActions = abstractActions.getNumberOfAbstractActions();

        if (numberOfAbstractStates <= 0 || numberOfAbstractActions <= 0) {
            throw new IllegalArgumentException(
                    "Number of abstract state and number of abstract actions need to be at least 1");
        }

        if (abstractStates.getNumberOfAbstractStates() * abstractActions.getNumberOfAbstractActions()
                != values.length) {
            throw new IllegalArgumentException("The number of values does not match the number " +
                    "of abstract states and abstract actions");
        }

        matrix = values.clone();
    }

    public AbstractActions getActionLabelingFunction() {
        return abstractActions;
    }

    private int getIndex(final int stateIndex, final int actionIndex) {
        return stateIndex * numberOfAbstractActions + actionIndex;
    }

    private int getIndex(final QBEState state, final UIAction action) {
        final int stateIndex = abstractStates.getAbstractStateIndex(state);
        if (stateIndex < 0 || stateIndex >= numberOfAbstractStates) {
            throw new IndexOutOfBoundsException("Abstract state index is out of bounds.");
        }

        final int actionIndex = abstractActions.getAbstractActionIndex(action);
        if (actionIndex < 0 || actionIndex >= numberOfAbstractActions) {
            throw new IndexOutOfBoundsException("Abstract action index is out of bounds.");
        }

        return getIndex(stateIndex, actionIndex);
    }

    public double getValue(final QBEState state, final UIAction action) {
        final int index = getIndex(state, action);
        return matrix[index];
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setValue(final QBEState state, final UIAction action, final double value) {
        final int index = getIndex(state, action);
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(
                    "The given double value has to be finite and non-negative.");
        } else {
            matrix[index] = value;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || this.getClass() != o.getClass()) {
            return false;
        } else {
            final QMatrix other = (QMatrix) o;
            return numberOfAbstractStates == other.numberOfAbstractStates
                    && numberOfAbstractActions == other.numberOfAbstractActions
                    && Arrays.equals(matrix, other.matrix);
        }
    }

    @Override
    public int hashCode() {
        return 31 * 31 * numberOfAbstractStates + 31 * numberOfAbstractActions
                + Arrays.hashCode(matrix);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < numberOfAbstractStates; ++i) {
            sb.append("\t[");
            for (int j = 0; j < numberOfAbstractActions; ++j) {
                sb.append(matrix[i * numberOfAbstractActions + j]);
                if (j < numberOfAbstractActions - 1) {
                    sb.append(", ");
                }
            }
            sb.append("],\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    public interface AbstractStates {

        int getAbstractStateIndex(QBEState state);

        int getNumberOfAbstractStates();
    }

    public interface AbstractActions {

        int getAbstractActionIndex(UIAction action);

        int getNumberOfAbstractActions();
    }
}
