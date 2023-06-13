package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

/**
 * JobType is used to describe the content of a job
 * <ul>
 * <li>ORDER: for a job that needs to be performed by the worker</li>
 * <li>RESPONSE: to send the result of a job back to the caller</li>
 * <li>CONTROL: to control the states of worker and caller</li>
 * </ul>
 */
public enum JobType {

  /**
   * An Order job contains a request to perform a specific sequence of actions by the worker.
   */
  ORDER,

  /**
   * Result jobs containing result data e.g. a rendered image
   */
  RESPONSE,

  /**
   * Control jobs containing a control command, e.g. end of job
   * If the job is a control job, the job must contain a JobControlCommand
   * @see JobControlCommand
   */
  CONTROL,
}
