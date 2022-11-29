package org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller;

public class FiniteStateMachineException extends Exception {

    public FiniteStateMachineException(String message) {
        super(message);
    }

    public FiniteStateMachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public FiniteStateMachineException(Throwable cause) {
        super(cause);
    }
}
