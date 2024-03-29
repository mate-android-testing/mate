package org.mate.model.fsm.sosm.subjective_logic;

import org.mate.utils.MathUtils;

import java.util.List;
import java.util.Objects;

/**
 * This class implements the logic of a binomial opinion using only unboxed values for performance.
 * Class {@link BinomialOpinion} implements a generic interface for subjective opinions, but
 * requires boxing all values.
 */
public final class RawBinomialOpinion {

    /**
     * A small negative epsilon.
     */
    private static final double NEGATIVE_EPS = -MathUtils.EPS;

    /**
     * The belief, disbelief, apriori and uncertainty of the raw binomial opinion.
     */
    private final double belief, disbelief, apriori, uncertainty;

    /**
     * Construct a new binomial opinion.
     *
     * @param belief The belief of the opinion in some event.
     * @param disbelief The disbelief of the opinion in some event.
     * @param uncertainty The degree to which the opinion is not certain about the event.
     * @param apriori A belief in the event, given that no observations have been made yet.
     */
    public RawBinomialOpinion(final double belief, final double disbelief,
                              final double uncertainty, final double apriori) {

        this(belief, disbelief, uncertainty, apriori, true);

        if (!selfValid()) {
            throw new IllegalArgumentException("Invalid Binomial opinion: " + this);
        }
    }

    /**
     * Construct a new binomial opinion with a default apriori of 0.5.
     *
     * @param belief The belief of the opinion in some event.
     * @param disbelief The disbelief of the opinion in some event.
     * @param uncertainty The degree to which the opinion is not certain about the event.
     */
    public RawBinomialOpinion(final double belief, final double disbelief, final double uncertainty) {
        this(belief, disbelief, uncertainty, 0.5);
    }

    /*
     * Private constructor, omits checking.
     */
    private RawBinomialOpinion(final double belief, final double disbelief, final double uncertainty,
                               final double apriori, boolean ignored) {
        this.belief = belief;
        this.disbelief = disbelief;
        this.uncertainty = uncertainty;
        this.apriori = apriori;
    }

    /**
     * Checks whether the specified properties form a valid binomial opinion.
     *
     * @return Returns {@code true} if the binomial opinion is valid, otherwise {@code false}.
     */
    private boolean selfValid() {
        return belief >= NEGATIVE_EPS
                && disbelief >= NEGATIVE_EPS
                && uncertainty >= NEGATIVE_EPS
                && MathUtils.isEpsEq(belief + disbelief + uncertainty, 1.0)
                && apriori >= 0.0
                && apriori <= 1.0;
    }

    /**
     * Represents the Weighted Average Fusion (WAF) operator.
     *
     * @param sources The list of binomial opinions that should be fused.
     */
    public static RawBinomialOpinion multiSourceFusion(final List<RawBinomialOpinion> sources) {

        if (sources.isEmpty()) {
            throw new IllegalArgumentException("Require at least one opinion for fusion.");
        }

        boolean hasNonOneUncertainty = false;
        boolean hasZeroUncertainty = false;
        int zeroUncertaintyOpinionCount = 0;
        double uncertaintySum = 0.0;
        double uncertaintyProduct = 1.0;
        double zeroUncertaintyBeliefSum = 0.0;
        double zeroUncertaintyDisbeliefSum = 0.0;
        double zeroUncertaintyAprioriSum = 0.0;
        double aprioriSum = 0.0;

        for (final RawBinomialOpinion opinion : sources) {

            final double uncertainty = opinion.uncertainty;

            if (MathUtils.isEpsEq(uncertainty)) {
                ++zeroUncertaintyOpinionCount;
                zeroUncertaintyBeliefSum += opinion.belief;
                zeroUncertaintyDisbeliefSum += opinion.disbelief;
                zeroUncertaintyAprioriSum += opinion.apriori;
                hasNonOneUncertainty = true;
                hasZeroUncertainty = true;
                continue;
            }

            if (!MathUtils.isEpsEq(uncertainty, 1.0)) {
                hasNonOneUncertainty = true;
            }

            uncertaintySum += uncertainty;
            uncertaintyProduct *= uncertainty;
            aprioriSum += opinion.apriori;
        }

        if (hasZeroUncertainty) {
            // Case 2: There exists a uncertainty value that is 0.0.
            final double newBelief = zeroUncertaintyBeliefSum
                    / (double) zeroUncertaintyOpinionCount;
            final double newDisbelief = zeroUncertaintyDisbeliefSum
                    / (double) zeroUncertaintyOpinionCount;
            final double newApriori = zeroUncertaintyAprioriSum
                    / (double) zeroUncertaintyOpinionCount;
            final double norm = newBelief + newDisbelief;
            return new RawBinomialOpinion(newBelief / norm, newDisbelief / norm,
                    0.0, newApriori);
        }

        if (!hasNonOneUncertainty) {
            // Case 3: All uncertainty values are 1.0.
            final double newApriori = aprioriSum / (double) sources.size();
            return new RawBinomialOpinion(0.0, 0.0, 1.0, newApriori);
        }

        // Case 1: No uncertainty value is 0.0 and at least one uncertainty value is not 1.0.
        double newBelief = 0.0;
        double newDisbelief = 0.0;
        double newApriori = 0.0;
        double denominator = 0.0;

        for (final RawBinomialOpinion opinion : sources) {
            final double uncertainty = opinion.uncertainty;
            final double negUncertainty = 1.0 - uncertainty;
            final double negUncertaintyProduct = uncertaintyProduct / uncertainty;
            newBelief += opinion.belief * negUncertainty * negUncertaintyProduct;
            newDisbelief += opinion.disbelief * negUncertainty * negUncertaintyProduct;
            newApriori += opinion.apriori * negUncertainty;
            denominator += negUncertaintyProduct;
        }

        final double n = sources.size();
        denominator -= n * uncertaintyProduct;
        newBelief /= denominator;
        newDisbelief /= denominator;
        final double newUncertainty = (n - uncertaintySum) * uncertaintyProduct / denominator;
        newApriori /= n - uncertaintySum;
        return new RawBinomialOpinion(newBelief, newDisbelief, newUncertainty, newApriori);
    }

    /**
     * Returns the belief in the binomial opinion.
     *
     * @return Returns the belief in the binomial opinion.
     */
    public double getBelief() {
        return belief;
    }

    /**
     * Returns the disbelief in the binomial opinion.
     *
     * @return Returns the disbelief in the binomial opinion.
     */
    public double getDisbelief() {
        return disbelief;
    }

    /**
     * Returns the uncertainty in the binomial opinion.
     *
     * @return Returns the uncertainty in the binomial opinion.
     */
    public double getUncertainty() {
        return uncertainty;
    }

    /**
     * Returns the apriori belief in the binomial opinion.
     *
     * @return Returns the apriori belief in the binomial opinion.
     */
    public double getApriori() {
        return apriori;
    }

    /**
     * Multiplies this opinion with another binomial opinion.
     *
     * @param operand The other binomial opinion.
     * @return Returns the resulting binomial opinion after applying multiplication.
     */
    public RawBinomialOpinion multiply(final RawBinomialOpinion operand) {
        return multiply(this, operand);
    }

    /**
     * Multiplies two binomial opinions.
     *
     * @param a The first binomial opinion.
     * @param b The second binomial opinion.
     * @return Returns the resulting binomial opinion.
     */
    public static RawBinomialOpinion multiply(final RawBinomialOpinion a, final RawBinomialOpinion b) {

        final double newApriori = a.apriori * b.apriori;
        final double newDisbelief = a.disbelief + b.disbelief - (a.disbelief * b.disbelief);

        final double newBelief = (a.belief * b.belief)
                + ((((1 - a.apriori) * b.apriori * a.belief * b.uncertainty)
                + (a.apriori * (1 - b.apriori) * a.uncertainty * b.belief))
                / (1 - newApriori));

        final double newUncertainty = (a.uncertainty * b.uncertainty)
                + ((((1 - b.apriori) * a.belief * b.uncertainty)
                + ((1 - a.apriori) * a.uncertainty * b.belief))
                / (1 - newApriori));

        return new RawBinomialOpinion(newBelief, newDisbelief, newUncertainty, newApriori, true);
    }

    /**
     * Multiplies multiple binomial opinions.
     *
     * @param opinions The multinomial opinions that should be multiplied.
     * @return Returns the resulting multinomial opinion.
     */
    public static RawBinomialOpinion multiply(final List<RawBinomialOpinion> opinions) {
        return opinions.stream().reduce((a, b) -> multiply(a, b)).get();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            /*
             * Note: We still require a strict comparison here to uphold the equals()/hashCode()
             * contract. If we only used approximate equality, then opinions that are equal
             * could have different hashcode values.
             */
            final RawBinomialOpinion that = (RawBinomialOpinion) o;
            return belief == that.belief
                    && disbelief == that.disbelief
                    && uncertainty == that.uncertainty
                    && apriori == that.apriori;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(belief, disbelief, uncertainty, apriori);
    }

    @Override
    public String toString() {
        return String.format(
                "RawBinomialOpinion{belief=%s, disbelief=%s,  uncertainty=%s, apriori=%s}",
                belief, disbelief, uncertainty, apriori);
    }
}



