package org.mate.exploration.qlearning.qbe.transitionSystem;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class TransitionSystem<S extends State<A>, A extends Action> {

  private final S initialState;
  private final Set<S> states = new HashSet<>();
  private final Set<A> actions = new HashSet<>();
  private final Set<TransitionRelation<S, A>> transitions = new HashSet<>();

  public TransitionSystem(final S initialState) {
    this.initialState = Objects.requireNonNull(initialState);
    states.add(initialState);
  }

  public S getInitialState() {
    return initialState;
  }

  public boolean addTransition(final TransitionRelation<S, A> transitionRelation) {
    states.add(transitionRelation.from);
    actions.add(transitionRelation.trigger);
    if (transitionRelation.to != null) {
      states.add(transitionRelation.to);
    }

    final boolean nonDeterministicTransitionRelation = transitions.stream().anyMatch(
            t -> t.from.equals(transitionRelation.from) && t.trigger.equals(transitionRelation.trigger)
                    && !Objects.equals(t.to, transitionRelation.to));
    transitions.add(transitionRelation);
    return nonDeterministicTransitionRelation;
  }

  public void removeTransition(final TransitionRelation<S, A> transitionRelation) {
    transitions.remove(transitionRelation);
  }

  public void addActions(final Collection<A> actions) {
    this.actions.addAll(actions);
  }

  public Set<A> nextActions(final S state) {
    return transitions.stream().filter(transitions -> transitions.from.equals(state))
            .map(transition -> transition.trigger).collect(Collectors.toSet());
  }

  public Set<TransitionRelation<S, A>> nextStates(final S state, final A action) {
    return transitions.stream()
            .filter(transition -> transition.from.equals(state) && transition.trigger.equals(action))
            .collect(Collectors.toSet());
  }

  public void removeUnreachableStates() {
    // Todo: This can probably be implemented more efficiently.
    final Set<TransitionRelation<S, A>> reachableTransitions = new HashSet<>(transitions.size());
    final Set<S> reachableStates = new HashSet<>(states.size());
    reachableStates.add(initialState);

    boolean change;
    do {
      final Set<TransitionRelation<S, A>> newTransitions = transitions.stream()
              .filter(transitions -> reachableStates.contains(transitions.from))
              .collect(Collectors.toSet());
      final Set<S> newStates = newTransitions.stream().map(transition -> transition.to)
              .collect(Collectors.toSet());
      change = reachableTransitions.addAll(newTransitions);
      change = reachableStates.addAll(newStates) || change; // Beware of short-circuited logic.
    } while (change);

    states.retainAll(reachableStates);
    transitions.retainAll(reachableTransitions);
  }

  public boolean isDeterministic() {
    for (final TransitionRelation<S, A> transition1 : transitions) {
      for (final TransitionRelation<S, A> transition2 : transitions) {
        if (transition1.from.equals(transition2.from) && transition1.trigger.equals(
                transition2.trigger) && !Objects.equals(transition1.to, transition2.to)) {
          return false;
        }
      }
    }

    return true;
  }

}
