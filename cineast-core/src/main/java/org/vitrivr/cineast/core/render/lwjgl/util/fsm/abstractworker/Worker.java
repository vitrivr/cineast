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
 * <h3>This Abstract Worker:</h3>
 * Worker is the abstract class for the concrete worker thread.
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
 * <h3>Concrete worker that extends from abstract Worker</h3>
 * The concrete worker has to implement all methods to do the concrete job {@code <T extends Job>}
 * If a method should be invoked on a state transition, the method has to be annotated with {@link StateEnter}, {@link StateLeave}, {@link StateTransition}
 * The graph has to be generated on instantiation of concrete worker. It has to describe all legal transitions.
 * Further it has to provide an initial state and a set of final states.
 * <p>
 * On each legal transition the {@link StateProviderAnnotationParser} invokes the methods that are annotated with {@link StateEnter}, {@link StateLeave}, {@link StateTransition}
 * The variant data is passed to the method as parameter, related to the defined key in the annotation.
 * <p>
 * If a final state is reached, the job is finished and the worker waits for the next job.
 * The concrete worker implementation has to handle the result of the job and return a {@link JobControlCommand} to the worker.
 * <p>
 * If an exception is thrown, the worker calls the method {@link #onJobException(Exception)} from the concrete worker implementation.
 * The concrete worker implementation has to handle the exception and return a {@link JobControlCommand} to the worker.
 *
 * @param <T> The type of the job
 */
@StateProvider
public abstract class Worker<T extends Job> implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Flag to shut down the worker.
   * After that the worker has to be reinitialized.
   * Usually on shutdown of the application.
   */
  private boolean shutdown;

  /**
   * The graph that describes the legal transitions for the concrete worker.
   * The finite state machine will walk through this graph.
   */
  private final Graph graph;

  /**
   * The queue of jobs that are waiting to be performed.
   * {@code <T>} is the type of concrete implementation of {@link Job}
   */
  private final BlockingDeque<T> jobs;

  /**
   * The current job that is performed.
   */
  protected T currentJob;

  /**
   * Constructor for the abstract worker.
   * Registers the queue of jobs that are waiting to be performed.
   * Calls the abstract method {@link #createGraph()}, which has to be implemented by the concrete worker.
   * @param jobs The queue of jobs that are waiting to be performed.
   */
  public Worker(BlockingDeque<T> jobs) {
    this.shutdown = false;
    this.jobs = jobs;
    this.graph = this.createGraph();
  }

  /**
   * Abstract method to create the graph.
   * The graph has to be generated on instantiation of concrete worker.
   * It has to describe all legal transitions.
   * Further it has to provide an initial state and a set of final states.
   * @return The graph that describes the legal transitions for the concrete worker.
   */
  protected abstract Graph createGraph();

  /**
   * Abstract method to handle the exception.
   * The concrete worker implementation has to handle the exception and return a {@link JobControlCommand} to the worker.
   * After that the job is finished and the worker waits for the next job.
   * @param ex The exception that was thrown.
   * @return The {@link JobControlCommand} to the worker.
   */
  protected abstract String onJobException(Exception ex);

  /**
   * Worker thread loop.
   */
  public void run() {
    this.shutdown = false;
    // While application is running
    while (!this.shutdown) {
      try {
        LOGGER.debug("Waiting for job. In Queue:" + this.jobs.size());
        this.currentJob = this.jobs.take();
        LOGGER.trace("Perform Job. In Queue:" + this.jobs.size());
        // Determine if job is an order or a control command
        switch (this.currentJob.getType()) {
          // If ordered, perform job
          case ORDER -> this.performJob(this.currentJob);
          // If control command, shutdown worker
          // This part can be extended to handle other control commands
          case CONTROL -> this.shutdown = true;
        }
      } catch (InterruptedException ex) {
        // Exception is thrown if the worker is interrupted (needed for blocking queue)
        LOGGER.fatal("Interrupted worker thread. Critical shutdown.", ex);
        this.shutdown = true;
      } finally {
        LOGGER.trace("Worker has performed Job. In Queue:" + this.jobs.size());
      }
    }
  }

  /**
   * Job loop. Perform a single Job
   * <ol>
   * <li>Setup the Statemachine with initialized graph</li>
   * <li>Gets the action sequence and the job data for this job</li>
   * <li>Do till final state in graph is reached (or exception is thrown)</li>
   * </ol>
   * A single job loop:
   * <ul>
   * <li> Take action</li>
   * <li> Move to next state</li>
   * <li> The StateProviderAnnotationParser will call all methods in the ConcreteWorker that were marked with a corresponding Annotation</li>
   * </ul>
   * @see StateEnter
   * @see StateLeave
   * @see StateTransition
   * @param job Job to be performed.
   */
  private void performJob(Job job) {

    // Mark if job is finished
    var performed = false;
    // Setup the Statemachine with initialized graph
    var fsm = new FiniteStateMachine(this.graph);

    // Get the action queue
    var actions = job.getActions();
    // Get the job data
    var data = job.getData();

    while (!performed) {
      try {
        // Take next action
        var action = actions.take();
        // S_{i}, The current state S_{i} is the state before the transition
        var leavedState = fsm.getCurrentState();
        // S_{i} -> T_{k} -> S_{i+1} Move to next state, get the transition
        var currentTransition = fsm.moveNextState(action);
        // S_{i+1}, The current state S_{i+1} is the state after the transition
        var enteredState = fsm.getCurrentState();
        // Instantiate the StateProviderAnnotationParser and run it.
        var sap = new StateProviderAnnotationParser();
        sap.runTransitionMethods(this, leavedState, enteredState, currentTransition, data);
        // Check if final state is reached
        performed = this.graph.isFinalState(enteredState);
      } catch (FiniteStateMachineException ex) {
        // Exception is thrown if an illegal transition is performed
        LOGGER.error("Error in job transition. Abort: ", ex);
        this.onJobException(ex);
        performed = true;
      } catch (InterruptedException ex){
        // Exception is thrown if the worker is interrupted (needed for blocking queue)
        LOGGER.error("Critical interruption in job task. Abort: ", ex);
        this.onJobException(ex);
        performed = true;
      }
      catch (InvocationTargetException | IllegalAccessException ex) {
        // This exception is thrown if an exception is thrown during invocation of the methods in the concrete worker
        this.onJobException(ex);
        LOGGER.error("Error in concrete Worker. Abort: ", ex);
        performed = true;
      }
      finally {
        LOGGER.trace("Job Sequence ended");
      }
    }
    LOGGER.trace("Job ended");
  }

  /**
   * Method to put an action at the end of the action queue.
   * Could be used to add an action to perform an error handling.
   * @param action The action to be put at the end of the action queue.
   */
  @SuppressWarnings("unused")
  protected void putActionFirst(Action action) {
    this.currentJob.getActions().addFirst(action);
  }
}
