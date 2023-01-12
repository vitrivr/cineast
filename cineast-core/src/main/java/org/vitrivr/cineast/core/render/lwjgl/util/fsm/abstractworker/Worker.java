package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller.FiniteStateMachine;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller.FiniteStateMachineException;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;

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
        LOGGER.error("Interrupted while waiting for job", ex);
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
        LOGGER.error("Error in job. Abort: " + ex.getMessage());
        this.shutdown = true;
      } catch (InvocationTargetException | IllegalAccessException ex) {
        //TODO The result and control job should not be an concrete job
        LOGGER.error("Error in job. Abort: " + ex.getMessage());
        this.shutdown = true;
      } finally {
        LOGGER.trace("Job Secuence ended");
      }
    }
    LOGGER.trace("Job ended");
  }

  protected void putActionfirst(Action action) {
    this.currentJob.getActions().addFirst(action);
  }

}
