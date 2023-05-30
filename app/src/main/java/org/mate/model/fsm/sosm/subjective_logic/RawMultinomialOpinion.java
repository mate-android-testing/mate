package org.mate.model.fsm.sosm.subjective_logic;

import org.mate.utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * This class implements the logic of a multinomial opinion using only unboxed values for
 * performance. Class {@link MultinomialOpinion} implements a generic interface for Subjective
 * Opinions, but requires boxing all values.
 */
public final class RawMultinomialOpinion {

    /**
     * A small negative epsilon.
     */
    private static final double NEGATIVE_EPS = -MathUtils.EPS;

    /**
     * The beliefs and apriori beliefs of the multinomial opinion.
     */
    private final double[] beliefs, aprioris;

    /**
     * The uncertainty of the multinomial opinion.
     */
    private final double uncertainty;

    /**
     * Constructs a new multinomial opinion.
     *
     * @param beliefs The belief in the different outcomes.
     * @param uncertainty The degree to which the opinion is not sure about how likely each outcome is.
     * @param aprioris The prior belief in each outcome when no observations have been made yet.
     */
    public RawMultinomialOpinion(final double[] beliefs, final double uncertainty,
                                 final double[] aprioris) {

        this.beliefs = beliefs;
        this.aprioris = aprioris;
        this.uncertainty = uncertainty;

        if (!verifySelf()) {
            throw new IllegalArgumentException("Invalid Multinomial opinion: " + this);
        }
    }

    /**
     * Constructs a new multinomial opinion where the uncertainty is inferred.
     *
     * @param beliefs The belief in the different outcomes.
     * @param aprioris The prior belief in each outcome when no observations have been made yet.
     */
    public RawMultinomialOpinion(final double[] beliefs, final double[] aprioris) {
        this(beliefs, 1.0 - sum(beliefs), aprioris);
    }

    /**
     * Creates a multinomial opinion with uninformative apriori beliefs.
     */
    public RawMultinomialOpinion(final double[] beliefs, final double uncertainty) {
        this(beliefs, uncertainty, uniformApriori(beliefs.length));
    }

    /**
     * Creates a multinomial opinion with uninformative apriori beliefs.
     */
    public RawMultinomialOpinion(final double[] beliefs) {
        this(beliefs, uniformApriori(beliefs.length));
    }

    /**
     * Checks whether the specified properties form a valid multinomial opinion.
     *
     * @return Returns {@code true} if the multinomial opinion is valid, otherwise {@code false}.
     */
    private boolean verifySelf() {
        return beliefs.length == aprioris.length
                && uncertainty >= NEGATIVE_EPS
                && Arrays.stream(beliefs).allMatch(b -> b >= NEGATIVE_EPS)
                && Arrays.stream(aprioris).allMatch(a -> a >= NEGATIVE_EPS)
                && MathUtils.isEpsEq(sum(beliefs) + uncertainty, 1.0)
                && MathUtils.isEpsEq(sum(aprioris), 1.0);
    }

    /**
     * Computes the sum over the given values.
     *
     * @param values The values that should be summed up.
     * @return Returns the sum over the given values.
     */
    private static double sum(final double[] values) {
        double sum = 0.0;
        for (final double aDouble : values) {
            sum += aDouble;
        }
        return sum;
    }

    /**
     * Constructs uniform apriori beliefs.
     *
     * @param size The number of apriori beliefs.
     * @return Returns the uniform apriori beliefs.
     */
    private static double[] uniformApriori(final int size) {
        final double[] aprioris = new double[size];
        Arrays.fill(aprioris, 1.0 / (double) size);
        return aprioris;
    }

    /**
     * Computes the coarsened binomial opinion.
     *
     * @param index The index of the binomial opinion encoded in the multinomial opinion.
     * @return Returns the coarsened binomial opinion.
     */
    public RawBinomialOpinion coarsenToOpinion(final int index) {
        final double targetBelief = beliefs[index];
        final double targetDisbelief = 1.0 - uncertainty - targetBelief;
        final double apriori = aprioris[index];
        return new RawBinomialOpinion(targetBelief, targetDisbelief, uncertainty, apriori);
    }

    /**
     * Returns the beliefs in the multinomial opinion.
     *
     * @return Returns the beliefs in the multinomial opinion.
     */
    public double[] getBeliefs() {
        return beliefs;
    }

    /**
     * Returns the apriori beliefs in the multinomial opinion.
     *
     * @return Returns the apriori beliefs in the multinomial opinion.
     */
    public double[] getAprioris() {
        return aprioris;
    }

    /**
     * Returns the uncertainty in the multinomial opinion.
     *
     * @return Returns the uncertainty in the multinomial opinion.
     */
    public double getUncertainty() {
        return uncertainty;
    }

    // TODO: Perform a code review.
    private double uXY(double bx, double ux, double ax, double by, double uy, double ay, double bxyS) {
        return (((((bx + ax * ux) * (by + ay * uy)) - bxyS) / (ax * ay)));
    }

    /**
     * Multiplies this opinion with another multinomial opinion.
     *
     * @param operand The other multinomial opinion.
     * @return Returns the resulting multinomial opinion.
     */
    public RawMultinomialOpinion multiply(final RawMultinomialOpinion operand) {

        // TODO: Perform a code review, see: https://shorturl.at/anqS1.

        // TODO: This method can certainly be optimized a lot further.
        final int thisSize = size();
        final int otherSize = operand.size();
        final int resultSize = thisSize * otherSize;

        final double[] thisBelief = beliefs;
        final double[] otherBelief = operand.beliefs;

        final List<double[]> singletonBeliefs = new ArrayList<>(thisSize);

        for (int i = 0; i < thisSize; i++) {
            final double currentBelief = thisBelief[i];
            final double[] sbeliefs = new double[otherSize];
            for (int j = 0; j < otherSize; j++) {
                sbeliefs[j] = currentBelief * otherBelief[j];
            }
            singletonBeliefs.add(sbeliefs);
        }

        final double[] brows = new double[thisSize];

        for (int i = 0; i < thisSize; i++) {
            brows[i] = thisBelief[i] * operand.uncertainty;
        }

        final double[] bcols = new double[otherSize];

        for (int i = 0; i < otherSize; i++) {
            bcols[i] = otherBelief[i] * uncertainty;
        }

        final double[] aprioriB = new double[otherSize];
        System.arraycopy(operand.aprioris, 0, aprioriB, 0, otherSize);

        final double[] aprioriBeliefs = new double[resultSize];

        for (int i = 0; i < thisSize; i++) {
            final double currentApriori = aprioris[i];
            for (int j = 0; j < otherSize; j++) {
                aprioriBeliefs[i * otherSize + j] = currentApriori * aprioriB[j];
            }
        }

        final double uRows = sum(brows);
        final double uCols = sum(bcols);
        final double uDomain = uncertainty * operand.uncertainty;

        final double maxU = uRows + uCols + uDomain;
        final double minU = uDomain;

        double minUxys = maxU;

        for (int i = 0; i < thisSize; i++) {
            final double bx = thisBelief[i];
            double ax = aprioris[i];
            final double[] singletons = singletonBeliefs.get(i);
            for (int j = 0; j < otherSize; j++) {
                final double by = operand.beliefs[i];
                final double ay = operand.aprioris[i];
                final double bxys = singletons[j];
                final double uxy = uXY(bx, uncertainty, ax, by, operand.uncertainty, ay, bxys);
                if (uxy < minUxys & uxy >= minU & uxy <= maxU) {
                    minUxys = uxy;
                }
            }
        }

        final double[] productBeliefs = new double[resultSize];

        for (int i = 0; i < thisSize; i++) {
            final double bx = thisBelief[i];
            final double ax = aprioris[i];
            for (int j = 0; j < otherSize; j++) {
                final double by = otherBelief[j];
                final double ay = aprioriB[j];
                final double bxy = (bx + ax * uncertainty) * (by + ay * operand.uncertainty)
                        - ax * ay * minUxys;
                productBeliefs[i * otherSize + j] = bxy;
            }
        }

        return new RawMultinomialOpinion(productBeliefs, aprioriBeliefs);
    }

    /**
     * Fuses multiple multinomial opinions.
     *
     * @param opinions The multinomial opinions that should be fused.
     * @return Returns the resulting multinomial opinion.
     */
    @SuppressWarnings("unused")
    public static RawMultinomialOpinion averagingFusion(
            final Collection<RawMultinomialOpinion> opinions) {

        // TODO: Perform a code review, see https://shorturl.at/uwABE.

        // TODO: This method can certainly be optimized a lot further.
        final List<RawMultinomialOpinion> opList = new ArrayList<>(opinions);
        final boolean nonZeroUncertainty = opList.stream().allMatch(op -> op.uncertainty != 0.0);
        final int size = opList.get(0).size();

        final double[] fusedBeliefs = new double[size];
        if (nonZeroUncertainty) {
            for (int i = 0; i < size; i++) {
                double numeratorSum = 0.0;
                for (int j = 0; j < opList.size(); j++) {
                    final RawMultinomialOpinion op = opList.get(j);
                    final double uncertaintyProduct = opList.stream()
                            .mapToDouble(opinion -> opinion.uncertainty)
                            .reduce(1.0, (a, b) -> a * b)
                            / opList.get(j).uncertainty;
                    numeratorSum += uncertaintyProduct * op.beliefs[i];
                }

                final double denominatorSum = getUncertaintyProduct(opList);
                fusedBeliefs[i] = numeratorSum / denominatorSum;
            }
        } else {
            final double weight = 1.0 / opList.size();
            for (int i = 0; i < size; i++) {
                final int finalI = i;
                final double beliefSum = opList.stream()
                        .mapToDouble(op -> op.beliefs[finalI]).sum() * weight;
                fusedBeliefs[i] = beliefSum;
            }
        }

        return new RawMultinomialOpinion(fusedBeliefs);
    }

    /**
     * Computes the product over the uncertainties of the given multinomial opinions.
     *
     * @param multinomialOpinions The multinomial opinions.
     * @return Returns the uncertainty product over the given multinomial opinions.
     */
    private static double getUncertaintyProduct(final List<RawMultinomialOpinion> multinomialOpinions) {
        final double product = multinomialOpinions.stream()
                .mapToDouble(opinion -> opinion.uncertainty)
                .reduce(1.0, (a, b) -> a * b);

        return multinomialOpinions.stream()
                .mapToDouble(multinomialOpinion -> product / multinomialOpinion.uncertainty)
                .sum();
    }

    /**
     * Returns the number of beliefs.
     *
     * @return Returns the number of beliefs.
     */
    public int size() {
        return beliefs.length;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        } else {
            /*
             * Note: We still require a strict comparison here to uphold the equals()/hashCode()
             * contract. If we only used approximate equality, then opinions that are equal
             * could have different hashcode values.
             */
            final RawMultinomialOpinion that = (RawMultinomialOpinion) other;
            return uncertainty == that.uncertainty
                    && Arrays.equals(beliefs, that.beliefs)
                    && Arrays.equals(aprioris, that.aprioris);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(beliefs), uncertainty, Arrays.hashCode(aprioris));
    }

    @Override
    public String toString() {
        return String.format("RawMultinomialOpinion{belief=%s, uncertainty=%s, apriori=%s}",
                Arrays.toString(beliefs), uncertainty, Arrays.toString(aprioris));
    }
}

