package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.model.fsm.FSM;
import org.mate.model.fsm.State;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines an Extended Labeled Transition System (ELTS) as described on page 107/108 in the paper
 * "QBE: QLearning-Based Exploration of Android Applications".
 */
public class ELTS extends FSM {

    /**
     * The set of all actions (input alphabet) Z.
     */
    private final Set<Action> actions;

    /**
     * Creates a new ELTS with an initial start state.
     *
     * @param root The start or root state.
     * @param packageName The package name of the AUT.
     */
    public ELTS(State root, String packageName) {
        super(root, packageName);
        actions = new HashSet<>();
    }




}
