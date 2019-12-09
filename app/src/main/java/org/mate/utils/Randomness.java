package org.mate.utils;

import org.mate.Registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Randomness {
    public static Random getRnd() {
        return Registry.getRandom();
    }

    public static <T> T randomElement(List<T> list) {
        return list.get(Registry.getRandom().nextInt(list.size()));
    }

    public static <T> int randomIndex(List<T> list) {
        return Registry.getRandom().nextInt(list.size());
    }

    public static <T> T randomElement(Set<T> set) {
        int index = new Random().nextInt(set.size());
        Iterator<T> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }

        if (iter.hasNext()) {
            return null;
        } else {
            return iter.next();
        }
    }

    public static int getInRangeStd(int range) {
        return getInRangeStd(range, 2.0/15.0 * range);
    }

    public static int getInRangeStd(int range, double std) {
        int x;
        do {
            x = (int) Math.round(Registry.getRandom().nextGaussian() * std + (range - 1) / 2.0);
        } while (x < 0 || x >= range);
        return x;
    }

    public static <T> void shuffleList(List<T> list) {
        List<T> pickList = new ArrayList<>(list);
        list.clear();
        int size = pickList.size();

        for (int i = 0; i < size; i++) {
            int choice = randomIndex(pickList);
            list.add(pickList.get(choice));
            pickList.remove(choice);
        }
    }
}
