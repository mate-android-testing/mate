package org.mate.exploration.qlearning.qbe.util;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Map;
import java.util.Random;

// Adapted from https://stackoverflow.com/a/20329901
@RequiresApi(api = Build.VERSION_CODES.N)
public final class DistributionRandomNumberGenerator {

  private static final Random random = new Random();

  private DistributionRandomNumberGenerator() {
    throw new IllegalStateException(
            "Class DistributionRandomNumberGenerator cannot be instantiated.");
  }

  public static <T> T getDistributedRandomNumber(final Map<T, Double> distribution) {
    if (distribution.isEmpty()) {
      throw new IllegalArgumentException("Distribution must not be empty.");
    }
    if (distribution.values().stream().anyMatch(d -> !Double.isFinite(d) || d < 0)) {
      throw new IllegalArgumentException(
              "The distribution has to consist of finite non-negative doubles");
    }

    final double distSum = distribution.values().stream().mapToDouble(d -> d).sum();
    if (distSum == 0.0) {
      // All zeros? Then choose uniformly randomly.
      return distribution.keySet().stream().skip(random.nextInt(distribution.size())).findFirst().get();
    }

    final double ratio = 1.0 / distSum;
    final double rand = random.nextDouble();

    double tempDist = 0;
    for (final T t : distribution.keySet()) {
      tempDist += distribution.get(t);
      if (rand / ratio <= tempDist) {
        return t;
      }
    }

    throw new AssertionError("Should never reach here.");
  }

}
