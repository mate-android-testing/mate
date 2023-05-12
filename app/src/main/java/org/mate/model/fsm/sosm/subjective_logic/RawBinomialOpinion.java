package org.mate.model.fsm.sosm.subjective_logic;

import org.mate.utils.MathUtils;

import java.util.List;
import java.util.Objects;

/**
 * This class implements the logic of a binomial opinion using only unboxed values for performance.
 * Class {@link BinomialOpinion} implements a generic interface for Subjective Opinions, but
 * requires boxing all values.
 */
public final class RawBinomialOpinion {

    private static final double NEGATIVE_EPS = -MathUtils.EPS;

    private final double belief, disbelief, apriori, uncertainty;

    /**
     * Construct a new binomial opinion.
     *
     * @param belief The belief of the opinion in some event.
     * @param disbelief The disbelief of the opinion in some event.
     * @param uncertainty The degree to which the opinion is not certain about the event.
     * @param apriori A belief in the event, given that no observations have been made yet.
     * @throws IllegalArgumentException If the values do not form a valid binomial opinion.
     */
    public RawBinomialOpinion(final double belief, final double disbelief, final double uncertainty,
                              final double apriori) {
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
     * @throws IllegalArgumentException If the values do not form a valid binomial opinion.
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

    private boolean selfValid() {
        return belief >= NEGATIVE_EPS
                && disbelief >= NEGATIVE_EPS
                && uncertainty >= NEGATIVE_EPS
                && MathUtils.isEpsEq(belief + disbelief + uncertainty, 1.0)
                && apriori >= 0.0
                && apriori <= 1.0;
    }

    /**
     * Weighted Average Fusion (WAF) operator.
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
            final double zuoc = zeroUncertaintyOpinionCount;
            final double newBelief = zeroUncertaintyBeliefSum / zuoc;
            final double newDisbelief = zeroUncertaintyDisbeliefSum / zuoc;
            final double newApriori = zeroUncertaintyAprioriSum / zuoc;
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
        double den = 0.0;

        for (final RawBinomialOpinion opinion : sources) {
            final double uncertainty = opinion.uncertainty;
            final double negUncertainty = 1.0 - uncertainty;
            final double negUncertaintyProduct = uncertaintyProduct / uncertainty;
            newBelief += opinion.belief * negUncertainty * negUncertaintyProduct;
            newDisbelief += opinion.disbelief * negUncertainty * negUncertaintyProduct;
            newApriori += opinion.apriori * negUncertainty;
            den += negUncertaintyProduct;
        }

        final double n = sources.size();
        den -= n * uncertaintyProduct;
        newBelief /= den;
        newDisbelief /= den;
        final double newUncertainty = (n - uncertaintySum) * uncertaintyProduct / den;
        newApriori /= n - uncertaintySum;
        return new RawBinomialOpinion(newBelief, newDisbelief, newUncertainty, newApriori);
    }

    public double getBelief() {
        return belief;
    }

    public double getDisbelief() {
        return disbelief;
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public double getApriori() {
        return apriori;
    }

    public RawBinomialOpinion multiply(final RawBinomialOpinion sub) {
        return multiply(this, sub);
    }

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



