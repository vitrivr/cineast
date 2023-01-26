package org.vitrivr.cineast.core.render.lwjgl.util.fsm;

import java.util.concurrent.BlockingDeque;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Job;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;

public class ConcreteJob extends Job {

  protected Integer x;

  public ConcreteJob(Variant data) {
    super(data);
  }

  public ConcreteJob(JobControlCommand command) {
    super(command);
  }
  public ConcreteJob(BlockingDeque<Action> actions, Variant data) {
    super(actions, data);
  }


}
