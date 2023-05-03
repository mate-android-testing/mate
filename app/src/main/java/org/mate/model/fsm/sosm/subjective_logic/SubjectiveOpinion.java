package org.mate.model.fsm.sosm.subjective_logic;

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
