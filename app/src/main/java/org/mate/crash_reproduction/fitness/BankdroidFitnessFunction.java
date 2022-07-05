package org.mate.crash_reproduction.fitness;

import android.util.Pair;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BankdroidFitnessFunction implements IFitnessFunction<TestCase> {
    private final static Predicate<TestCase> RIGHT_ACTIVITY = t -> t.getVisitedActivities().contains("com.liato.bankdroid.BankEditActivity");
    private final static Predicate<TestCase> RIGHT_STATE = t -> t.getStateSequence().stream().anyMatch(s -> s.getWidgets().stream().anyMatch(w -> w.getClazz().equals("android.widget.TextView") && w.getText().equals("Appeak Poker")));
    private final static Predicate<TestCase> RIGHT_MOTIF_ACTION = t -> t.getEventSequence().stream().anyMatch(a -> a instanceof MotifAction && ((MotifAction) a).getUIActions().stream().anyMatch(ac -> ac instanceof WidgetAction && ((WidgetAction) ac).getWidget().containsAnyToken(Collections.singleton("Save"))));
    private final static Predicate<TestCase> RIGHT_ACTION_SEQUENCE = t -> {
        Optional<Action> rightTextInput = Optional.empty();

        for (Action action : t.getEventSequence()) {
            if (action instanceof WidgetAction) {
                if (((WidgetAction) action).getActionType() == ActionType.TYPE_TEXT) {
                    rightTextInput = Optional.of(action);
                } else if (((WidgetAction) action).getWidget().containsAnyToken(Collections.singleton("save")) && rightTextInput.isPresent()) {
                    return true;
                }
            }

        }
        return false;
    };
    private final static Predicate<TestCase> OPENED_MENU = t -> t.getStateSequence().stream()
            .anyMatch(s -> s.getActivityName().equals("com.liato.bankdroid.MainActivity") && s.getWidgets().stream().anyMatch(w -> w.getText().equals("Add account")));
    private final static Predicate<TestCase> OPENED_BANK_SELECTION = t -> t.getStateSequence().stream()
            .anyMatch(s -> s.getActivityName().equals("com.liato.bankdroid.BankEditActivity") && s.getWidgets().stream().anyMatch(w -> w.getClazz().equals("android.widget.CheckedTextView") && w.getText().equals("Appeak Poker")));

    private final List<String> targetStackTrace = Registry.getEnvironmentManager().getStackTrace();
    private final Predicate<TestCase> REACHED_CRASH = t -> t.reachedTarget(targetStackTrace);

    private final BarrierFitnessFunction<TestCase> barrierFitnessFunction = new BarrierFitnessFunction<>(new LinkedList<Pair<Predicate<TestCase>, String>>() {{
        add(Pair.create(OPENED_MENU, "Opened menu"));
        add(Pair.create(RIGHT_ACTIVITY, "Right activity"));
        add(Pair.create(OPENED_BANK_SELECTION, "Opened bank selection"));
        add(Pair.create(RIGHT_STATE, "Right state"));
        add(Pair.create(RIGHT_STATE.and(RIGHT_MOTIF_ACTION.or(RIGHT_ACTION_SEQUENCE)), "Right actions"));
        add(Pair.create(REACHED_CRASH, "Reached crash"));
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
        return 0.6 * barrierFitnessFunction.getNormalizedFitness(chromosome)
                + 0.2 * getNumOfActionsOnTarget(chromosome)
                + 0.2 * getNumActionsAfterTarget(chromosome);
    }

    private double getNumOfActionsOnTarget(IChromosome<TestCase> chromosome) {
        List<Action> actions = chromosome.getValue().getEventSequence();
        double actionsOnBankEdit = actions.stream().filter(a -> a instanceof UIAction && ((UIAction) a).getActivityName().equals("com.liato.bankdroid.BankEditActivity")).count();
        return (actions.size() - actionsOnBankEdit - 2) / actions.size(); // -2 because we need (at least) two actions to reach activity
    }

    private double getNumActionsAfterTarget(IChromosome<TestCase> chromosome) {
        double sequence = 0;
        boolean reachedTarget = false;

        for (IScreenState state : chromosome.getValue().getStateSequence()) {
            if (state.getActivityName().equals("com.liato.bankdroid.BankEditActivity")) {
                reachedTarget = true;
            } else if (reachedTarget) {
                sequence += 1;
            }
        }

        return reachedTarget ? sequence / chromosome.getValue().getEventSequence().size() : 1;
    }
}
