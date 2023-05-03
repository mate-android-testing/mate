package org.mate.model.fsm.sosm.subjective_logic;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public final class MultinomialOpinion implements SubjectiveOpinion<List<Double>, MultinomialOpinion> {

    private final RawMultinomialOpinion opinion;

    public MultinomialOpinion(final RawMultinomialOpinion opinion) {
        this.opinion = opinion;
    }

    public RawMultinomialOpinion getRawOpinion() {
        return opinion;
    }

    public BinomialOpinion coarsenToOpinion(final int index) {
        return new BinomialOpinion(opinion.coarsenToOpinion(index));
    }

    @Override
    public List<Double> getBelief() {
        return Arrays.stream(opinion.getBeliefs()).boxed().collect(toList());
    }

    @Override
    public List<Double> getApriori() {
        return Arrays.stream(opinion.getAprioris()).boxed().collect(toList());
    }

    @Override
    public double getUncertainty() {
        return opinion.getUncertainty();
    }

    @Override
    public MultinomialOpinion multiply(final MultinomialOpinion operand) {
        return new MultinomialOpinion(opinion.multiply(operand.opinion));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        } else {
            final MultinomialOpinion that = (MultinomialOpinion) other;
            return opinion.equals(that.opinion);
        }
    }

    @Override
    public int hashCode() {
        return opinion.hashCode();
    }

    @Override
    public String toString() {
        return String.format("MultinomialOpinion{opinion=%s}", opinion);
    }
}
