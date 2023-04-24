package org.vitrivr.cineast.core.render.lwjgl.util.fsm;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateEnter;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateProvider;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Worker;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;

/**
 * Example class for concrete worker. Has to extend the abstract worker class.
 * <p>
 * The concrete worker has to implement all methods which are needed to do the ConcreteJob.
 * Each method which has to be executed on transitions has to be annotated
 * <p>
 * The concrete worker has to define the Graph
 * <p>
 * It hast to handle exceptions with the {@link #onJobException(Exception)} method.
 * <p>
 * It has to provide the Job END command on a final state.
 *
 * @see StateEnter
 * @see StateProvider
 * @see ConcreteJob
 * @see Worker
 */
@SuppressWarnings("unused")
@StateProvider
public class ConcreteWorker extends Worker<ConcreteJob> {

  /**
   * Instantiates a new ConcreteWorker. Registers Job input queue for ConcreteJobs
   */
  public ConcreteWorker(BlockingDeque<ConcreteJob> actions) {
    super(actions);
  }

  /**
   * Create the concrete Graph of the application.
   * @return Graph of the application.
   */
  // @formatter:off
  @Override
  protected Graph createGraph() {
    return new Graph(new Hashtable<>() {{
      {put(new Transition(new State(ConcreteStates.STARTUP), new Action(ConcreteActions.WAIT)), new State(ConcreteStates.STARTUP));}
      {put(new Transition(new State(ConcreteStates.STARTUP), new Action(ConcreteActions.PRINT)), new State(ConcreteStates.PRINT));}
      {put(new Transition(new State(ConcreteStates.STARTUP), new Action(ConcreteActions.END)), new State(ConcreteStates.RESULT));}
      {put(new Transition(new State(ConcreteStates.PRINT), new Action(ConcreteActions.PRINT)), new State(ConcreteStates.PRINT));}
      {put(new Transition(new State(ConcreteStates.PRINT), new Action(ConcreteActions.PRINT)), new State(ConcreteStates.RESULT));}
    }},
        new State(ConcreteStates.STARTUP),
        new HashSet<>() {{
          {add(new State(ConcreteStates.RESULT));}
        }});
  }
  // @formatter:on

  /**
   * Handle exceptions which occur during the execution of the job.
   *
   * @param ex Exception which occurred.
   * @return handle message
   */
  @Override
  protected String onJobException(Exception ex) {
    this.currentJob.putResultQueue(new ConcreteJob(JobControlCommand.JOB_FAILURE));
    return "nothing cleaned";
  }

  /**
   * A method example for a state enter action without data.
   */
  @StateEnter(state = ConcreteStates.STARTUP)
  public void startup() {
    System.out.println("Startup");
  }

  /**
   * A method example for a state enter action with data.
   */
  @StateEnter(state = ConcreteStates.PRINT, data = {ConcreteData.MODEL1, ConcreteData.MODEL2})
  public void printInteger(int i) {

    var data = new Variant().set(ConcreteData.MODEL3, "model3");
    var responseJob = new ConcreteJob(data);
    this.currentJob.putResultQueue(responseJob);
  }

  /**
   * A method example for a state enter action with data, where the state is final state.
   */
  @StateEnter(state = ConcreteStates.RESULT, data = ConcreteData.MODEL1)
  public void provideResult(LinkedBlockingDeque<String> q) {
    var responseJob = new ConcreteJob(JobControlCommand.JOB_DONE);
    this.currentJob.putResultQueue(responseJob);
  }

}
