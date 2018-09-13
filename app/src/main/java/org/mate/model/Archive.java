package org.mate.model;

import java.util.Vector;

public class Archive {

    private final float threshold = 3;
    private Vector<TestCase> archivedTCs;

    private Archive(){
        archivedTCs = new Vector<>();
    }

    public void updateArchive(TestCase tc){
        archivedTCs.add(tc);
    }
}
