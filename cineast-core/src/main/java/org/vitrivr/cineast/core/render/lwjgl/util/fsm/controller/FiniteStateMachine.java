package org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;

/**
 * Implements a FiniteStateMachine
 *
 * <p>Inspired from Design pattern presented on Stackoverflow
 *
 * @see <a href="https://stackoverflow.com/questions/5923767/simple-state-machine-example-in-c">
 * https://stackoverflow.com/questions/5923767/simple-state-machine-example-in-c/a>
 */
public class FiniteStateMachine {

  Logger LOGGER = LogManager.getLogger(FiniteStateMachine.class);
  /**
   * Hashtable for Unique State transitions
   */
  private final Graph graph;
  /**
   * The current State
   */
  private State currentState;

  /**
   * @return current State of FSM
   */
  public State getCurrentState() {
    return this.currentState;
  }

  /**
   * Constructs a {@link FiniteStateMachine}
   * Sets the initial state
   * @param graph the graph which contains all states and transitions
   *              and the initial state
   */
  public FiniteStateMachine(Graph graph) {
    this.graph = graph;
    this.currentState = graph.initialState();
  }


  /**
   * Gives a preview on next state with a hypothetical command
   *
   * @param command given hypothetical command
   * @return NextState resulting State
   * @throws FiniteStateMachineException if transition is not valid
   */
  public State previewNextState(Action command) throws FiniteStateMachineException {
    Transition transition = new Transition(this.currentState, command);
    if (this.graph.containsTransition(transition)) {
      return this.graph.getNextState(transition);
    } else {
      LOGGER.error("FSM transition to next state failed!");
      throw new FiniteStateMachineException("Invalid transition: " + this.currentState.toString() + " -> " + command.toString());
    }
  }

  /**
   * Moves the FSM to the next state
   *
   * @param command given command
   * @return the resulting state after transition
   * @throws FiniteStateMachineException if transition is not valid
   */
  public Transition moveNextState(Action command) throws FiniteStateMachineException {
    var performedTransition = new Transition(this.currentState, command);
    this.currentState = this.previewNextState(command);
    return performedTransition;
  }
}
