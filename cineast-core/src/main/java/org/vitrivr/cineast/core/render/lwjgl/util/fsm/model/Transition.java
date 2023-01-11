package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

/**
 * StateTransition descripes a transition out of a currentState
 *
 * <p>StateTransition is also a unique key for Hashmap in FSM
 */
public class Transition {

  private State currentState;
  private Action command;

  /**
   * Constructor for StateTransition
   *
   * @param currentState
   * @param command
   */
  public Transition(State currentState, Action command) {
    this.currentState = currentState;
    this.command = command;
  }

  /**
   * Creates a Unique hashcode for combination current state and command
   *
   * @return a hashCode for StateTransition
   */
  @Override
  public final int hashCode() {
    return 17 + 31 * this.currentState.hashCode() + 31 * this.command.hashCode();
  }

  /**
   * Implements the equals method for state Transition
   *
   * @param obj to compare
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    Transition other = (Transition) obj;
    return other != null
        && this.currentState.equals(other.currentState)
        && this.command.equals(other.command);
  }

  public State getState() {
    return this.currentState;
  }
  public Action getCommand() {
    return this.command;
  }
}