package org.vitrivr.cineast.core.render.lwjgl.util.fsm;

/**
 * Example class for concrete states.
 * This class is used to set the Annotation State names in concrete worker classes.
 * <p>
 * This is an enum workaround,
 * because enums cannot be provided a string to annotate on runtime.
 * @see ConcreteWorker
 */
@SuppressWarnings("unused")
public final class ConcreteStates {

  public static final String STARTUP = "STARTUP";
  public static final String PRINT = "PRINT";
  public static final String RESULT = "RESULT";
}
