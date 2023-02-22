package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation a method can be marked as a state transition method.
 * The method will be called when a specific transition is triggered.
 * (A transition is a state action pair)
 * The method must have the same number of parameters as the data array.
 * The parameters must be in the data container with the same key.
 * {@code  @StateTransition(state = "STATENAME", action = "ACTION", data = {"dataKey1", "dataKey2"} }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateTransition {

  String state();
  String action();
  String[] data() default {};
}
