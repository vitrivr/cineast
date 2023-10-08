package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Represents a graph that can be used by an FSM to traverse.
 */
public class Graph {

  /**
   * The initial state of the graph.
   */
  private final State initialState;
  /**
   * The set of goal states of the graph.*
   */
  private final Set<State> goalStates;

  /**
   * The transitions of the graph.
   */
  private final Hashtable<Transition, State> transitions;

  /**
   * Creates a new Graph.
   * The setup process can be as follows:
   * The transitions have to describe for each state which action leads to which state.
   * <p>
   * {@code
   * return new Graph(new Hashtable<>() {{
   * {put(new Transition(new State("Startup"), new Action("wait")), new State("Startup"));}
   * {put(new Transition(new State("Startup"), new Action("print")), new State("Print"));}
   * ...
   * }}, new State("Startup"), new HashSet<>() {{
   * {add(new State("Result"));}
   * }});}
   * </p>
   * @param transitions The transitions of the graph as a hashtable.
   * @param initialState The initial state of the graph.
   * @param goalStates The set of goal states of the graph.
   */
  public Graph(Hashtable<Transition, State> transitions, State initialState, HashSet<State> goalStates) {
    this(transitions, initialState, goalStates, false);
  }

  /**
   * @see Graph#Graph(Hashtable, State, HashSet)
   * @param export parameter to disable the export of the graph.
   */
  public Graph(Hashtable<Transition, State> transitions, State initialState, HashSet<State> goalStates,
      boolean export) {
    this.transitions = transitions;
    this.initialState = initialState;
    this.goalStates = goalStates;
    if (export) {
      this.export();
    }
  }

  /**
   * Returns the initial state of the graph.
   *
   * @return The initial state of the graph.
   */
  public State initialState() {
    return this.initialState;
  }

  /**
   * True if graph contains the transition
   * @param transition transition to check
   * @return true if graph contains the transition
   */
  public boolean containsTransition(Transition transition) {
    return this.transitions.containsKey(transition);
  }

  /**
   * Returns the next state based on given transition which is a unique state action pair.
   * @param transition The transition to check.
   * @return The next state for a given transition.
   */
  public State getNextState(Transition transition) {
    return this.transitions.get(transition);
  }

  /**
   * Returns true if the given state is a goal state.
   * @param enteredState The state to check.
   * @return True if the given state is a goal state.
   */
  public boolean isFinalState(State enteredState) {
    return this.goalStates.contains(enteredState);
  }

  /**
   * Generates a graph viz string representation of the graph.
   */
  public String toString(String flavour) {

    var sb = new StringBuilder();
    sb.append("@startuml");
    sb.append("\n");
    sb.append("[*] --> ");
    sb.append(this.initialState);
    sb.append("\n");
    for (var entry : this.transitions.entrySet()) {
      sb.append(entry.getKey().getState().toString());
      sb.append(" --> ");
      sb.append(entry.getValue().toString());
      sb.append(" : ");
      sb.append(entry.getKey().getCommand().toString());
      sb.append("\n");
    }
    for (var entry : this.goalStates) {
      sb.append(entry.toString());
      sb.append(" --> [*]");
      sb.append("\n");
    }
    sb.append("@enduml");

    return sb.toString();
  }

  /**
   * Helper for exports the graph to a file.
   */
  private void export() {
    var file = new File("fsm.txt");
    try {
      Files.writeString(file.toPath(), this.toString("plantuml"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
