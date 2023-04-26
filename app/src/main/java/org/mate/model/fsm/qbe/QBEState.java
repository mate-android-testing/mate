package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.model.fsm.State;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QBEState extends State {

    /**
     * The number of GUI components on the underlying screen.
     */
    private int numberOfDummyComponents;

    private final boolean isCrashState;

    private final boolean isVirtualRootState;

    QBEState(int id, IScreenState screenState) {
        this(id, screenState, false, false);
    }

    QBEState(final QBEState other) {
        this(other.id, other.screenState, other.isCrashState, other.isVirtualRootState);
    }

    private QBEState(int id, IScreenState screenState, boolean isCrashState, boolean isVirtualRootState) {
        super(id, screenState);
        assert !isCrashState || !isVirtualRootState;
        numberOfDummyComponents = 0;
        this.isCrashState = isCrashState;
        this.isVirtualRootState = isVirtualRootState;
    }

    public static QBEState createCrashState(int id, IScreenState screenState) {
        return new QBEState(id, screenState, true, false);
    }

    public static QBEState createVirtualRootState(int id, IScreenState screenState) {
        return new QBEState(id, screenState, false, true);
    }

    public List<Action> getActions() {
        if (isCrashState) {
            return Collections.emptyList();
        }

        if (isVirtualRootState) {
            return new ArrayList<Action>() {{
                new StartAction();
            }};
        }

        return new ArrayList<>(screenState.getUIActions());
    }

    public void addDummyComponent() {
        ++numberOfDummyComponents;
    }

    public int getWidgetCount() {
        int widgetCount = isCrashState || isVirtualRootState ? 0 : screenState.getWidgets().size();
        return numberOfDummyComponents + widgetCount;
    }
}
