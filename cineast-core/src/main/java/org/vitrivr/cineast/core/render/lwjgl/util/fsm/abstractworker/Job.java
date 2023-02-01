package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;

public abstract class Job {

  private static final Logger LOGGER = LogManager.getLogger();
  private final BlockingDeque<Action> actions;

  private final BlockingDeque<Job> resultQueue;
  private Variant data;
  private final JobType type;
  private final JobControlCommand command;

  /**
   * Creates a new Job to perform a task Creates a result Queue to provide results or commands
   *
   * @param actions
   * @param data
   */
  protected Job(BlockingDeque<Action> actions, Variant data) {
    this.command = null;
    this.actions = actions;
    this.data = data;
    this.type = JobType.ORDER;
    this.resultQueue = new LinkedBlockingDeque<>();
  }

  /**
   * Creates a new Response Job A Job response contains data wich the worker calculated
   *
   * @param data
   */
  protected Job(Variant data) {
    this.actions = null;
    this.command = null;
    this.data = data;
    this.type = JobType.RESPONSE;
    this.resultQueue = null;
  }

  /**
   * Creates a new Control Job A control Job contains command e.g. end of job or error etc.
   *
   * @param command
   */
  protected Job(JobControlCommand command) {
    this.actions = null;
    this.command = command;
    this.type = JobType.CONTROL;
    this.resultQueue = null;
  }

  public Job getResults() throws InterruptedException {
    return this.resultQueue.take();
  }

  public void putResultQueue(Job job) {
    try {
      this.resultQueue.put(job);
    } catch (InterruptedException ex) {
      LOGGER.error("Error while putting result into result queue", ex);
    }
  }

  public void setData(Variant data) {
    this.data = data;
  }

  public Variant getData() {
    return this.data;
  }

  public BlockingDeque<Action> getActions() {
    return this.actions;
  }

  public JobType getType() {
    return this.type;
  }

  public JobControlCommand getCommand() {
    return this.command;
  }

  public void clean(){
    this.data.clear();
    this.data = null;
    if (this.actions != null) {
      this.actions.clear();
    }
    if (this.resultQueue != null) {
      this.resultQueue.clear();
    }
  }
}