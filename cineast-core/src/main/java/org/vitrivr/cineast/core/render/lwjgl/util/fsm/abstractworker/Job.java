package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;

/**
 * A Job is a container for data and actions. A Job can be of type ORDER, RESPONSE or CONTROL
 * <ul>
 *   <li>ORDER: Needs action, data and provides a result queue </li>
 *   <li>RESPONSE: A job which contains data which the worker calculated</li>
 *   <li>CONTROL: A job which contains a command e.g. end of job or error etc.</li>
 * </ul>
 * <p>
 * {@link  JobType}
 * <p>
 * If a job is a CONTROL job it contains a command
 * {@link  JobControlCommand}
 */
public abstract class Job {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The actions to perform These actions are performed by the worker
   * <p>
   * The actions can be added before registering the job to the worker
   * <p>
   * The actions can also be added on the fly
   */
  private final BlockingDeque<Action> actions;
  /**
   * The result queue is used to provide results in a ORDER job
   */
  private final BlockingDeque<Job> resultQueue;
  /**
   * The data to process The resulting data
   */
  private Variant data;
  /**
   * The type of the job The type of the job can be ORDER, RESPONSE or CONTROL {@link  JobType}
   */
  private final JobType type;
  /**
   * The command of the job if the job is a control job The command of the job can be END, ERROR or NONE {@link   JobControlCommand}
   */
  private final JobControlCommand command;

  /**
   * Creates a new ORDER Job to perform a task.
   * <p>
   * ORDER: Needs actions, data and provides a result queue
   *
   * @param actions The actions to perform
   * @param data   The data to process
   */
  protected Job(BlockingDeque<Action> actions, Variant data) {
    this.command = null;
    this.actions = actions;
    this.data = data;
    this.type = JobType.ORDER;
    this.resultQueue = new LinkedBlockingDeque<>();
  }

  /**
   * Creates a new Response Job.
   * <p>
   * RESPONSE: contains data which the worker calculated
   *
   * @param data The resulting data
   */
  protected Job(Variant data) {
    this.actions = null;
    this.command = null;
    this.data = data;
    this.type = JobType.RESPONSE;
    this.resultQueue = null;
  }

  /**
   * Creates a new Control Job
   * <p>
   * CONTROL: contains a command e.g. end of job or error etc.
   * @param command The command of the job
   */
  protected Job(JobControlCommand command) {
    this.actions = null;
    this.command = command;
    this.type = JobType.CONTROL;
    this.resultQueue = null;
  }

  /**
   * Returns the result of the job
   * <p>
   * This method blocks until a result is available
   *
   * @return The result of the job
   * @throws InterruptedException If the thread is interrupted while waiting for a result
   */
  public Job getResults() throws InterruptedException {
    assert this.resultQueue != null;
    return this.resultQueue.take();
  }

  /**
   * Puts a result into the result queue
   *
   * @param job The result to put into the result queue
   */
  public void putResultQueue(Job job) {
    try {
      assert this.resultQueue != null;
      this.resultQueue.put(job);
    } catch (InterruptedException ex) {
      LOGGER.error("Error while putting result into result queue", ex);
    }
  }

  /**
   * Sets the data of the job
   * @param data The data to set
   */
  public void setData(Variant data) {
    this.data = data;
  }

  /**
   * Returns the data of the job
   * @return The data of the job
   */
  public Variant getData() {
    return this.data;
  }

  /**
   * Returns the actions queue of the job
   * @return The actions queue of the job
   */
  public BlockingDeque<Action> getActions() {
    return this.actions;
  }

  /**
   * Returns the type of the job
   * @return The type of the job
   */
  public JobType getType() {
    return this.type;
  }

  /**
   * Returns the command of the job
   * @return The command of the job
   */
  public JobControlCommand getCommand() {
    return this.command;
  }

  /**
   * Cleans the job
   * <p>
   * This method should be called after the job is processed
   * <p>
   * It does not affect data in the variant
   */
  public void clean() {
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