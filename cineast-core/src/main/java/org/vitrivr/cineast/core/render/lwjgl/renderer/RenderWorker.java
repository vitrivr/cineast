package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.system.Configuration;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Job;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateEnter;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateProvider;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Worker;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;

@StateProvider
public class RenderWorker extends Worker<RenderJob> {

  private static final Logger LOGGER = LogManager.getLogger();
  LWJGLOffscreenRenderer renderer;
  private static BlockingDeque<RenderJob> renderJobQueue;

  public RenderWorker(BlockingDeque<RenderJob> jobs) {
    super(jobs);
    RenderWorker.renderJobQueue = jobs;
    LOGGER.trace("Initialized RenderWorker");
  }

  public static BlockingDeque<RenderJob> getRenderJobQueue() {
    return renderJobQueue;
  }

  public void run() {
    Configuration.STACK_SIZE.set((int) java.lang.Math.pow(2, 15));
    this.renderer = new LWJGLOffscreenRenderer();
    var defaultOptions = new WindowOptions();
    renderer.setWindowOptions(defaultOptions);
    renderer.startEngine();
    super.run();
    LOGGER.trace("Running RenderWorker");
  }

  // @formatter:off
  @Override
  protected Graph createGraph() {
    return new Graph(
        new Hashtable<>(){{
          {put(new Transition(new State(RenderStates.IDLE), new Action(RenderActions.SETUP)),new State(RenderStates.INIT_WINDOW));}
          {put(new Transition(new State(RenderStates.INIT_WINDOW), new Action(RenderActions.SETUP)),new State(RenderStates.LOAD_MODEL));}
          {put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.SETUP)),new State(RenderStates.INIT_RENDERER));}
          {put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.LOOKAT)),new State(RenderStates.LOOKAT));}
          {put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.LOOKAT_FROM)),new State(RenderStates.LOOK_FROM_AT_O));}
          {put(new Transition(new State(RenderStates.INIT_RENDERER), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {put(new Transition(new State(RenderStates.INIT_RENDERER), new Action(RenderActions.LOOKAT)),new State(RenderStates.LOOKAT));}
          {put(new Transition(new State(RenderStates.INIT_RENDERER), new Action(RenderActions.LOOKAT_FROM)),new State(RenderStates.LOOK_FROM_AT_O));}
          {put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.ROTATE)),new State(RenderStates.ROTATE));}
          {put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.LOOKAT)),new State(RenderStates.LOOKAT));}
          {put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.LOOKAT_FROM)),new State(RenderStates.LOOK_FROM_AT_O));}
          {put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.SETUP)),new State(RenderStates.UNLOAD_MODEL));}
          {put(new Transition(new State(RenderStates.ROTATE), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {put(new Transition(new State(RenderStates.LOOKAT), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {put(new Transition(new State(RenderStates.LOOK_FROM_AT_O), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
        }},
        new State(RenderStates.IDLE),
        new HashSet<>(){{
          {add(new State(RenderStates.UNLOAD_MODEL));}
        }}
    );
  }
  // @formatter:on


  @StateEnter(state = RenderStates.INIT_WINDOW, data = RenderData.WINDOWS_OPTIONS)
  public void setWindowOptions(WindowOptions opt) {
    LOGGER.trace("INIT_WINDOW RenderWorker");
    this.renderer = new LWJGLOffscreenRenderer();

    renderer.setWindowOptions(opt);
    renderer.startEngine();
  }
  @StateEnter(state = RenderStates.INIT_RENDERER, data = RenderData.RENDER_OPTIONS)
  public void setRendererOptions(RenderOptions opt) {
    LOGGER.trace("INIT_RENDERER RenderWorker");
    this.renderer.setRenderOptions(opt);
  }



  @StateEnter(state = RenderStates.IDLE)
  public void idle() {
    LOGGER.trace("IDLE RenderWorker");
  }

  @StateEnter(state = RenderStates.LOAD_MODEL, data = RenderData.MODEL)
  public void registerModel(IModel model) {
    LOGGER.trace("LOAD_MODEL RenderWorker");
    this.renderer.assemble(model);
  }

  @StateEnter(state = RenderStates.RENDER)
  public void renderModel() {
    LOGGER.trace("RENDER RenderWorker");
    this.renderer.render();
    var pic = this.renderer.obtain();
    var data = new Variant().set(RenderData.IMAGE, pic);
    var responseJob = new RenderJob(data);
    this.currentJob.putResultQueue(responseJob);
  }

  @StateEnter(state = RenderStates.ROTATE, data = RenderData.VECTOR)
  public void rotate(Vector3f rotation) {
    LOGGER.trace("ROTATE RenderWorker");
    this.renderer.moveCameraOrbit(rotation.x, rotation.y, rotation.z);
  }

  @StateEnter(state = RenderStates.LOOKAT, data = RenderData.VECTORS)
  public void lookAt(LinkedList<Vector3f> vectors) {
    LOGGER.trace("Look at RenderWorker");
    var vec = vectors.poll();
    assert vec != null;
    this.renderer.setCameraOrbit(vec.x, vec.y, vec.z);
  }

  @StateEnter(state = RenderStates.LOOK_FROM_AT_O, data = RenderData.VECTORS)
  public void lookFromAtO(LinkedList<Vector3f> vectors) {
    LOGGER.trace("LOOK_FROM_AT_O RenderWorker");
    var vec = vectors.poll();
    assert vec != null;
    this.renderer.lookFromAtO(vec.x, vec.y, vec.z);
  }

  @StateEnter(state = RenderStates.UNLOAD_MODEL)
  public void unload() {
    LOGGER.trace("UNLOAD_MODEL RenderWorker");
    this.renderer.clear();
    this.renderer = null;
    var responseJob = new RenderJob(JobControlCommand.JOB_DONE);
    this.currentJob.putResultQueue(responseJob);
  }
}
