package org.mate.model.fsm.sosm.subjective_logic;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A binomial opion is a opions on a domain with two or more possible outcomes b1, b2, ..., bn.
 * Defining property is {@code b1 + b2 + ... + bn == 1.0}.
 *
 * This class is a wrapper around a {@link RawMultinomialOpinion} that implements the
 * {@link SubjectiveOpinion} interface. This split is done, to prevent unneccessary boxing and
 * uboxing for performance, but still allow to implement a generic interface without breaking static
 * type safty.
 */
public final class MultinomialOpinion implements SubjectiveOpinion<List<Double>, MultinomialOpinion> {

    private final RawMultinomialOpinion opinion;

    public MultinomialOpinion(final RawMultinomialOpinion opinion) {
        this.opinion = opinion;
    }

    @SuppressWarnings("unused")
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
