package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

/** Input commands for Finite State Machine */
public class State {

  private String name;
  public State(String name) {
    this.name = name;
  }

  /**
   * Creates a Unique hashcode for combination current state and command
   *
   * @return a hashCode for StateTransition
   */
  @Override
  public final int hashCode() {
    return 17 + 31 * this.name.hashCode();
  }

  /**
   * Implements the equals method for state Transition
   *
   * @param obj to compare
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    State other = (State) obj;
    return other != null
        && this.name.equals(other.name);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
