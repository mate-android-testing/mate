package org.mate.exploration.qlearning.qbe.util;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class CosineSimilarity {

  private CosineSimilarity() {
    throw new IllegalStateException("Class CosineSimilarity cannot be instantiated.");
  }

  public static double dotProduct(final List<Double> vector1, final List<Double> vector2) {
    if (vector1.size() != vector2.size()) {
      throw new IllegalArgumentException("Both vectors need to have the same size.");
    }

    return IntStream.range(0, vector1.size()).mapToDouble(i -> vector1.get(i) * vector2.get(i))
            .sum();
  }

  public static double norm(final List<Double> vector) {
    return sqrt(vector.stream().mapToDouble(x -> x * x).sum());
  }

  public static double cosineSimilarity(final List<Double> vector1, final List<Double> vector2) {
    return dotProduct(vector1, vector2) / (norm(vector1) * norm(vector2));
  }
}
