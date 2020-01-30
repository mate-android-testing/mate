package org.mate.exploration.ant;

import org.mate.Properties;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;

public class AntColony {
    public void run() {
        //TODO start algorithm
        String targetLine = Properties.TARGET_LINE();
        LineCoveredPercentageFitnessFunction lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);
    }
}
