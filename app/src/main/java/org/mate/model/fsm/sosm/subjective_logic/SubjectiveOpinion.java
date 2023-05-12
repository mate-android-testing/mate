package org.mate.model.fsm.sosm.subjective_logic;

/**
 * A subjetive Opinion is either a {@link BinomialOpinion} or a {@link MultinomialOpinion}.
 * This interface provides an abstraction on the concrete kind of subjective opnion.
 *
 * Depending on the kind of opion that implements the interface, {@code getBelief()} and
 * {@code getApriori()} either return a single scalar value, or a list of values. Using a Self type,
 * (also known as CRTP) allows to model this flexibility without breaking the Java type system.
 *
 * @param <T> THe return type of {@code getBelief()} and {@code getApriori()}
 * @param <B> The concrete subtype that implements this interface.
 */
public interface SubjectiveOpinion<T, B extends SubjectiveOpinion<T, B>> {

    T getBelief();

    T getApriori();

    double getUncertainty();

    B multiply(B operand);

    @Override
    boolean equals(Object other);

    @Override
    int hashCode();

    @Override
    String toString();
}
