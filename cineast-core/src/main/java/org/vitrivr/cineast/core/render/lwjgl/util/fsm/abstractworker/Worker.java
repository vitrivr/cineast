package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller.FiniteStateMachine;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.controller.FiniteStateMachineException;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;

@StateProvider
public abstract class  Worker <T extends Job>  implements Runnable {


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
    this.shutdown = false;
    while (!this.shutdown) {
      try {
        LOGGER.info("Waiting for job. In Queue:" + this.jobs.size());
        this.currentJob = this.jobs.take();
        LOGGER.info("Perform Job. In Queue:" + this.jobs.size());
        switch (this.currentJob.getType()) {
          case ORDER -> this.performJob(this.currentJob);
          case CONTROL -> this.shutdown = true;
        }
      } catch (InterruptedException ex) {
        this.shutdown = true;
      } finally {
        LOGGER.info("Worker Task ended");
      }
    }
  }

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
        LOGGER.fatal(ex.getMessage());
        this.shutdown = true;
      } catch (InvocationTargetException | IllegalAccessException ex) {
        //TODO Check Stack Space Error
        LOGGER.fatal(ex.getMessage());
        this.shutdown = true;
      } finally {
        //LOGGER.info("Job Secuence ended");
      }
    }
    LOGGER.info("Job ended");
  }

  protected void putActionfirst(Action action) {
    this.currentJob.getActions().addFirst(action);
  }

}
