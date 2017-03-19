package org.vitrivr.cineast.core.render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.m3d.Renderable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.GL2GL3.GL_POINT;
import static com.jogamp.opengl.GLContext.CONTEXT_CURRENT;
import static com.jogamp.opengl.GLContext.CONTEXT_CURRENT_NEW;

/**
 * This class can be used to render 3D models (Meshes or Voxel-models) using the JOGL rendering environment. It
 * currently has the following features:
 *
 * - Rendering of single Mesh or VoxelGrid
 * - Free positioning of the camera in terms of either Carthesian or Polar coordinate
 * - Snapshot of the rendered image can be obtained at any time.
 *
 * The class supports offscreen rendering and can be accessed by multipled Threads. However, the multithreading
 * model of JOGL requires a thread to retain() and release() the JOGLOffscreenRenderer before rendering anything
 * by calling the respective function.
 *
 * @see org.vitrivr.cineast.core.data.m3d.Mesh
 * @see org.vitrivr.cineast.core.data.m3d.VoxelGrid
 *
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class JOGLOffscreenRenderer {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Default GLProfile to be used. Should be GL2. */
    private static final GLProfile glprofile = GLProfile.get(GLProfile.GL2);

    /** GLCapabilities. Can be used to enable/disable hardware acceleration etc. */
    private static final GLCapabilities capabilities = new GLCapabilities(glprofile);

    /** OpenGL Utility Library reference */
    private final GLU glu;

    /** OpenGL context reference used for drawing. */
    private final GL2 gl;

    /** Width of the JOGLOffscreenRenderer in pixels. */
    private final int width;

    /** Height of the JOGLOffscreenRenderer in pixels. */
    private final int height;

    /** Aspect-ratio of the JOGLOffscreenRenderer. */
    private final float aspect;

    /** Polygon-mode used during rendering. */
    private int polygonmode = GL_FILL;

    /** Lock that makes sure that only a single Thread is using the classes rendering facility at a time. */
    private ReentrantLock lock = new ReentrantLock(true);

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
        /* Assign width and height. */
        this.width = width;
        this.height = height;
        this.aspect = (float) width / (float) height;

        /* Initialize GLOffscreenAutoDrawable. */
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glprofile);
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null,capabilities,null,width,height);
        drawable.display();

        /* Initialize GLU and GL2. */
        this.glu = new GLU();
        this.gl = drawable.getGL().getGL2();

        /* Set default color. */
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
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
     * @return Aspect ratio of the JOGLOffscreenRenderer.
     */
    public final float getAspect() {
        return aspect;
    }

    /**
     * Getter for polygonmode.
     *
     * @return Polygonmode for drawing, either GL_POINT, GL_LINE or GL_FILL.
     */
    public int getPolygonmode() {
        return polygonmode;
    }

    /**
     * Setter for polygonmode.
     *
     * @param polygonmode Polygonmode for drawing, either GL_POINT, GL_LINE or GL_FILL.
     */
    public synchronized void setPolygonmode(int polygonmode) {
        if (polygonmode == GL_POINT || polygonmode == GL_LINE || polygonmode == GL_FILL) {
            this.polygonmode = polygonmode;
        }
    }

    /**
     * Renders a mesh and positions the camera according to the specified, spherical coordinates so
     * that it faces the origin. Calling this methods is equivalent to calling clear(), draw() and positionCamera()
     * in this order.
     *
     * @param renderable Renderable object to be drawn.
     */
    public void render(Renderable renderable) {
        /* Clears buffers to preset-values. */
        this.clear();

        /* Draws the mesh. */
        this.draw(renderable);
    }

    /**
     * Draws a mesh in the buffer.
     *
     * @param renderable Renderable object to be drawn.
     */

    public void draw(Renderable renderable) {
        /* Check context. */
        if (!this.checkContext()) return;

        /* Switch matrix mode to modelview. */
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);
        gl.glLoadIdentity();

        /* Set drawing-style.*/
        gl.glPolygonMode(GL_FRONT_AND_BACK, this.polygonmode);

        /* Assemble and render model */
        int modelHandle = renderable.assemble(this.gl);
        gl.glCallList(modelHandle);
    }

    /**
     * Changes the positionCamera of the camera.
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks).
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks).
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks).
     */
    public final void positionCamera(double ex, double ey, double ez, double cx, double cy, double cz) {
        /* Check context. */
        if (!this.checkContext()) return;

        /* Switch matrix mode to projection. */
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        /* Set default perspective. */
        glu.gluPerspective(45.0f, this.aspect, 0.01f, 100.0f);

        /* Update camera position. */
        glu.gluLookAt(ex,ey,ez,cx,cy,cz,0.0,-1.0,0.0);
    }

    /**
     * Changes the positionCamera of the camera. This method makes sur, that camera always points towards
     * the origin [0,0,0]
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     */
    public final void positionCamera(double ex, double ey, double ez) {
        this.positionCamera(ex,ey,ez,0.0,0.0,0.0);
    }

    /**
     * Changes the positionCamera of the camera. This method makes sur, that camera always points towards
     * the origin [0,0,0]
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     */
    public final void positionCamera(float ex, float ey, float ez) {
        this.positionCamera(ex,ey,ez,0.0,0.0,0.0);
    }

    /**
     * Changes the positionCamera of the camera. The camera can be freely rotated around the origin [1,0,0] (cartesian
     * coordinates) and it can take any distance from that same origin.
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks).
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks).
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks).
     */
    public final void positionCamera(float ex, float ey, float ez, float cx, float cy, float cz) {
        this.positionCamera((double)ex,(double)ey,(double)ez,(double)cx,(double)cy,(double)cz);
    }

    /**
     * Changes the positionCamera of the camera. The camera can be freely rotated around the origin [1,0,0] (cartesian
     * coordinates) and it can take any distance from that same origin.
     *
     * @param r Distance of the camera from (0,0,0)
     * @param theta Polar angle of the camera (i.e. angle between vector and z-axis) in degree
     * @param phi z Azimut angle of the camera (i.e. angle between vector and x-axis) in degree
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks).
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks).
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks).
     */
    public final void positionCameraPolar(double r, double theta, double phi, double cx, double cy, double cz) {
        double theta_rad = Math.toRadians(theta);
        double phi_rad = Math.toRadians(phi);

        double x = r * Math.sin(theta_rad) * Math.cos(phi_rad);
        double y = r * Math.sin(theta_rad) * Math.sin(phi_rad);
        double z = r * Math.cos(theta_rad);

        positionCamera((float)x,(float)y,(float)z,cx,cy,cz);
    }

    /**
     * Changes the positionCamera of the camera. The camera can be freely rotated around the origin [1,0,0] (cartesian
     * coordinates) and it can take any distance from that same origin.
     *
     * @param r Distance of the camera from (0,0,0)
     * @param theta Polar angle of the camera (i.e. angle between vector and z-axis) in degree
     * @param phi z Azimut angle of the camera (i.e. angle between vector and x-axis) in degree
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks).
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks).
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks).
     */
    public final void positionCameraPolar(float r, float theta, float phi, float cx, float cy, float cz) {
        this.positionCamera((double)r,(double)theta,(double)phi, (double)cx,(double)cy,(double)cz);
    }

    /**
     * Clears buffers to preset-values.
     */
    public final void clear() {
        if (!this.checkContext()) return;
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Clears buffers to preset-values and applies a user-defined background colour.
     *
     * @param color The background colour to be used.
     */
    public void clear(Color color) {
        if (!this.checkContext()) return;
        gl.glClearColorIi(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Obtains and returns a BufferedImage in AWT orientation from the current JOGLOffscreenRenderer.
     *
     * @return BufferedImage containing a snapshot of the current render-buffer.
     */
    public final BufferedImage obtain() {
        /* Create and return a BufferedImage from buffer. */
        if (!this.checkContext()) return null;
        AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(gl.getGL2().getGLProfile(), false);
        return glReadBufferUtil.readPixelsToBufferedImage(gl.getGL2(), true);
    }

    /**
     * Makes the current thread try to retain the GLContext of this JOGLOffscreenRenderer. The
     * method returns true upon success and false otherwise.
     *
     * <b>Important: </b> Only one thread can retain a GLContext at a time. Relinquish the thread by
     * calling release().
     *
     * @return True if GLContext was retained and false otherwise.
     */
    public final boolean retain() {
        this.lock.lock();
        int result = this.gl.getContext().makeCurrent();
        if (result == CONTEXT_CURRENT_NEW || result == CONTEXT_CURRENT) {
            return true;
        } else {
            this.lock.unlock();
            LOGGER.error("Thread '{}' failed to retain JOGLOffscreenRenderer.", Thread.currentThread().getName());
            return false;
        }

    }

    /**
     * Makes the current thread release its ownership of the current JOGLOffscreenRenderer's GLContext.
     */
    public final void release() {
        if (this.checkContext()) {
            this.gl.getContext().release();
            this.lock.unlock();
        }
    }

    /**
     * Checks if the thread the GLContext is assigned to is equal to the Thread the current
     * code is being executed in.
     *
     * @return True if context-thread is equal to current thread and false otherwise,
     */
    private boolean checkContext() {
        if (!this.lock.isHeldByCurrentThread()) {
            LOGGER.error("Cannot access JOGLOffscreenRenderer because current thread '{}' does not own its GLContext.", Thread.currentThread().getName());
            return false;
        } else {
            return true;
        }
    }
}
