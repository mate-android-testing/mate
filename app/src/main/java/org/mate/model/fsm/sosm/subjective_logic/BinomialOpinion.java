package org.mate.model.fsm.sosm.subjective_logic;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class BinomialOpinion implements SubjectiveOpinion<Double, BinomialOpinion> {

    public final static BinomialOpinion DISBELIEF
            = new BinomialOpinion(new RawBinomialOpinion(0.0, 1.0, 0.0, 0.0));

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

