package org.mate.state.equivalence.checks;

import org.junit.Test;
import org.mate.Properties;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.state.IScreenState;
import org.mate.state.equivalence.IStateEquivalence;
import org.mate.state.equivalence.StateEquivalenceFactory;
import org.mate.state.equivalence.StateEquivalenceLevel;
import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.AppScreen;
import org.mockito.MockedStatic;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Provides a couple of simple tests to evaluate the cosine similarity equivalence check between
 * two {@link IScreenState}s.
 */
public class CosineSimilarityTest {

    /**
     * Mocks a widget with the specified attributes.
     *
     * @param clazz The widget's class name.
     * @param depth The depth of the widget in the ui hierarchy.
     * @param text The text of the widget.
     * @param contentDescription The content description of the widget.
     * @return Returns the mocked widget.
     */
    private Widget mockWidget(String clazz, int depth, String text, String contentDescription) {
        Widget widget = mock(Widget.class);
        when(widget.getClazz()).thenReturn(clazz);
        when(widget.getDepth()).thenReturn(depth);
        when(widget.getText()).thenReturn(text);
        when(widget.getContentDesc()).thenReturn(contentDescription);
        return widget;
    }

    /**
     * Mocks an app screen with the given widgets.
     *
     * @param widgets The widgets of the app screen.
     * @return Returns a mocked app screen with the given widgets.
     */
    private AppScreen mockAppScreen(Widget... widgets) {
        AppScreen screenMock = mock(AppScreen.class);
        when(screenMock.getWidgets()).thenReturn(Arrays.asList(widgets));
        return screenMock;
    }

    /**
     * Tests two screen states for a cosine similarity of 1.
     */
    @Test
    public void testFullCosineSimilarity() {

        // create two screen states with the same two buttons
        Widget cancel = mockWidget("Button", 3, "Cancel", "Cancel");
        Widget submit = mockWidget("Button", 3, "Submit", "Submit");

        AppScreen screenMock = mockAppScreen(cancel, submit);
        IScreenState first = new ActionsScreenState(screenMock);
        IScreenState second = new ActionsScreenState(screenMock);

        try (MockedStatic<Properties> propertyMock = mockStatic(Properties.class)) {
            propertyMock.when(Properties::STATE_EQUIVALENCE_LEVEL)
                    .thenReturn(StateEquivalenceLevel.COSINE_SIMILARITY);
            // there can be a minor deviation from 1.0 due to the square root operation
            propertyMock.when(Properties::COSINE_SIMILARITY_THRESHOLD).thenReturn(0.9999f);

            IStateEquivalence cosineSimilarity =
                    StateEquivalenceFactory.getStateEquivalenceCheck(Properties.STATE_EQUIVALENCE_LEVEL());

            assertTrue(cosineSimilarity.checkEquivalence(first, second));
        }
    }

    /**
     * Tests two screen states for a cosine similarity of 0.
     */
    @Test
    public void testZeroCosineSimilarity() {

        Widget cancel = mockWidget("Button", 3, "Cancel", "Cancel");
        Widget submit = mockWidget("Button", 3, "Submit", "Submit");

        AppScreen firstScreenMock = mockAppScreen(cancel);
        AppScreen secondScreenMock = mockAppScreen(submit);
        IScreenState first = new ActionsScreenState(firstScreenMock);
        IScreenState second = new ActionsScreenState(secondScreenMock);

        try (MockedStatic<Properties> propertyMock = mockStatic(Properties.class)) {
            propertyMock.when(Properties::STATE_EQUIVALENCE_LEVEL)
                    .thenReturn(StateEquivalenceLevel.COSINE_SIMILARITY);
            propertyMock.when(Properties::COSINE_SIMILARITY_THRESHOLD).thenReturn(0.0f);

            IStateEquivalence cosineSimilarity =
                    StateEquivalenceFactory.getStateEquivalenceCheck(Properties.STATE_EQUIVALENCE_LEVEL());

            assertTrue(cosineSimilarity.checkEquivalence(first, second));
        }
    }

    /**
     * Tests two screen states for a cosine similarity between 0 and 1.
     */
    @Test
    public void testPartialCosineSimilarity() {

        Widget cancel = mockWidget("Button", 3, "Cancel", "Cancel");
        Widget submit = mockWidget("Button", 3, "Submit", "Submit");

        AppScreen firstScreenMock = mockAppScreen(cancel, submit);
        AppScreen secondScreenMock = mockAppScreen(submit);
        IScreenState first = new ActionsScreenState(firstScreenMock);
        IScreenState second = new ActionsScreenState(secondScreenMock);

        try (MockedStatic<Properties> propertyMock = mockStatic(Properties.class)) {
            propertyMock.when(Properties::STATE_EQUIVALENCE_LEVEL)
                    .thenReturn(StateEquivalenceLevel.COSINE_SIMILARITY);
            propertyMock.when(Properties::COSINE_SIMILARITY_THRESHOLD).thenReturn(0.7f);

            IStateEquivalence cosineSimilarity =
                    StateEquivalenceFactory.getStateEquivalenceCheck(Properties.STATE_EQUIVALENCE_LEVEL());

            assertTrue(cosineSimilarity.checkEquivalence(first, second));
        }
    }
}

