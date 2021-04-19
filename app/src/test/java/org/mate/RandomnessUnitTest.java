package org.mate;

import org.junit.Test;
import org.mate.utils.Randomness;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RandomnessUnitTest {
    
    @Test
    public void shuffleTest() throws Exception {
        List<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);
        a.add(4);
        a.add(5);
        List<Integer> b = new ArrayList<>(a);

        Randomness.shuffleList(b);

        assertEquals(a.size(), b.size());

        for (Integer integer : b) {
            assertTrue(a.contains(integer));
            a.remove(integer);
        }
    }
}
