package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;

/** Input commands for Finite State Machine */
public class Action {

  private  final String name;
  private final Variant data;
  public Action(String name) {
    this.name = name;
    this.data = null;
  }

  public Action(String name, Variant data) {
    this.name = name;
    this.data = data;
  }

  public boolean hasData(){
    return this.data == null;
  }

  public Variant getData(){
    return this.data;
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
    Action other = (Action) obj;
    return other != null
        && this.name.equals(other.name);
  }

  @Override
  public String toString(){
    return this.name;
  }
}
