package org.mate.crash_reproduction.eda;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EstimationOfDistribution extends CrashReproduction {
    private final int populationSize;
    private final IDistributionModel model;
    private final LocalDateTime start = LocalDateTime.now();
    private final List<String> targetStackTrace = Registry.getEnvironmentManager().getStackTrace();

    public EstimationOfDistribution(List<IFitnessFunction<TestCase>> fitnessFunctions,
                                    IDistributionModel model,
                                    int populationSize) {
        super(fitnessFunctions);
        this.populationSize = populationSize;
        this.model = model;
    }

    @Override
    protected List<IChromosome<TestCase>> initialPopulation() {
        return Stream.generate(model::createChromosome)
                .limit(populationSize)
                .collect(Collectors.toList());
    }

    @Override
    protected List<IChromosome<TestCase>> evolve(List<IChromosome<TestCase>> prevPopulation) {
        model.update(prevPopulation);
        String fileName = String.format("%s-gen-%d-model.dot", start.format(DateTimeFormatter.ISO_DATE_TIME), currentGenerationNumber);
        Registry.getEnvironmentManager().writeFile(fileName, model.toString());

        List<IChromosome<TestCase>> newPopulation = new LinkedList<>();

        for (int i = 0; i < prevPopulation.size(); i++) {
            IChromosome<TestCase> testCase = model.createChromosome();
            newPopulation.add(testCase);

            if (testCase.getValue().reachedTarget(targetStackTrace)) {
                MATE.log("Early exit because we reached target");
                break;
            }
        }

        return newPopulation;
    }
}
