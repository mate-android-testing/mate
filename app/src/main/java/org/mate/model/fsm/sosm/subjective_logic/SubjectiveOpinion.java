package org.mate.model.fsm.sosm.subjective_logic;

/**
 * A subjective Opinion is either a {@link BinomialOpinion} or a {@link MultinomialOpinion}.
 * This interface provides an abstraction on the concrete kind of subjective opinion.
 *
 * Depending on the kind of opinion that implements the interface, {@code getBelief()} and
 * {@code getApriori()} either return a single scalar value, or a list of values. Using a self type,
 * (also known as CRTP) allows to model this flexibility without breaking the Java type system.
 *
 * @param <T> The return type of {@code getBelief()} and {@code getApriori()}.
 * @param <B> The concrete subtype that implements this interface.
 */
public interface SubjectiveOpinion<T, B extends SubjectiveOpinion<T, B>> {

    /**
     * Returns the belief associated with the subjective opinion.
     *
     * @return Returns the belief in the subjective opinion.
     */
    T getBelief();

    /**
     * Returns the apriori associated with the subjective opinion.
     *
     * @return Returns the apriori in the subjective opinion.
     */
    T getApriori();

    /**
     * Returns the uncertainty associated with the subjective opinion.
     *
     * @return Returns the uncertainty of th subjective opinion.
     */
    double getUncertainty();

    /**
     * Multiplies this opinion with the given opinion.
     *
     * @param operand The opinion with which should be multiplied.
     * @return Returns the resulting opinion after applying multiplication.
     */
    B multiply(B operand);

    /**
     * Checks for equality between this and another subjective opinion.
     *
     * @param other The other subjective opinion.
     * @return Returns {@code true} if the two subjective opinions are equal, otherwise {@code false}.
     */
    @Override
    boolean equals(Object other);

    /**
     * Computes the hash code for the subjective opinion.
     *
     * @return Returns the computed hash code for the subjective opinion.
     */
    @Override
    int hashCode();

    /**
     * Provides a textual representation of the subjective opinion.
     *
     * @return Returns the textual representation of the subjective opinion.
     */
    @Override
    String toString();
}
