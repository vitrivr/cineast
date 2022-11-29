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
 * <p>Implemented from Design pattern released on Stackoverflow
 *
 * @see <a href="https://stackoverflow.com/questions/5923767/simple-state-machine-example-in-c">
 *     https://stackoverflow.com/questions/5923767/simple-state-machine-example-in-c/a>
 */
public class FiniteStateMachine {

  Logger LOGGER = LogManager.getLogger(FiniteStateMachine.class);
  // Hashtable for Unique State transitions
  private Graph graph;
  // The current State
  private State currentState;

  /** @return current State of FSM */
  public State getCurrentState() {
    return this.currentState;
  }

  /**
   * Constructs a FSM
   *
   * <p>Constructor defines initialState creates Transitios-Hashmap @<code>
   * new StateTransition(ProcessState.Startup, Command.Begin), ProcessState.EvaluateGameNumber
   * </code> StateTransition(ProcessState.Startup, Command.Begin), descripes the command given in
   * the actual state ProcessState.EvaluateGameNumber, descripes the nect State after transition
   */
  public FiniteStateMachine(Graph graph) {
    this.graph = graph;
    this.currentState = graph.initialState();
  }


  /**
   * Gives a preview on next state with comman
   *
   * @param command given
   * @return NextState
   * @throws Exception
   */
  public State previewNextState(Action command) throws FiniteStateMachineException {
    Transition transition = new Transition(this.currentState, command);
    if (this.graph.containsTransition(transition)) {
      State nextState = this.graph.getNextState(transition);
      return nextState;
    } else {
      LOGGER.error("FSM transition to next state failed!");
      throw new FiniteStateMachineException("Invalid transition: " + this.currentState.toString() + " -> " + command.toString());
    }
  }

  /**
   * Change to next State
   *
   * @param command given
   * @return new Current state
   * @throws Exception
   */
  public Transition moveNextState(Action command) throws FiniteStateMachineException {
    var performedTransition = new Transition(this.currentState, command);
    this.currentState = this.previewNextState(command);
    return performedTransition;
  }
}
