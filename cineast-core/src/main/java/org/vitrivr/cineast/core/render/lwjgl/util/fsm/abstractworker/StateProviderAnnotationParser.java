package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.lwjgl.system.Configuration;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;

public class StateProviderAnnotationParser {

  protected void runTransitionMethods(Object object, State leavedState, State enteredState,
      Transition currentTransition,
      Variant data) throws InvocationTargetException, IllegalAccessException {
    this.checkIfStateProvider(object);
    var methods = this.getTransitionRelatedMethods(object, leavedState, enteredState, currentTransition);
    for (var method : methods) {
      var params = new ArrayList<Object>();
      var paramNames = getMethodRelatedParams(method);
      for (var name : paramNames) {
        params.add(data.get(Object.class, name));
      }
      method.setAccessible(true);
      try {
        method.invoke(object, params.toArray());
      } catch (IllegalArgumentException ex) {
        throw new StateProviderException("The method " + method.getName() + " has the wrong parameters" + params);
      }
    }
  }

  private void checkIfStateProvider(Object object) throws StateProviderException {
    if (Objects.isNull(object)) {
      throw new StateProviderException("StateProvider is null");
    }
    var clazz = object.getClass();
    if (!clazz.isAnnotationPresent(StateProvider.class)) {
      throw new StateProviderException(
          "The class "
              + clazz.getSimpleName()
              + " is not a state provider"
      );
    }

  }

  private List<Method> getTransitionRelatedMethods(Object object, State leavedState, State enteredState,
      Transition currentTransition) {
    var methods = new LinkedList<Method>();
    var clazz = object.getClass();
    for (var method : clazz.getDeclaredMethods()) {
      if (this.shouldInvokeMethod(method, leavedState, enteredState, currentTransition)) {
        methods.add(method);
      }
    }
    return methods;
  }

  private boolean shouldInvokeMethod(Method method, State leavedState, State enteredState,
      Transition currentTransition) {
    if (method.isAnnotationPresent(StateTransition.class)) {
      var at = method.getAnnotation(StateTransition.class);
      return this.shouldInvokeMethod(at, currentTransition);
    } else if (method.isAnnotationPresent(StateEnter.class)) {
      var at = method.getAnnotation(StateEnter.class);
      return this.shouldInvokeMethod(at, enteredState);
    } else if (method.isAnnotationPresent(StateLeave.class)) {
      var at = method.getAnnotation(StateLeave.class);
      return this.shouldInvokeMethod(at, leavedState);
    }
    return false;
  }

  private boolean shouldInvokeMethod(StateTransition at, Transition currentTransition) {
    return new Transition(new State(at.state()), new Action(at.action())).equals(currentTransition);
  }

  private boolean shouldInvokeMethod(StateEnter at, State enteredState) {
    return new State(at.state()).equals(enteredState);
  }

  private boolean shouldInvokeMethod(StateLeave at, State enteredState) {
    return new State(at.state()).equals(enteredState);
  }

  private List<String> getMethodRelatedParams(Method method) {
    if (method.isAnnotationPresent(StateLeave.class)) {
      var at = method.getAnnotation(StateLeave.class);
      return Arrays.stream(at.data()).toList();
    } else if (method.isAnnotationPresent(StateTransition.class)) {
      var at = method.getAnnotation(StateTransition.class);
      return Arrays.stream(at.data()).toList();
    } else if (method.isAnnotationPresent(StateEnter.class)) {
      var at = method.getAnnotation(StateEnter.class);
      return Arrays.stream(at.data()).toList();
    }
    return null;
  }
}
