package org.mate.exploration.accessibility;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

//https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
//https://stackoverflow.com/users/57695/peter-lawrey
//https://github.com/Macil/PSA/blob/master/src/main/java/com/minesnap/psa/RandomCollection.java
public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}