package org.mate.model;

import java.util.ArrayList;
import java.util.List;

public class Archive {

    private final float threshold = 3;
    private List<TestCase> archivedTCs;

    private Archive(){
        archivedTCs = new ArrayList<>();
    }

    public void updateArchive(TestCase tc){
        archivedTCs.add(tc);
    }
}
