package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

/**
 *  Represents a State in a Finite State Machine
 *  The State is uniquely identified by its name
 *  The name is used to create a hashcode and to compare states
 */
public class State {

  /**
   * Unique Name of the state
   */
  private final String name;

  /**
   * Creates a new State with a unique name
   *
   * @param name of the state, should be unique
   */
  public State(String name) {
    this.name = name;
  }

  /**
   * Creates a Unique hashcode for current state
   *
   * @return a hashCode for state
   */
  @Override
  public final int hashCode() {
    return 17 + 31 * this.name.hashCode();
  }

  /**
   * Implements the equals method for state
   *
   * @param obj to compare
   * @return true if the states are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if ((!(obj instanceof State other))) {
      return false;
    }
    return this.name.equals(other.name);
  }

  /**
   * Returns the unique name of the state
   *
   * @return name of the state
   */
  @Override
  public String toString() {
    return this.name;
  }
}
