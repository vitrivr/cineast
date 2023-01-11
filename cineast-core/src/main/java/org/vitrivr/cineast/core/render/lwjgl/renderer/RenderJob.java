package org.vitrivr.cineast.core.render.lwjgl.renderer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import javax.swing.JWindow;
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
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum.Type;

public class RenderJob extends Job {

  private static final Logger LOGGER = LogManager.getLogger();
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
  }

  ;

  public static List<BufferedImage> performStandardRenderJob(BlockingDeque<RenderJob> renderJobQueue, IModel model, double[][] cameraPositions, WindowOptions windowOptions, RenderOptions renderOptions) {
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
      vectors.add(new Vector3f((float) position[0], (float) position[1], (float) position[2]));
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
        }
      }
    } catch (InterruptedException ex) {
      LOGGER.error("Could not render model", ex);
    }
    return image;
  }
}
