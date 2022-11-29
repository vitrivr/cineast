package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateLeave {

  String state();
  String[] data() default {};
}
