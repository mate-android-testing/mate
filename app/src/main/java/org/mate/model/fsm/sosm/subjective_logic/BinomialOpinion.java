package org.mate.model.fsm.sosm.subjective_logic;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A binomial opinion is a opinion on a binary domain. You belief in some event with a certain
 * probability, you disbelief that event with another probability, or your are not sure about it.
 * The following property holds for a binomial opinion: {@code belief + disbelief + uncertainty == 1.0}.
 *
 * This class is a wrapper around a {@link RawBinomialOpinion} that implements the
 * {@link SubjectiveOpinion} interface. This split is done, to prevent unnecessary boxing and
 * unboxing for performance, but still allow to implement a generic interface without breaking static
 * type safety.
 */
public final class BinomialOpinion implements SubjectiveOpinion<Double, BinomialOpinion> {

    /**
     * The raw binomial opinion.
     */
    private final RawBinomialOpinion opinion;

    /**
     * Creates a new binomial opinion from the given raw binomial opinion.
     *
     * @param rawBinomialOpinion The given raw binomial opinion.
     */
    public BinomialOpinion(final RawBinomialOpinion rawBinomialOpinion) {
        this.opinion = rawBinomialOpinion;
    }

    /**
     * Returns the wrapped raw binomial opinion.
     *
     * @return Returns the wrapped raw binomial opinion.
     */
    public RawBinomialOpinion getRawOpinion() {
        return opinion;
    }

    /**
     * Multiplies multiple binomial opinions with this opinion.
     *
     * @param opinions The opinions that should be multiplied with this opinion.
     * @return Returns the resulting binomial opinion after applying the multiplications.
     */
    public static BinomialOpinion multiply(final List<BinomialOpinion> opinions) {
        final List<RawBinomialOpinion> rawBinomialOpinions = opinions.stream()
                .map(BinomialOpinion::getRawOpinion)
                .collect(toList());
        return new BinomialOpinion(RawBinomialOpinion.multiply(rawBinomialOpinions));
    }

    /**
     * Fuses the given binomial opinions into a single binomial opinion.
     *
     * @param opinions The binomial opinions.
     * @return Returns the resulting binomial opinion.
     */
    public static BinomialOpinion multiSourceFusion(final List<BinomialOpinion> opinions) {
        return new BinomialOpinion(
                RawBinomialOpinion.multiSourceFusion(
                        opinions.stream()
                                .map(BinomialOpinion::getRawOpinion)
                                .collect(toList())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getBelief() {
        return opinion.getBelief();
    }

    /**
     * Returns the disbelief associated with the binomial opinion.
     *
     * @return Returns the disbelief associated with the binomial opinion.
     */
    public double getDisbelief() {
        return opinion.getDisbelief();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getApriori() {
        return opinion.getApriori();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getUncertainty() {
        return opinion.getUncertainty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BinomialOpinion multiply(final BinomialOpinion operand) {
        return new BinomialOpinion(opinion.multiply(operand.opinion));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        } else {
            final BinomialOpinion that = (BinomialOpinion) other;
            return opinion.equals(that.opinion);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return opinion.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("BinomialOpinion{opinion=%s}", opinion);
    }
}

