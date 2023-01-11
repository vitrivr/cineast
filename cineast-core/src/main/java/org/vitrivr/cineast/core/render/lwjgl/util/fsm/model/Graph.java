package org.vitrivr.cineast.core.render.lwjgl.util.fsm.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Graph {

  private final State initialState;
  private final Set<State> goalStates;

  private final Hashtable<Transition, State> transitions;

  public Graph(Hashtable<Transition, State> transitions, State initialState, HashSet<State> goalStates) {
    this(transitions, initialState, goalStates, true);
  }

  public Graph(Hashtable<Transition, State> transitions, State initialState, HashSet<State> goalStates,
      boolean export) {
    this.transitions = transitions;
    this.initialState = initialState;
    this.goalStates = goalStates;
    if (export) {
      this.export();
    }
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

  private void export() {
    var file = new File("fsm.txt");
    try {
      Files.writeString(file.toPath(), this.toString("plantuml"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
