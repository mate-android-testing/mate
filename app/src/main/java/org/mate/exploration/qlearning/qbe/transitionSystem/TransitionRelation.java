package org.mate.exploration.qlearning.qbe.transitionSystem;

import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;

import java.util.Objects;

public final class TransitionRelation<S extends State<A>, A extends Action> {

  public final S from;
  public final A trigger;
  public final S to;

  public TransitionRelation(final S from, final A trigger, final S to) {
    this.from = Objects.requireNonNull(from);
    this.trigger = Objects.requireNonNull(trigger);
    this.to = to;
  }

  public TransitionRelation(final TransitionRelation<S, A> ts) {
    this(ts.from, ts.trigger, ts.to);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    } else if (o == null || getClass() != o.getClass()) {
      return false;
    } else {
      final TransitionRelation<?, ?> other = (TransitionRelation<?, ?>) o;
      return from.equals(other.from) && trigger.equals(other.trigger) && Objects.equals(to,
              other.to);
    }
  }

  @Override
  public int hashCode() {
    return 31 * 31 * from.hashCode() + 31 * trigger.hashCode() + (to != null ? to.hashCode() : 0);
  }

  @Override
  public String toString() {
    return "{\"from\":" + from + ",\"trigger\":" + trigger + ",\"to\":" + to + "}";
  }
}
