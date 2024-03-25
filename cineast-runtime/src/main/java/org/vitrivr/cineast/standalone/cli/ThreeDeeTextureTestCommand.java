package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;
import org.vitrivr.cineast.core.data.m3d.texturemodel.ModelLoader;

import org.vitrivr.cineast.core.data.m3d.texturemodel.util.TextureLoadException;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

@Command(name = "3dtexttest", description = "Starts a 3D rendering test to check availability of an LWJGL OpenGL renderer.")
public class ThreeDeeTextureTestCommand extends AbstractCineastCommand {

    @Override
    public void execute() {
        System.out.println("Performing 3D test on texture model...");
        Model model = null;
        try {
            model = ModelLoader.loadModel("unit-cube",
                    "./resources/renderer/lwjgl/models/unit-cube/Cube_Text.gltf");
        } catch (TextureLoadException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        // Options for window
        var windowOptions = new WindowOptions() {{
            this.hideWindow = true;
            this.width = 600;
            this.height = 600;
        }};
        // Options for renderer
        var renderOptions = new RenderOptions() {{
            this.showTextures = true;
        }};
        // Get camera viewpoint for chosen strategy
        var cameraPositions = new LinkedList<Vector3f>() {{
            add(new Vector3f(-1, 1, 1).normalize());
        }};
        // Render an image for each camera position
        var images = RenderJob.performStandardRenderJob(RenderWorker.getRenderJobQueue(),
                model, cameraPositions, windowOptions, renderOptions);

        try {
            ImageIO.write(images.get(0), "PNG", new File("cineast-3dtexttest.png"));
            System.out.println("3D test complete. Check for cineast-3dtexttest.png");
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not save rendered image due to an IO error.");
        }
    }
}
