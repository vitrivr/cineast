package org.vitrivr.cineast.core.render.lwjgl.util.fsm;

/**
 * Example class for concrete data.
 * This class is used to set the Annotation Data names in concrete worker classes.
 * <p>
 * This is an enum workaround,
 * because enums cannot be provided a string to annotate on runtime.
 * @see ConcreteWorker
 */
@SuppressWarnings("unused")
public final class ConcreteData {

  public static final String MODEL1 = "MODEL1";
  public static final String MODEL2 = "MODEL2";
  public static final String MODEL3 = "MODEL3";

}
