package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller.FiniteStateMachine;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller.FiniteStateMachineException;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;

/**
 * Worker is the abstract class for the worker thread.
 * <b>The Abstract Worker provides:</b>
 * <ul>
 *   <li>Loading the {@link Graph} from concrete worker implementation</li>
 *   <li>Creating a finite state machine {@link FiniteStateMachine}</li>
 *   <li>Waiting on a {@link Job}</li>
 *   <li>Performing a {@link Job}, by perform {@link Action} and walk with the {@link FiniteStateMachine} through the {@link Graph}</li>
 *   <li>On each transition a {@link StateProviderAnnotationParser} to invoke the marked methods
 *   (with {@link StateEnter}, {@link StateLeave}, {@link StateTransition}) from the concrete worker implementation</li>
 *   <li>Handling exceptions {@link StateProviderException} or {@link FiniteStateMachine}</li>
 * <p>
 * This abstract worker has to be extended by a <b>concrete worker</b> implementation.
 * <p>
 * The concrete worker has to implement all methods to do the job
 * If a method should be invoked on a state transition, the method has to be annotated with {@link StateEnter}, {@link StateLeave}, {@link StateTransition}
 * The graph has to describe all legal transitions.
 * @param <T> The type of the job
 */
@StateProvider
public abstract class Worker<T extends Job> implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger();
  private boolean shutdown;
  private final Graph graph;

  private final BlockingDeque<T> jobs;
  protected T currentJob;

  public Worker(BlockingDeque<T> jobs) {
    this.jobs = jobs;
    this.graph = this.createGraph();
  }

  protected abstract Graph createGraph();
  protected abstract String onJobException(Exception ex);

  public void run() {
    // Worker loop. Waiting on job or shutdown.
    this.shutdown = false;
    while (!this.shutdown) {
      try {
        LOGGER.debug("Waiting for job. In Queue:" + this.jobs.size());
        this.currentJob = this.jobs.take();
        LOGGER.trace("Perform Job. In Queue:" + this.jobs.size());
        switch (this.currentJob.getType()) {
          case ORDER -> this.performJob(this.currentJob);
          case CONTROL -> this.shutdown = true;
        }
      } catch (InterruptedException ex) {
        LOGGER.fatal("critical shutdown on renderer", ex);
        this.shutdown = true;
      } finally {
        LOGGER.debug("Worker has performed Job. In Queue:" + this.jobs.size());
      }
    }
  }

  /**
   * Job loop. Perform a single Job
   * 1. Setup the Statemachine with initialized graph
   * 2. Gets the action sequence and the job data for this job
   * 3. Do till final state in graph is reached
   * - Take action
   * - Move to next state
   * - The StateProviderAnnotationParser will call all methods in the ConcreteWorker that were marked with a corresponding Annotation
   * @see StateEnter,
   * @see StateLeave,
   * @see StateTransition
   * @param job Job to be performed.
   */
  private void performJob(Job job) {

    var performed = false;
    var fsm = new FiniteStateMachine(this.graph);

    var actions = job.getActions();
    var data = job.getData();

    while (!performed) {
      try {
        var action = actions.take();
        var leavedState = fsm.getCurrentState();
        var currentTransition = fsm.moveNextState(action);
        var enteredState = fsm.getCurrentState();
        var sap = new StateProviderAnnotationParser();
        sap.runTransitionMethods(this, leavedState, enteredState, currentTransition, data);
        performed = this.graph.isFinalState(enteredState);
      } catch (InterruptedException | FiniteStateMachineException ex) {
        LOGGER.error("Error in job. Abort: ", ex);
        this.onJobException(ex);
        performed = true;
      } catch (InvocationTargetException | IllegalAccessException ex) {
        this.onJobException(ex);
        LOGGER.error("Error in job. Abort: ", ex);
        performed = true;
      }
      finally {
        LOGGER.trace("Job Secuence ended");
      }
    }
    LOGGER.trace("Job ended");
  }

  protected void putActionfirst(Action action) {
    this.currentJob.getActions().addFirst(action);
  }

}
