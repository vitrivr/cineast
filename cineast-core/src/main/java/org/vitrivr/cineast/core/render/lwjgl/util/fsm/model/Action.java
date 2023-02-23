package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;

/**
 *  Represents an Action in a Finite State Machine
 *  The Action is uniquely identified by its name
 *  The name is used to create a hashcode and to compare actions
 *  The Action can contain data
 *  The data provides in an action is only visible to the state transition which were triggered by the action
 */
public class Action {

  /**
   * Unique Name of the action
   */
  private  final String name;

  /**
   * Data of the action
   */
  private final Variant data;

  /**
   * Creates a new Action with a unique name
   *
   * @param name of the action, should be unique
   */
  public Action(String name) {
    this.name = name;
    this.data = null;
  }

  /**
   * Creates a new Action with a unique name and additional data
   *
   * @param name of the action, should be unique
   * @param data of the action
   */
  public Action(String name, Variant data) {
    this.name = name;
    this.data = data;
  }

  /**
   * @return true if the action has data
   */
  @SuppressWarnings("unused")
  public boolean hasData(){
    return this.data != null;
  }

  /**
   * @return the data of the action
   */
  public Variant getData(){
    return this.data;
  }

  /**
   * Creates a Unique hashcode for command
   *
   * @return a hashCode for Command
   */
  @Override
  public final int hashCode() {
    return 17 + 31 * this.name.hashCode();
  }

  /**
   * Implements the equals method for state Transition
   *
   * @param obj to compare
   * @return true if the states are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Action other)) {
      return false;
    }
    return this.name.equals(other.name);
  }

  /**
   * Returns the unique name of the action
   *
   * @return name of the action
   */
  @Override
  public String toString(){
    return this.name;
  }
}
