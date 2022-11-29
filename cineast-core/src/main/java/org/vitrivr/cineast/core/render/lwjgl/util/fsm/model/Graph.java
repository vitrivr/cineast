package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Graph {

  private final State initialState;
  private final Set<State> goalStates;

  private final Hashtable<Transition, State> transitions;

  public Graph(Hashtable<Transition, State> transitions, State initialState, HashSet<State> goalStates) {
    this.transitions = transitions;
    this.initialState = initialState;
    this.goalStates = goalStates;
  }

  public State initialState() {
    return this.initialState;
  }

  public boolean containsTransition(Transition transition) {
    return this.transitions.containsKey(transition);
  }

  public State getNextState(Transition transition) {
    return this.transitions.get(transition);
  }

  public boolean isFinalState(State enteredState) {
    return this.goalStates.contains(enteredState);
  }
}
