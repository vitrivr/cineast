package org.vitrivr.cineast.core.render.lwjgl.util.fsm;


/**
 * Example class for concrete actions.
 * This class is used to set the Annotation Action names in concrete worker classes.
 * <p>
 * This is an enum workaround,
 * because enums cannot be provided a string to annotate on runtime.
 * @see ConcreteWorker
 */
@SuppressWarnings("unused")
public final class ConcreteActions {
  public static final String WAIT = "WAIT";
  public static final String PRINT = "PRINT";
  public static final String END = "END";
}
