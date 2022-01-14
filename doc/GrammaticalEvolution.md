# Grammatical Evolution
The grammatical evolution algorithms build on top of the standard genetic
algorithm. A key difference is that for the grammatical evolution algorithms
the Android test cases are not created and executed when updating the
population (i.e. creating the initial population, doing cross over and
mutation). Instead, the algorithms only produce and alter codon sequences (i.e.
lists of integers) in those phases.

## Genotype Phenotype Mapping and Fitness
The mapping from the codon sequences to Android test cases happens during
fitness evaluation. For this we employ the
GenotypePhenotypeMappedFitnessFunction. It is setup with a method to convert
the codon sequence to an Android test case and a fitness function that can
measure the fitness of the resulting Android test case. The latter is the same
fitness function that would be used for the standard genetic algorithm. The
method to convert the codon sequence to an Android test case depends on which
grammatical evolution approach is chosen. For the standard grammatical
evolution every codon with an odd position (1, 3, 5, ...) determines whether
the Android test case it produces should end or not. Every codon at an even (2,
4, 6, ...) position determines which of the available actions will be
performed. A test case ends either when the first codon at an odd position with
a test case ending codon value is encountered or when an action results in the
app being closed. After converting all chromosomes to test cases they are
evaluated with the given fitness function.

## Cross Over and Mutation
Cross over and mutation functions are performed on the codon sequences.
Currently there is only one implementation of a cross over function: the
IntegerSequencePointCrossOverFunction. For mutation there are two options. The
IntegerSequencePointMutationFunction which randomly changes a single codon of
the sequence. The IntegerSequenceLengthMutationFunction randomly removes or
adds codons to the sequence. This mutation function is used for the grammatical
evolution algorithm that uses the length of the codon sequence as the length of
the resulting Android test case.

## Initial Population
Creating the initial population is very simple as we only need to produce lists
with random integers. This is done by the IntegerSequenceChromosomeFactory.

