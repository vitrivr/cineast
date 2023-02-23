package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

/**
 * StateTransition describes a transition out of a currentState with a command
 * Therefor it consists of a state action pair
 * The state action pair creates a unique hashcode
 */
@SuppressWarnings("ClassCanBeRecord")
public class Transition {

  /**
   * (current / outgoing) state of the transition
   */
  private final State state;
  /**
   * command which triggers the transition from state
   */
  private final Action command;

  /**
   * Constructor for StateTransition
   *
   * @param state (current / outgoing) state of the transition
   * @param command command which triggers the transition from state
   */
  public Transition(State state, Action command) {
    this.state = state;
    this.command = command;
  }

  /**
   * Creates a Unique hashcode for combination current state and command
   *
   * @return a hashCode for StateTransition
   */
  @Override
  public final int hashCode() {
    return 17 + 31 * this.state.hashCode() + 31 * this.command.hashCode();
  }

  /**
   * Implements the equals method for state Transition
   *
   * @param obj to compare
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Transition other)) {
      return false;
    }
    return this.state.equals(other.state) && this.command.equals(other.command);
  }

  @Override
  public String toString() {
    return "Transition{" + "state=" + state + ", command=" + command + '}';
  }

  /**
   * Returns the (current / outgoing) state of the transition
   *
   * @return current state
   */
  public State getState() {
    return this.state;
  }
  /**
   * Returns the command of the transition
   *
   * @return command
   */
  public Action getCommand() {
    return this.command;
  }
}