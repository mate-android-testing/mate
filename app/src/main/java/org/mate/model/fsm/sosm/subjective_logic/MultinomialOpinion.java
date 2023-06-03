package org.mate.model.fsm.sosm.subjective_logic;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A multinomial opinion is a opinion on a domain with two or more possible outcomes b1, b2, ..., bn,
 * where the property {@code b1 + b2 + ... + bn == 1.0} holds.
 *
 * This class is a wrapper around a {@link RawMultinomialOpinion} that implements the
 * {@link SubjectiveOpinion} interface. This split is done, to prevent unnecessary boxing and
 * unboxing for performance, but still allow to implement a generic interface without breaking static
 * type safety.
 */
public final class MultinomialOpinion implements SubjectiveOpinion<List<Double>, MultinomialOpinion> {

    /**
     * The raw multinomial opinion.
     */
    private final RawMultinomialOpinion opinion;

    /**
     * Creates a multinomial opinion from the given raw multinomial opinion.
     *
     * @param opinion The raw multinomial opinion.
     */
    public MultinomialOpinion(final RawMultinomialOpinion opinion) {
        this.opinion = opinion;
    }

    /**
     * Returns the raw multinomial opinion.
     *
     * @return Returns the raw multinomial opinion.
     */
    @SuppressWarnings("unused")
    public RawMultinomialOpinion getRawOpinion() {
        return opinion;
    }

    /**
     * Returns the coarsened binomial opinion.
     *
     * @param index The index of the encoded binomial opinion.
     * @return Returns the coarsened binomial opinion.
     */
    public BinomialOpinion coarsenToOpinion(final int index) {
        return new BinomialOpinion(opinion.coarsenToOpinion(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Double> getBelief() {
        return Arrays.stream(opinion.getBeliefs()).boxed().collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Double> getApriori() {
        return Arrays.stream(opinion.getAprioris()).boxed().collect(toList());
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
    public MultinomialOpinion multiply(final MultinomialOpinion operand) {
        return new MultinomialOpinion(opinion.multiply(operand.opinion));
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
            final MultinomialOpinion that = (MultinomialOpinion) other;
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
        return String.format("MultinomialOpinion{opinion=%s}", opinion);
    }
}
