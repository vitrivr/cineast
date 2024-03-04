package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.system.Configuration;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.IGLModel;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.LightingOptions;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateEnter;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.StateProvider;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Worker;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Graph;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.State;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Transition;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;


/**
 * The RenderWorker is a worker which is responsible for rendering a model.
 * <p>
 * This worker implements all methods which are needed to do a RenderJob.
 * <p>
 * It constructs a Graph which describes the states and transitions which a render worker can do.
 * <p>
 * If a job throws an exception the worker will send a JobControlCommand. ERROR to the caller. Furthermore, the worker
 * will unload the model.
 * <p>
 * Each rendered image will be sent to the caller.
 * <p>
 * The worker initializes the LWJGL engine.
 *
 * @see LWJGLOffscreenRenderer
 * @see Worker
 */
@SuppressWarnings("unused")
@StateProvider
public class RenderWorker extends Worker<RenderJob> {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The Lightweight Java Game Library (LWJGL) Offscreen Renderer instance.
   */
  private LWJGLOffscreenRenderer renderer;

  /**
   * Since only one OpenGl renderer can be instanced
   * use Static queue
   * The queue can be accessed by the caller wit {@link RenderWorker#getRenderJobQueue()}
   * On this queue the caller will provide new render jobs.
   */
  private static BlockingDeque<RenderJob> renderJobQueue;

  /**
   * Instantiates a new RenderWorker.
   * <p>
   * The queue is stores in a static variable.
   */
  public RenderWorker(BlockingDeque<RenderJob> jobs) {
    super(jobs);
    RenderWorker.renderJobQueue = jobs;
    LOGGER.trace("Initialized RenderWorker");
  }

  /**
   * static getter for the renderJobQueue
   * A caller can use get the queue submit new jobs to the render worker.
   * @return the render job queue
   */
  public static BlockingDeque<RenderJob> getRenderJobQueue() {
    return renderJobQueue;
  }


  /**
   * The render worker main thread.
   */
  public void run() {
    Configuration.STACK_SIZE.set((int) java.lang.Math.pow(2, 19));
    this.renderer = new LWJGLOffscreenRenderer();
    var defaultOptions = new WindowOptions();
    renderer.setWindowOptions(defaultOptions);
    renderer.startEngine();
    super.run();
    LOGGER.trace("Running RenderWorker");
  }


  // @formatter:off

  /**
   * Creates the graph for the RenderWorker.
   * @return the graph
   */
  @Override
  protected Graph createGraph() {
    return new Graph(
        // Setup the graph for the RenderWorker
        new Hashtable<>(){{
          {this.put(new Transition(new State(RenderStates.IDLE), new Action(RenderActions.SETUP)),new State(RenderStates.INIT_WINDOW));}
          {this.put(new Transition(new State(RenderStates.INIT_WINDOW), new Action(RenderActions.SETUP)),new State(RenderStates.LOAD_MODEL));}
          {this.put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.SETUP)),new State(RenderStates.INIT_RENDERER));}
          {this.put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {this.put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.LOOKAT)),new State(RenderStates.LOOKAT));}
          {this.put(new Transition(new State(RenderStates.LOAD_MODEL), new Action(RenderActions.LOOKAT_FROM)),new State(RenderStates.LOOK_FROM_AT_O));}
          {this.put(new Transition(new State(RenderStates.INIT_RENDERER), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {this.put(new Transition(new State(RenderStates.INIT_RENDERER), new Action(RenderActions.LOOKAT)),new State(RenderStates.LOOKAT));}
          {this.put(new Transition(new State(RenderStates.INIT_RENDERER), new Action(RenderActions.LOOKAT_FROM)),new State(RenderStates.LOOK_FROM_AT_O));}
          {this.put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.ROTATE)),new State(RenderStates.ROTATE));}
          {this.put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.LOOKAT)),new State(RenderStates.LOOKAT));}
          {this.put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.LOOKAT_FROM)),new State(RenderStates.LOOK_FROM_AT_O));}
          {this.put(new Transition(new State(RenderStates.RENDER), new Action(RenderActions.SETUP)),new State(RenderStates.UNLOAD_MODEL));}
          {this.put(new Transition(new State(RenderStates.ROTATE), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {this.put(new Transition(new State(RenderStates.LOOKAT), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
          {this.put(new Transition(new State(RenderStates.LOOK_FROM_AT_O), new Action(RenderActions.RENDER)),new State(RenderStates.RENDER));}
        }},
        new State(RenderStates.IDLE),
        new HashSet<>(){{
          {this.add(new State(RenderStates.UNLOAD_MODEL));}
        }}
    );
  }
  // @formatter:on

  /**
   * Handler for render exceptions.
   * Unloads the model and sends a JobControlCommand. ERROR to the caller.
   * @param ex The exception that was thrown.
   * @return The handler message.
   */
  @Override
  protected String onJobException(Exception ex) {
    this.unload();
    this.currentJob.putResultQueue(new RenderJob(JobControlCommand.JOB_FAILURE));
    return "Job failed";
  }

  /**
   * Initializes the renderer. Sets the window options and starts the engine.
   */
  @StateEnter(state = RenderStates.INIT_WINDOW, data = RenderData.WINDOWS_OPTIONS)
  public void setWindowOptions(WindowOptions opt) {
    LOGGER.trace("INIT_WINDOW RenderWorker");
    this.renderer = new LWJGLOffscreenRenderer();

    renderer.setWindowOptions(opt);
    renderer.startEngine();
  }

  /**
   * Sets specific render options.
   */
  @StateEnter(state = RenderStates.INIT_RENDERER, data = RenderData.RENDER_OPTIONS)
  public void setRendererOptions(RenderOptions opt) {
    LOGGER.trace("INIT_RENDERER RenderWorker");
    this.renderer.setRenderOptions(opt);
    if (opt.lightingOptions != null) {
      this.renderer.setLighting(opt.lightingOptions);
    }
  }

  /**
   * State to wait for new jobs.
   */
  @StateEnter(state = RenderStates.IDLE)
  public void idle() {
    LOGGER.trace("IDLE RenderWorker");
  }

  /**
   * Register a model to the renderer.
   *
   * @param model The model to register and to be rendered.
   */
  @StateEnter(state = RenderStates.LOAD_MODEL, data = RenderData.MODEL)
  public void registerModel(IModel model) {
    LOGGER.trace("LOAD_MODEL RenderWorker");
    this.renderer.assemble(model);
  }

  /**
   * Renders the model. Sends the rendered image to the caller.
   */
  @StateEnter(state = RenderStates.RENDER)
  public void renderModel() {
    LOGGER.trace("RENDER RenderWorker");
    this.renderer.render();
    var pic = this.renderer.obtain();
    var data = new Variant().set(RenderData.IMAGE, pic);
    var responseJob = new RenderJob(data);
    this.currentJob.putResultQueue(responseJob);
  }

  /**
   * Rotates the camera.
   *
   * @param rotation The rotation vector (x,y,z)
   */
  @StateEnter(state = RenderStates.ROTATE, data = RenderData.VECTOR)
  public void rotate(Vector3f rotation) {
    LOGGER.trace("ROTATE RenderWorker");
    this.renderer.moveCameraOrbit(rotation.x, rotation.y, rotation.z);
  }

  /**
   * Looks at the origin from a specific position. The rotation is not affected. Removes the processed position vector
   * from the list.
   *
   * @param vectors The list of position vectors
   */
  @StateEnter(state = RenderStates.LOOKAT, data = RenderData.VECTORS)
  public void lookAt(LinkedList<Vector3f> vectors) {
    LOGGER.trace("Look at RenderWorker");
    var vec = vectors.poll();
    assert vec != null;
    this.renderer.setCameraOrbit(vec.x, vec.y, vec.z);
  }

  /**
   * Looks from a specific position at the origin. Removes the processed position vector from the list.
   *
   * @param vectors The list of position vectors
   */
  @StateEnter(state = RenderStates.LOOK_FROM_AT_O, data = RenderData.VECTORS)
  public void lookFromAtO(LinkedList<Vector3f> vectors) {
    LOGGER.trace("LOOK_FROM_AT_O RenderWorker");
    var vec = vectors.poll();
    assert vec != null;
    this.renderer.lookFromAtO(vec.x, vec.y, vec.z);
  }

  /**
   * Unloads the model and sends a JobControlCommand.JOB_DONE to the caller.
   */
  @StateEnter(state = RenderStates.UNLOAD_MODEL)
  public void unload() {
    LOGGER.trace("UNLOAD_MODEL RenderWorker");
    this.renderer.clear();
    this.renderer = null;
    var responseJob = new RenderJob(JobControlCommand.JOB_DONE);
    this.currentJob.putResultQueue(responseJob);
  }
}
