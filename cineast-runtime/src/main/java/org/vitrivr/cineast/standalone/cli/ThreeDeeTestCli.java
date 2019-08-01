package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A CLI command that can be used to start a 3d test.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "3dtest", description = "Starts a 3D rendering test to check availability of an OpenGL renderer.")
public class ThreeDeeTestCli extends CineastCli {
    @Override
    public void run() {
        super.loadConfig();
        System.out.println("Performing 3D test...");

        Mesh mesh = new Mesh(2, 6);
        mesh.addVertex(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f));
        mesh.addVertex(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        mesh.addVertex(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f));

        mesh.addVertex(new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f));
        mesh.addVertex(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
        mesh.addVertex(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(1.0f, 0.0f, 1.0f));

        mesh.addFace(new Vector3i(1, 2, 3));
        mesh.addFace(new Vector3i(4, 5, 6));

        JOGLOffscreenRenderer renderer = new JOGLOffscreenRenderer(250, 250);
        renderer.retain();
        renderer.positionCameraPolar(2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        renderer.assemble(mesh);
        renderer.render();
        BufferedImage image = renderer.obtain();
        renderer.clear();
        renderer.release();

        try {
            ImageIO.write(image, "PNG", new File("cineast-3dtest.png"));
            System.out.println("3D test complete. Check for cineast-3dtest.png");
        } catch (IOException e) {
            System.err.println("Could not save rendered image due to an IO error.");
        }
    }
}
