package org.mate.crash_reproduction.fitness;

import android.util.Pair;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A debug fitness function for nerd.tuxmobil.fahrplan.congress
 */
public class CongressFitnessFunction implements IFitnessFunction<TestCase> {
    private final static String MAIN_ACTIVITY = "nerd.tuxmobil.fahrplan.congress.MainActivity";
    private final static String SETTINGS_ACTIVITY = "nerd.tuxmobil.fahrplan.congress.SettingsActivity";

    private final static Function<Widget, Widget> GET_ROOT = widget -> {
        while (widget.getParent() != null) {
            widget = widget.getParent();
        }
        return widget;
    };

    private final static Function<Widget, Stream<Widget>> GET_DESENDANTS = new Function<Widget, Stream<Widget>>() {
        @Override
        public Stream<Widget> apply(Widget widget) {
            return getChildren(widget);
        }

        private Stream<Widget> getChildren(Widget widget) {
            return Stream.concat(
                    Stream.of(widget),
                    widget.hasChildren() ? widget.getChildren().stream().flatMap(this::getChildren) : Stream.empty()
                    );
        }
    };

    private final static Predicate<TestCase> CLOSED_DIALOG = t -> t.getStateSequence().stream()
            .anyMatch(s -> s.getActivityName().equals(MAIN_ACTIVITY) && s.getWidgets().stream().noneMatch(w -> w.getResourceID().equals("nerd.tuxmobil.fahrplan.congress:id/alertTitle")));
    private final static Predicate<TestCase> OPENED_MENU = t -> t.getStateSequence().stream()
            .anyMatch(s -> s.getActivityName().equals(MAIN_ACTIVITY)
                    && s.getWidgets().stream().anyMatch(w -> w.getText().equals("Alarms"))
                    && s.getWidgets().stream().anyMatch(w -> w.getText().equals("Schedule changes"))
                    && s.getWidgets().stream().anyMatch(w -> w.getText().equals("Settings")));
    private final static Predicate<TestCase> CHOSE_SETTINGS = t -> t.getVisitedActivities().contains(SETTINGS_ACTIVITY);
    private final static Predicate<Widget> ON_CLICK_ALTERNATIVE_SCHEDULE_URL_DIALOG = w -> w.getResourceID().equals("android:id/alertTitle") && w.getText().equals("Alternative Schedule URL");
    private final static Predicate<TestCase> CLICKED_ALTERNATIVE_SCHEDULE_URL = t -> t.getStateSequence().stream()
            .anyMatch(s -> s.getActivityName().equals(SETTINGS_ACTIVITY) && s.getWidgets().stream().anyMatch(ON_CLICK_ALTERNATIVE_SCHEDULE_URL_DIALOG));
    private final static Predicate<Action> CLICKED_OK_ON_SCHEDULE_URL_ACTION = click -> click instanceof WidgetAction
            && ((WidgetAction) click).getWidget().isButtonType()
            && ((WidgetAction) click).getWidget().getText().equals("OK")
            && GET_DESENDANTS.apply(GET_ROOT.apply(((WidgetAction) click).getWidget())).anyMatch(ON_CLICK_ALTERNATIVE_SCHEDULE_URL_DIALOG);
    private final static Predicate<Action> SAVED_ALTERNATIVE_SCHEDULE_URL_ACTION = a -> a instanceof MotifAction
            && ((MotifAction) a).getUIActions().stream().anyMatch(CLICKED_OK_ON_SCHEDULE_URL_ACTION)
            || CLICKED_OK_ON_SCHEDULE_URL_ACTION.test(a);
    private final static Predicate<TestCase> SAVED_ALTERNATIVE_SCHEDULE_URL = t -> t.getEventSequence().stream().anyMatch(SAVED_ALTERNATIVE_SCHEDULE_URL_ACTION);
    private final static Predicate<TestCase> WENT_BACK_AFTER_SAVING_ALTERNATIVE_SCHEDULE = t -> {
        boolean didAction = false;
        for (int i = 0; i < t.getEventSequence().size() && i + 1 < t.getStateSequence().size(); i++) {
            IScreenState state = t.getStateSequence().get(i + 1);
            if (didAction && state.getActivityName().equals(MAIN_ACTIVITY)) {
                return true;
            } else if (SAVED_ALTERNATIVE_SCHEDULE_URL_ACTION.test(t.getEventSequence().get(i))) {
                didAction = true;
            }
        }

        return false;
    };
    private final static Predicate<TestCase> REPRODUCED_CRASH = t -> t.reachedTarget(Registry.getEnvironmentManager().getStackTrace());

    private final BarrierFitnessFunction<TestCase> barrierFitnessFunction = new BarrierFitnessFunction<>(new LinkedList<Pair<Predicate<TestCase>, String>>(){{
        add(Pair.create(CLOSED_DIALOG, "Closed dialog"));
        add(Pair.create(OPENED_MENU, "Opened menu"));
        add(Pair.create(CHOSE_SETTINGS, "Chose settings"));
        add(Pair.create(CLICKED_ALTERNATIVE_SCHEDULE_URL, "Clicked alternative schedule url"));
        add(Pair.create(SAVED_ALTERNATIVE_SCHEDULE_URL, "Saved alternative schedule url"));
        add(Pair.create(WENT_BACK_AFTER_SAVING_ALTERNATIVE_SCHEDULE, "Went back after saving alternative schedule"));
        add(Pair.create(REPRODUCED_CRASH, "Reproduced crash"));
    }});

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return barrierFitnessFunction.getNormalizedFitness(chromosome);
    }
}
