package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Job;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;

public class RenderJob extends Job {

  protected BlockingDeque<RenderJob> resultQueue;

  public RenderJob(BlockingDeque<Action> actions, Variant data) {
    super(actions, data);
    this.resultQueue = new LinkedBlockingDeque<>();
  }

  public RenderJob(Variant data) {
    super(data);
    this.resultQueue = null;
  }
  public RenderJob(JobControlCommand command) {
    super(command);
    this.resultQueue = null;
  }


  public RenderJob getResults() throws InterruptedException {
    return this.resultQueue.take();
  };
}
