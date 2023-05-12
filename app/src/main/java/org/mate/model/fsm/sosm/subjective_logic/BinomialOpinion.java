package org.mate.model.fsm.sosm.subjective_logic;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A binomial opion is a opions on a binary domain. You belief in some event with a certain
 * probability, you disblief that event with another probability, or your are not sure about it.
 * Defining property of a Binomial opinion is that {@code belief + disblief + uncertainty == 1.0}.
 *
 * This class is a wrapper around a {@link RawBinomialOpinion} that implements the
 * {@link SubjectiveOpinion} interface. This split is done, to prevent unneccessary boxing and
 * uboxing for performance, but still allow to implement a generic interface without breaking static
 * type safty.
 */
public final class BinomialOpinion implements SubjectiveOpinion<Double, BinomialOpinion> {

    private final RawBinomialOpinion opinion;

    public BinomialOpinion(final RawBinomialOpinion rawBinomialOpinion) {
        this.opinion = rawBinomialOpinion;
    }

    public RawBinomialOpinion getRawOpinion() {
        return opinion;
    }

    public static BinomialOpinion multiply(final List<BinomialOpinion> opinions) {
        final List<RawBinomialOpinion> rawBinomialOpinions = opinions.stream()
                .map(BinomialOpinion::getRawOpinion)
                .collect(toList());
        return new BinomialOpinion(RawBinomialOpinion.multiply(rawBinomialOpinions));
    }

    public static BinomialOpinion multiSourceFusion(final List<BinomialOpinion> opinions) {
        return new BinomialOpinion(
                RawBinomialOpinion.multiSourceFusion(
                        opinions.stream()
                                .map(BinomialOpinion::getRawOpinion)
                                .collect(toList())));
    }

    @Override
    public Double getBelief() {
        return opinion.getBelief();
    }

    public double getDisbelief() {
        return opinion.getDisbelief();
    }

    @Override
    public Double getApriori() {
        return opinion.getApriori();
    }

    @Override
    public double getUncertainty() {
        return opinion.getUncertainty();
    }

    @Override
    public BinomialOpinion multiply(final BinomialOpinion operand) {
        return new BinomialOpinion(opinion.multiply(operand.opinion));
    }

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

    @Override
    public int hashCode() {
        return opinion.hashCode();
    }

    @Override
    public String toString() {
        return String.format("BinomialOpinion{opinion=%s}", opinion);
    }
}

