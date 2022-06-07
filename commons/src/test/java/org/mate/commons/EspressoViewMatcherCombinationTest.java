package org.mate.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Test;
import org.mate.commons.interaction.action.espresso.matchers_combination.BaseMatcherType;
import org.mate.commons.interaction.action.espresso.matchers_combination.EspressoViewMatcherCombination;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.view_tree.PathStepType;
import org.mate.commons.mock.MockEditText;
import org.mate.commons.mock.MockTextView;
import org.mate.commons.mock.MockViewGroup;

public class EspressoViewMatcherCombinationTest {
    @Test
    public void idMatcher() {
        TextView tv1 = new MockTextView().withId(32).getView();
        TextView tv2 = new MockTextView().withId(42).getView();
        ViewGroup vg = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.ResourceId) &&
                        m.getPath().getSteps().size() == 0));
    }

    @Test
    public void textMatcher() {
        TextView tv1 = new MockTextView().withId(32).withText("Hi").getView();
        TextView tv2 = new MockTextView().withId(32).withText("Bye").getView();
        ViewGroup vg = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.Text) &&
                        m.getPath().getSteps().size() == 0));
    }

    @Test
    public void contentDescriptionMatcher() {
        TextView tv1 = new MockTextView().withId(32).withContentDescription("Hi").getView();
        TextView tv2 = new MockTextView().withId(32).withContentDescription("Bye").getView();
        ViewGroup vg = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.ContentDescription) &&
                        m.getPath().getSteps().size() == 0));
    }

    @Test
    public void classNameMatcher() {
        TextView tv = new MockTextView().withId(32).getView();
        EditText et = new MockEditText().withId(32).getView();
        ViewGroup vg = new MockViewGroup().withChild(tv).withChild(et).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(tv), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.ClassName) &&
                        m.getPath().getSteps().size() == 0));
    }

    @Test
    public void matcherUsingParent() {
        TextView tv1 = new MockTextView().withContentDescription("Hi").getView();
        ViewGroup vg1 = new MockViewGroup().withId(32).withChild(tv1).getView();

        TextView tv2 = new MockTextView().withContentDescription("Hi").getView();
        ViewGroup vg2 = new MockViewGroup().withId(42).withChild(tv2).getView();

        ViewGroup vg3 = new MockViewGroup().withChild(vg1).withChild(vg2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg3);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.ResourceId) &&
                        m.getPath().getSteps().size() == 1 &&
                        m.getPath().getSteps().get(0).getType() == PathStepType.MOVE_TO_PARENT));
    }

    @Test
    public void matcherUsingChild() {
        TextView tv1 = new MockTextView().withId(32).getView();
        ViewGroup vg1 = new MockViewGroup().withChild(tv1).getView();

        TextView tv2 = new MockTextView().withId(42).getView();
        ViewGroup vg2 = new MockViewGroup().withChild(tv2).getView();

        ViewGroup vg3 = new MockViewGroup().withChild(vg1).withChild(vg2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg3);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(vg1), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.ResourceId) &&
                        m.getPath().getSteps().size() == 1 &&
                        m.getPath().getSteps().get(0).getType() == PathStepType.MOVE_TO_CHILD));
    }

    @Test
    public void matcherUsingSibling() {
        TextView tv1 = new MockTextView().withId(32).getView();
        TextView tv2 = new MockTextView().withText("Hi").getView();
        ViewGroup vg1 = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        TextView tv3 = new MockTextView().withId(32).getView();
        TextView tv4 = new MockTextView().withText("Bye").getView();
        ViewGroup vg2 = new MockViewGroup().withChild(tv3).withChild(tv4).getView();

        ViewGroup vg3 = new MockViewGroup().withChild(vg1).withChild(vg2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg3);

        EspressoViewMatcherCombination matcherCombination =
                EspressoViewMatcherCombination.buildUniqueCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        assertTrue(matcherCombination.getMatchers().stream().anyMatch(m ->
                m.getType().equals(BaseMatcherType.Text) &&
                        m.getPath().getSteps().size() == 2 &&
                        m.getPath().getSteps().get(0).getType() == PathStepType.MOVE_TO_PARENT &&
                        m.getPath().getSteps().get(1).getType() == PathStepType.MOVE_TO_CHILD));
    }
}