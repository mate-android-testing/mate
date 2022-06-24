package org.mate.crash_reproduction;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CrashReproduction implements Algorithm {
    private final List<String> targetStackTrace = Registry.getEnvironmentManager().getStackTrace();
    private final List<IFitnessFunction<TestCase>> fitnessFunctions;
    protected int currentGenerationNumber = 0;

    protected CrashReproduction(List<IFitnessFunction<TestCase>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    @Override
    public void run() {
        List<IChromosome<TestCase>> population = initialPopulation();
        logPopulationFitness(population);

        currentGenerationNumber++;

        while (!reachedEnd(population)) {
            population = evolve(population);
            logPopulationFitness(population);

            currentGenerationNumber++;
        }
    }

    protected abstract List<IChromosome<TestCase>> initialPopulation();

    protected abstract List<IChromosome<TestCase>> evolve(List<IChromosome<TestCase>> prevPopulation);

    private boolean reachedEnd(List<IChromosome<TestCase>> population) {
        for (IChromosome<TestCase> testCaseIChromosome : population) {
            if (testCaseIChromosome.getValue().reachedTarget(targetStackTrace)) {
                MATE.log("Was able to reproduce crash with " + testCaseIChromosome.getValue().getId() + "!");
                Registry.getEnvironmentManager().setChromosome(testCaseIChromosome);
                MATE.log("Actions necessary: [" + testCaseIChromosome.getValue().getEventSequence().stream().map(Action::toString).collect(Collectors.joining(", ")) + "]");

                for (int i = 0; i < testCaseIChromosome.getValue().getEventSequence().size(); i++) {
                    MATE.log("  " + i + ". " + testCaseIChromosome.getValue().getEventSequence().get(i).toString());
                }
                return true;
            }
        }

        return false;
    }


    private void logPopulationFitness(List<IChromosome<TestCase>> population) {
        if (population.size() <= 10) {
            MATE.log_acc("Fitness of generation #" + (currentGenerationNumber + 1) + " :");
            for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
                MATE.log_acc("Fitness function " + (i + 1) + ":");
                IFitnessFunction<TestCase> fitnessFunction = fitnessFunctions.get(i);
                for (int j = 0; j < population.size(); j++) {
                    IChromosome<TestCase> chromosome = population.get(j);
                    MATE.log_acc("Chromosome " + (j + 1) + ": "
                            + fitnessFunction.getFitness(chromosome));
                }
            }
            if (fitnessFunctions.size() > 5) {
                MATE.log_acc("Omitted other fitness function because there are too many ("
                        + fitnessFunctions.size() + ")");
            }
        }

        population.stream().mapToDouble(fitnessFunctions.get(0)::getFitness).average()
                .ifPresent(popFitness -> MATE.log("Fitness of generation #" + (currentGenerationNumber + 1) + " : " + popFitness));
    }

    public static boolean reachedTarget(Set<String> targets, IScreenState state) {
        return targets.contains(state.getActivityName())
                || state.getFragmentNames().stream().anyMatch(fragment -> targets.stream().anyMatch(target -> target.contains(fragment)));
    }
}
