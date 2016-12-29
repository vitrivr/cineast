package org.vitrivr.cineast.core.render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import org.vitrivr.cineast.core.data.m3d.Mesh;

import java.awt.image.BufferedImage;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL2GL3.GL_LINE;

/**
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class JOGLOffscreenRenderer {

    /**
     * Default GLProfile to be used. Should be GL2.
     */
    private static final GLProfile glprofile = GLProfile.get(GLProfile.GL2);

    /**
     * GLCapabilities. Can be used to enable/disable hardware acceleration etc.
     */
    private static final GLCapabilities capabilities = new GLCapabilities(glprofile);

    /**
     * OpenGL Utility Library reference
     */
    private final GLU glu;

    /**
     * OpenGL context reference used for drawing.
     */
    private final GL2 gl;

    /**
     * Width of the JOGLOffscreenRenderer in pixels.
     */
    private final int width;

    /**
     * Height of the JOGLOffscreenRenderer in pixels.
     */
    private final int height;

    /**
     * Aspect-ration of the JOGLOffscreenRenderer.
     */
    private final float aspect;

    /*
     * This code-block can be used to configure the off-screen renderer's capabilities.
     */
    static {
        capabilities.setOnscreen(false);
        capabilities.setHardwareAccelerated(true);
    }

    /**
     * Default constructor. Defines the width and the height of this JOGLOffscreenRenderer and
     * initializes all the required OpenGL bindings.
     *
     * @param width Width in pixels.
     * @param height Height in pixels.
     */
    public JOGLOffscreenRenderer(int width, int height) {
        /* Assign widht and height. */
        this.width = width;
        this.height = height;
        this.aspect = (float) width / (float) height;

        /* Initialize GLOffscreenAutoDrawable. */
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glprofile);
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null,capabilities,null,width,height);
        drawable.display();
        drawable.getContext().makeCurrent();

        /* Initialize GLU and GL2. */
        this.glu = new GLU();
        this.gl = drawable.getGL().getGL2();
    }

    /**
     * Getter for width.
     *
     * @return Width of the JOGLOffscreenRenderer.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Getter for height.
     *
     * @return Height of the JOGLOffscreenRenderer.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Getter for aspect.
     *
     * @return Aspect ration of the JOGLOffscreenRenderer.
     */
    public final float getAspect() {
        return aspect;
    }

    /**
     * Renders a mesh and positions the camera in its default position in [1,0,0]
     * (spherical coordinates).
     *
     * @param mesh Mesh object to be drawn.
     */
    public void render(Mesh mesh) {
         this.render(mesh, 1.0f, 0.0f, 0.0f);
    }

    /**
     * Renders a mesh and positions the camera according to the specified, spherical coordinates so
     * that it faces the origin. Calling this methods is equivalent to calling clear(), draw() and position()
     * in this order.
     *
     * @param mesh Mesh object to be drawn.
     * @param distance Distance of the camera from the origin (r).
     * @param polar Polar angle of the camera in degrees
     * @param azimuth Azimuth angle of the camer in degrees.
     */
    public void render(Mesh mesh, float distance, float polar, float azimuth) {
        /* Clears buffers to preset-values. */
        this.clear();

        /* Draws the mesh. */
        this.draw(mesh);

        /* Positions the camera. */
        this.position(distance, polar, azimuth);
    }

    /**
     * Draws a mesh in the buffer.
     *
     * @param mesh Mesh object to be drawn.
     */

    public void draw(Mesh mesh) {
        /* Switch matrix mode to modelview. */
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        /* Set drawing-style.*/
        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        /* Assemble and render model */
        int modelHandle = mesh.assemble(this.gl);
        gl.glCallList(modelHandle);
        gl.glDeleteLists(modelHandle, 1);
    }

    /**
     * Changes the position of the camera. The camera can be freely rotated around the origin [1,0,0] (cartesian
     * coordinates) and it can take any distance from that same origin.
     *
     * @param distance Distance of the camera from the origin (r).
     * @param polar Polar angle of the camera in degrees
     * @param azimuth Azimuth angle of the camer in degrees.
     */
    public final void position(float distance, float polar, float azimuth) {
        /* Switch matrix mode to projection. */
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        /* Set perspective. */
        glu.gluPerspective(60.0f, this.aspect, 0.01f, 100.0f);

        /* Move camera a distance r away from the center. */
        gl.glTranslatef(0, 0, -distance);

        gl.glRotatef(polar, 0, 1, 0);
        gl.glRotatef(azimuth, 1, 0, 0);

        /* move to center of circle. */
        gl.glTranslatef(0, 0, 0);
    }

    /**
     * Clears buffers to preset-values.
     */
    public final void clear() {
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Obtains and returns a BufferedImage in AWT orientation from the current JOGLOffscreenRenderer.
     *
     * @return BufferedImage containing a snapshot of the current render-buffer.
     */
    public final BufferedImage obtain() {
        /* Create and return a BufferedImage from buffer. */
        AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(gl.getGL2().getGLProfile(), false);
        return glReadBufferUtil.readPixelsToBufferedImage(gl.getGL2(), true);
    }
}
