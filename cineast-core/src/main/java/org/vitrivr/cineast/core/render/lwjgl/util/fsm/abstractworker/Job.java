package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.util.concurrent.BlockingDeque;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;

public abstract class Job {

  private final BlockingDeque<Action> actions;
  private Variant data;
  private JobType type;
  private JobControlCommand command;

  protected Job(BlockingDeque<Action> actions, Variant data) {
    this.command = null;
    this.actions = actions;
    this.data = data;
    this.type = JobType.ORDER;
  }

  protected Job(Variant data) {
    this.actions = null;
    this.command = null;
    this.data = data;
    this.type = JobType.RESPONSE;
  }

  protected Job(JobControlCommand command) {
    this.actions = null;
    this.command = command;
    this.type = JobType.CONTROL;
  }

  public void setData(Variant data) {
    this.data = data;
  }

  public Variant getData(){
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
}