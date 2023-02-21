package org.vitrivr.cineast.core.render.lwjgl.util.fsm;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Job;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateEnter;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateProvider;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Worker;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;

@StateProvider
public class ConcreteWorker extends Worker<ConcreteJob> {

  public ConcreteWorker(BlockingDeque<ConcreteJob> actions) {
    super(actions);

    var v = new Variant();
    v.set("x", 1);
    var x2 = v.get(Integer.class, "x");


  }
  @Override
  protected Graph createGraph() {
    return new Graph(new Hashtable<>() {{
        {put(new Transition(new State("Startup"), new Action("wait")), new State("Startup"));}
        {put(new Transition(new State("Startup"), new Action("print")), new State("Print"));}
        {put(new Transition(new State("Print"), new Action("print")), new State("Print"));}
        {put(new Transition(new State("Print"), new Action("end")), new State("Result"));}
      }},
        new State("Startup"),
        new HashSet<>() {{
        {add(new State("Result"));}
      }});
  }

  @Override
  protected String onJobException(Exception ex) {
    this.currentJob.putResultQueue(new ConcreteJob(JobControlCommand.JOB_FAILURE));
    return "nothing cleaned";
  }

  @StateEnter(state = ConreteStates2.TEST)
  public void startup() {
    System.out.println("Startup");
  }

  @StateEnter(state = "Print", data = {"x"})
  public void printInteger(int i) {
    this.currentJob.x = i;
    System.out.println("printInteger: " + i);
  }

  @StateEnter(state = "Print", data =  {"sb"})
  public void printSb(StringBuffer sb) {
    System.out.println("printSb: " + sb);
  }

  @StateEnter(state = "Result", data = "q")
  public void provideResult(LinkedBlockingDeque<String> q) {
    q.add("provideResult");
  }

}
