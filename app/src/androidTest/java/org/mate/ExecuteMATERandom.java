package org.mate;

import org.junit.Test;

/**
 * Created by marceloeler on 21/06/17.
 */

public class ExecuteMATERandom {

    @Test
    public void useAppContext() throws Exception {
        MATE mate = new MATE();
        mate.testApp("UniformRandom");
    }
}
