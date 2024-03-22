package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.util.datatype.Variant;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.Job;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobType;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.model.Action;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;

/**
 * The RenderJob is a job which is responsible for rendering a model.
 * <p>
 * This job extends the abstract class Job.
 * <p>
 *   It provides constructors for the different types of jobs.
 *   ORDER Job to render a model.
 *   COMMAND Job signals caller that the job is done or an error occurred.
 *   RESULT Job contains the result of the rendering.
 */
public class RenderJob extends Job {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Creates a new ORDER RenderJob with the given action sequence and data (containing the model to render).
   */
  public RenderJob(BlockingDeque<Action> actions, Variant data) {
    super(actions, data);
  }

  /**
   * Creates a new RESPONSE RenderJob with the rendered image.
   */
  public RenderJob(Variant data) {
    super(data);
  }

  /**
   * Creates a new CONTROL RenderJob with the given command.
   */
  public RenderJob(JobControlCommand command) {
    super(command);
  }


  /**
   * Static method to create a standard render job.
   * <p>
   * @see RenderJob#performStandardRenderJob(BlockingDeque, IModel, LinkedList, WindowOptions, RenderOptions)
   */
  public static List<BufferedImage> performStandardRenderJob(BlockingDeque<RenderJob> renderJobQueue, IModel model, double[][] cameraPositions, WindowOptions windowOptions, RenderOptions renderOptions) {
    var cameraPositionVectors = new LinkedList<Vector3f>();
    for (double[] cameraPosition : cameraPositions) {
      assert cameraPosition.length == 3;
      cameraPositionVectors.add(new Vector3f((float) cameraPosition[0], (float) cameraPosition[1], (float) cameraPosition[2]));
    }
    return performStandardRenderJob(renderJobQueue, model, cameraPositionVectors, windowOptions, renderOptions);
  }

  /**
   * Static method to create a standard render job.
   * <p>
   *   <ul>
   *   <li>Creates a job for given model and each camera position.</li>
   *   <li>Adds the data to the variant (data bag)</li>
   *   <li>Generates the needed actions for the job.</li>
   *   <li>Creates the job and adds it to the provided queue.</li>
   *   <li>Waits for the job to finish. (or fail)</li>
   *   <li>Returns the rendered images.</li>
   *   <li>Cleans up the resources.</li>
   *   <ul>
   *   <p>
   * @param renderJobQueue The queue to add the job to.
   * @param model        The model to render.
   * @param cameraPositions The camera positions to render the model from.
   * @param windowOptions The window options to use for the rendering.
   * @param renderOptions The render options to use for the rendering.
   * @return The rendered images.
   */
  public static List<BufferedImage> performStandardRenderJob(BlockingDeque<RenderJob> renderJobQueue, IModel model, LinkedList<Vector3f> cameraPositions, WindowOptions windowOptions, RenderOptions renderOptions) {
    // Create data bag for the job.
    var jobData = new Variant();
    jobData.set(RenderData.WINDOWS_OPTIONS, windowOptions)
        .set(RenderData.RENDER_OPTIONS, renderOptions)
        .set(RenderData.MODEL, model);

    // Setup the action sequence to perform the jop
    // In standard jop, this is an image for each camera position
    var actions = new LinkedBlockingDeque<Action>();

    actions.add(new Action(RenderActions.SETUP));
    actions.add(new Action(RenderActions.SETUP));
    actions.add(new Action(RenderActions.SETUP));

    var vectors = new LinkedList<Vector3f>();
    for (var position : cameraPositions) {
      // Create a copy of the vector to avoid concurrent modification exceptions
      vectors.add(new Vector3f(position));
      actions.add(new Action(RenderActions.LOOKAT_FROM));
      actions.add(new Action(RenderActions.RENDER));
    }
    actions.add(new Action(RenderActions.SETUP));
    jobData.set(RenderData.VECTORS, vectors);

    // Add the job to the queue
    var job = new RenderJob(actions, jobData);
    renderJobQueue.add(job);

    // Wait for the job to finish
    var finishedJob = false;
    var image = new ArrayList<BufferedImage>();

    // Add images to result or finish the job
    try {
      while (!finishedJob) {
        var result = job.getResults();
        if (result.getType() == JobType.RESPONSE) {
          image.add(result.getData().get(BufferedImage.class, RenderData.IMAGE));
        } else if (result.getType() == JobType.CONTROL) {
          if (result.getCommand() == JobControlCommand.JOB_DONE) {
            finishedJob = true;
          }
          if (result.getCommand() == JobControlCommand.JOB_FAILURE) {
            LOGGER.error("Job failed");
            finishedJob = true;
          }
        }
      }
    } catch (InterruptedException ex) {
      LOGGER.error("Could not render model", ex);
    } finally {
      job.clean();
    }
    return image;
  }
}
