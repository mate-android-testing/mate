package org.mate.exploration.genetic.fitness;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;

import java.util.HashMap;
import java.util.Map;

public class BranchDistanceFitnessFunction implements IFitnessFunction<TestCase> {

    public static final String FITNESS_FUNCTION_ID = "branch_distance_fitness_function";

    private static final Map<IChromosome<TestCase>, Double> cache = new HashMap<>();

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        MATE.log("Retrieving Branch Distance for testcase: " + chromosome.getValue().getId());
        if (cache.containsKey(chromosome)) {
            double branchDistance = cache.get(chromosome);
            MATE.log("Branch Distance (cached): " + branchDistance);
            return branchDistance;
        } else {
            retrieveFitnessValues(chromosome);
            // should be cached now
            return cache.get(chromosome);
        }
    }

    /**
     * Computes the fitness values for a given test case. As a side effect, an
     * intent is sent to the injected broadcast receiver of the AUT, which
     * stores the collected traces in the app-internal storage. Also updates
     * the visited branches, in case branch coverage information is needed.
     *
     * @param chromosome The test case for which we want to evaluate its fitness values.
     */
    public static void retrieveFitnessValues(IChromosome<TestCase> chromosome) {

        if (cache.containsKey(chromosome)) {
            double branchDistance = cache.get(chromosome);
            MATE.log("Branch Distance: " + branchDistance);
        } else {

            /*
            * API level 23 and higher requires that permissions are also granted
            * at runtime, i.e. it is not sufficient to specify them only in
            * the AndroidManifest file. However, a reset/restart of the app
            * causes the loss of the granted runtime permissions. Thus, we
            * need to grant those permissions after each reset/restart.
             */
            EnvironmentManager.grantRuntimePermissions(MATE.packageName);

            Intent intent = new Intent("STORE_TRACES");
            Bundle bundle = new Bundle();
            bundle.putString("packageName", MATE.packageName);
            intent.setComponent(new ComponentName(MATE.packageName,
                   "de.uni_passau.fim.auermich.branchdistance.tracer.Tracer"));
            intent.putExtras(bundle);

            MATE.log("Sending Broadcast to AUT " + MATE.packageName + " in order to store collected traces!");
            InstrumentationRegistry.getContext().sendBroadcast(intent);
            // InstrumentationRegistry.getTargetContext().sendBroadcast(intent);

            double branchDistance = EnvironmentManager.getBranchDistance(chromosome);
            cache.put(chromosome, branchDistance);
            MATE.log("Branch Distance: " + branchDistance);

            // we can end execution if we covered the target vertex
            if (branchDistance == 1.0) {
                ConditionalTerminationCondition.satisfiedCondition();
            }
        }
    }
}
