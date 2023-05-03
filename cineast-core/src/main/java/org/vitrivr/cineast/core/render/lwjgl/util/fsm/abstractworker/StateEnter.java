package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation a method can be marked as a state enter method.
 * The method will be called when the state is entered.
 * The method must have the same number of parameters as the data array.
 * The parameters must be in the data container with the same key.
 * {@code  @StateEnter(state = "STATENAME", data = {"dataKey1", "dataKey2"} }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateEnter {

  String state();
  String[] data() default {};
}
