package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;

/**
 * This class is used to parse the annotations of a state provider
 * @see StateProviderAnnotationParser#runTransitionMethods(Object, State, State, Transition, Variant)
 */
public class StateProviderAnnotationParser {

  /**
   * This method is used to invoke annotated methods of a state provider
   * It invokes all of provided Object methods which are annotated with {@link StateTransition}, {@link StateEnter} or {@link StateLeave}
   * For this purpose the caller has to provide the current state, the state which is leaved and the current transition
   * The sequence of the method invocation is the following:
   * <ol>
   *   <li> Check if the object is a state provider (has the {@link StateProvider} annotation) </li>
   *   <li> Get all methods which are annotated with {@link StateTransition}, {@link StateEnter} or {@link StateLeave} </li>
   *   <li> Invoke the methods with the provided data </li>
 *   </ol>
   *
   * @see StateProvider
   * @see StateTransition
   * @see StateEnter
   * @see StateLeave
   *
   * @param object the instance of the state provider
   * @param leavedState the state which is leaved
   * @param enteredState the state which is entered
   * @param currentTransition the current transition (a state action pair)
   * @param data the data which is passed to the state provider methods
   * @throws InvocationTargetException if the method cannot be invoked
   * @throws IllegalAccessException if the method cannot be accessed
   */
  protected void runTransitionMethods(Object object, State leavedState, State enteredState,
      Transition currentTransition, Variant data) throws InvocationTargetException, IllegalAccessException {
    this.checkIfStateProvider(object);
    var methods = this.getTransitionRelatedMethods(object, leavedState, enteredState, currentTransition);
    for (var method : methods) {
      var params = new ArrayList<>();
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

  /**
   * Checks if the provided object is a state provider
   * @param object the object as instance of worker implementation
   * @throws StateProviderException if the object is not a state provider
   */
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

  /**
   * Returns all methods which are annotated with {@link StateTransition}, {@link StateEnter} or {@link StateLeave}
   * such that the method is related to the provided state and transition.
   * Which means that the method has to be invoked on this state transition
   * @param object the instance of the state provider
   * @param leavedState the state which is leaved
   * @param enteredState the state which is entered
   * @param currentTransition the current transition (a state action pair)
   * @return All methods which have to be invoked on this state transition
   */
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

  /**
   * Helper method which returns true if a given method is related to the provided state and transition
   * Which means that the method has to be invoked on this state transition
   * @param method the method which is checked
   * @param leavedState  the state which is leaved
   * @param enteredState  the state which is entered
   * @param currentTransition the current transition (a state action pair)
   * @return true if the method has to be invoked on this state transition
   */
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

  /**
   * Helper method for StateTransition annotation
   * @param at the annotation of the method
   * @param currentTransition the current transition (a state action pair)
   * @return true if the method has to be invoked on this transition
   */
  private boolean shouldInvokeMethod(StateTransition at, Transition currentTransition) {
    return new Transition(new State(at.state()), new Action(at.action())).equals(currentTransition);
  }

  /**
   * Helper method for StateEnter annotation
   * @param at  the annotation of the method
   * @param enteredState the state which is entered
   * @return true if the method has to be invoked on this state enter
   */
  private boolean shouldInvokeMethod(StateEnter at, State enteredState) {
    return new State(at.state()).equals(enteredState);
  }

  /**
   * Helper method for StateLeave annotation
   * @param at the annotation of the method
   * @param enteredState the state which is entered
   * @return true if the method has to be invoked on this state leave
   */
  private boolean shouldInvokeMethod(StateLeave at, State enteredState) {
    return new State(at.state()).equals(enteredState);
  }

  /**
   * Returns the names of parameters of a method which is annotated with {@link StateTransition}, {@link StateEnter} or {@link StateLeave}
   * @param method the method which is checked
   * @return List of parameter names
   */
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
    throw new StateProviderException("The method " + method.getName() + " is not a state provider method");
  }
}
