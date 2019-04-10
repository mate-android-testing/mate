package org.mate.model;

import java.util.List;
import java.util.Vector;

public class Archive {

    private final float threshold = 3;
    private List<TestCase> archivedTCs;

    private Archive(){
        archivedTCs = new Vector<>();
    }

    public void updateArchive(TestCase tc){
        archivedTCs.add(tc);
    }
}
