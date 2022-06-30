package org.mate.state.equivalence.checks;

import org.mate.commons.interaction.action.ui.Widget;
import org.mate.state.IScreenState;
import org.mate.state.equivalence.IStateEquivalence;
import org.mate.state.equivalence.StateEquivalenceLevel;

import java.util.List;
import java.util.Objects;

/**
 * Compares two {@link IScreenState}s for equality based on the widgets including text and content
 * description. This corresponds to the equivalence level
 * {@link StateEquivalenceLevel#WIDGET_WITH_ATTRIBUTES}.
 */
public class WidgetWithAttributesEquivalence implements IStateEquivalence {

    @Override
    public boolean checkEquivalence(IScreenState first, IScreenState second) {

        Objects.requireNonNull(first, "First screen state must be not null!");
        Objects.requireNonNull(second, "Second screen state must be not null!");

        if (first == second) {
            return true;
        }

        return Objects.equals(first.getPackageName(), second.getPackageName())
                && Objects.equals(first.getActivityName(), second.getActivityName())
                && Objects.equals(first.getWidgets(), second.getWidgets())
                && checkWidgetAttributesEquivalence(first.getWidgets(), second.getWidgets());
    }

    /**
     * Checks for equality between the given two {@link Widget} lists.
     *
     * @param first The widgets of the first state.
     * @param second The widgets of the second state.
     * @return Returns {@code true} if both widget lists are equal, otherwise {@code false} is
     *         returned.
     */
    private boolean checkWidgetAttributesEquivalence(List<Widget> first, List<Widget> second) {

        assert first.size() == second.size();

        for (int i = 0; i < first.size(); i++) {

            Widget thisWidget = first.get(i);
            Widget thatWidget = second.get(i);

            if (!Objects.equals(thisWidget.getContentDesc(), thatWidget.getContentDesc())
                    || !Objects.equals(thisWidget.getText(), thatWidget.getText())) {
                return false;
            }
        }

        return true;
    }
}

