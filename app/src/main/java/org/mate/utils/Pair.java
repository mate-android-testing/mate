package org.mate.utils;

import java.util.Objects;

public final class Pair<T, U> {

  public final T first;
  public final U second;

  public Pair(final T first, final U second) {
    this.first = Objects.requireNonNull(first);
    this.second = Objects.requireNonNull(second);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    } else if (o == null || getClass() != o.getClass()) {
      return false;
    } else {
      final Pair<?, ?> other = (Pair<?, ?>) o;
      return first.equals(other.first) && second.equals(other.second);
    }
  }

  @Override
  public int hashCode() {
    return 31 * first.hashCode() + second.hashCode();
  }
}
