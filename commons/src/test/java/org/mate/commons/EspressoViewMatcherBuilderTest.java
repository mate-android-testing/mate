package org.mate.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Test;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.AllOfMatcher;
import org.mate.commons.interaction.action.espresso.matchers_combination.RelativeMatcherCombination;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.mock.MockEditText;
import org.mate.commons.mock.MockTextView;
import org.mate.commons.mock.MockViewGroup;

public class EspressoViewMatcherBuilderTest {
    @Test
    public void idMatcher() {
        TextView tv1 = new MockTextView().withId(32).getView();
        TextView tv2 = new MockTextView().withId(42).getView();
        ViewGroup vg = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertEquals("withId(32)", espressoViewMatcher.getCode());
    }

    @Test
    public void textMatcher() {
        TextView tv1 = new MockTextView().withId(32).withText("Hi").getView();
        TextView tv2 = new MockTextView().withId(32).withText("Bye").getView();
        ViewGroup vg = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertEquals("withText(\"Hi\")", espressoViewMatcher.getCode());
    }

    @Test
    public void contentDescriptionMatcher() {
        TextView tv1 = new MockTextView().withId(32).withContentDescription("Hi").getView();
        TextView tv2 = new MockTextView().withId(32).withContentDescription("Bye").getView();
        ViewGroup vg = new MockViewGroup().withChild(tv1).withChild(tv2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertEquals("withContentDescription(\"Hi\")", espressoViewMatcher.getCode());
    }

    @Test
    public void classNameMatcher() {
        TextView tv = new MockTextView().withId(32).getView();
        EditText et = new MockEditText().withId(32).getView();
        ViewGroup vg = new MockViewGroup().withChild(tv).withChild(et).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(tv), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertTrue(espressoViewMatcher.getCode().startsWith("withClassName(equalTo(\"android.widget.TextView"));
    }

    @Test
    public void matcherUsingParent() {
        TextView tv1 = new MockTextView().withContentDescription("Hi").getView();
        ViewGroup vg1 = new MockViewGroup().withId(32).withChild(tv1).getView();

        TextView tv2 = new MockTextView().withContentDescription("Hi").getView();
        ViewGroup vg2 = new MockViewGroup().withId(42).withChild(tv2).getView();

        ViewGroup vg3 = new MockViewGroup().withChild(vg1).withChild(vg2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg3, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertEquals("withParent(withId(32))", espressoViewMatcher.getCode());
    }

    @Test
    public void matcherUsingChild() {
        TextView tv1 = new MockTextView().withId(32).getView();
        ViewGroup vg1 = new MockViewGroup().withChild(tv1).getView();

        TextView tv2 = new MockTextView().withId(42).getView();
        ViewGroup vg2 = new MockViewGroup().withChild(tv2).getView();

        ViewGroup vg3 = new MockViewGroup().withChild(vg1).withChild(vg2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg3, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(vg1), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertEquals("withChild(withId(32))", espressoViewMatcher.getCode());
    }

    @Test
    public void matcherUsingSibling() {
        TextView tv1 = new MockTextView().withTag("TV1").withId(32).withText("Item").getView();
        TextView tv2 = new MockTextView().withTag("TV2").withId(42).withText("Hi").getView();
        ViewGroup vg1 = new MockViewGroup().withTag("VG1").withChild(tv1).withChild(tv2).getView();

        TextView tv3 = new MockTextView().withTag("TV3").withId(32).withText("Item").getView();
        TextView tv4 = new MockTextView().withTag("TV4").withId(42).withText("Bye").getView();
        ViewGroup vg2 = new MockViewGroup().withTag("VG1").withChild(tv3).withChild(tv4).getView();

        ViewGroup vg3 = new MockViewGroup().withTag("VG3").withChild(vg1).withChild(vg2).getView();

        EspressoViewTree viewTree = new EspressoViewTree(vg3, "com.activity.name");

        RelativeMatcherCombination matcherCombination =
                RelativeMatcherCombination.buildUnequivocalCombination(
                        viewTree.findNodeForView(tv1), viewTree);

        assertNotNull(matcherCombination);

        EspressoViewMatcher espressoViewMatcher =
                matcherCombination.getMinimalCombination().getEspressoViewMatcher();

        assertTrue(espressoViewMatcher instanceof AllOfMatcher);
        assertEquals(2, ((AllOfMatcher) espressoViewMatcher).getMatchers().size());
        assertTrue(((AllOfMatcher) espressoViewMatcher).getMatchers().stream().anyMatch(
                (m) -> "withParent(withChild(withText(\"Hi\")))".equals(m.getCode())
        ));
        assertTrue(((AllOfMatcher) espressoViewMatcher).getMatchers().stream().anyMatch(
                (m) -> "withText(\"Item\")".equals(m.getCode())
        ));
    }
}