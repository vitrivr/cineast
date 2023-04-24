package org.vitrivr.cineast.core.render.lwjgl.util.fsm;

import java.util.concurrent.BlockingDeque;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Job;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobType;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;

/**
 * Example class for concrete Job.
 * Has to extend the abstract Job class.
 * <p>
 * The concrete Job has to be defined in the concrete worker class as generic type.
 * @see ConcreteWorker
 */
@SuppressWarnings("unused")
public class ConcreteJob extends Job {

  /**
   * Example concrete data that can be used in the concrete worker class.
   */
  protected Integer x;

  /**
   * For instantiating a new RESPONSE Job.
   * @see Job#Job(Variant)
   * @see JobType
   * @param data Generic data that can be used in the concrete worker class.
   */
  public ConcreteJob(Variant data) {
    super(data);
  }

  /**
   * For instantiating a new CONTROL Job.
   * @see Job#Job(JobControlCommand)
   * @see JobType
   * @param command Command that can be used to control the worker.
   */
  public ConcreteJob(JobControlCommand command) {
    super(command);
  }

  /**
   * For instantiating a new ORDER Job.
   * @see Job#Job(BlockingDeque, Variant)
   * @param actions  Actions that have to be performed by the worker.
   * @param data Generic data that can be used in the concrete worker class.
   */
  public ConcreteJob(BlockingDeque<Action> actions, Variant data) {
    super(actions, data);
  }
}
