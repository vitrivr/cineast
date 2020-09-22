package org.vitrivr.cineast.core.render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class can be used to render 3D models (Meshes or Voxel-models) using the JOGL rendering environment. It
 * currently has the following features:
 *
 * - Rendering of single Mesh or VoxelGrid
 * - Free positioning of the camera in terms of either cartesian or polar coordinate
 * - Snapshot of the rendered image can be obtained at any time.
 *
 * The class supports offscreen rendering and can be accessed by multipled Threads. However, the multithreading
 * model of JOGL requires a thread to retain() and release() the JOGLOffscreenRenderer before rendering anything
 * by calling the respective function.
 *
 * @see Mesh
 * @see VoxelGrid
 *
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class JOGLOffscreenRenderer implements Renderer {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Default GLProfile to be used. Should be GL2. */
    private static final GLProfile GL_PROFILE = GLProfile.get(GLProfile.GL2);

    /** GLCapabilities. Can be used to enable/disable hardware acceleration etc. */
    private static final GLCapabilities GL_CAPABILITIES = new GLCapabilities(GL_PROFILE);

    /** OpenGL Utility Library reference */
    private final GLU glu;

    /** OpenGL drawable reference. */
    private final GLOffscreenAutoDrawable drawable;

    /** OpenGL context reference used for drawing. */
    private final GL2 gl;

    /** Width of the JOGLOffscreenRenderer in pixels. */
    private final int width;

    /** Height of the JOGLOffscreenRenderer in pixels. */
    private final int height;

    /** Aspect-ratio of the JOGLOffscreenRenderer. */
    private final float aspect;

    /** Polygon-mode used during rendering. */
    private int polygonmode = GL2GL3.GL_FILL;

    /** Lock that makes sure that only a single Thread is using the classes rendering facility at a time. */
    private ReentrantLock lock = new ReentrantLock(true);

    /** List of object handles that should be rendered. */
    private final List<Integer> objects = new ArrayList<>();

    /*
     * This code-block can be used to configure the off-screen renderer's GL_CAPABILITIES.
     */
    static {
        GL_CAPABILITIES.setOnscreen(false);
        GL_CAPABILITIES.setHardwareAccelerated(true);
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
        GLDrawableFactory factory = GLDrawableFactory.getFactory(GL_PROFILE);
        this.drawable = factory.createOffscreenAutoDrawable(null, GL_CAPABILITIES,null,width,height);
        this.drawable.display();

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
        if (polygonmode == GL2GL3.GL_POINT || polygonmode == GL2GL3.GL_LINE || polygonmode == GL2GL3.GL_FILL) {
            this.polygonmode = polygonmode;
        }
    }

    /**
     *
     */
    @Override
    public void render() {
        /* Clear context. */
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        /* Switch matrix mode to modelview. */
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glLoadIdentity();
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, this.polygonmode);

        /* Call list. */
        for (Integer handle : this.objects) {
            gl.glCallList(handle);
        }
    }

    /**
     * Renders a new Mesh object and thereby removes any previously rendered object
     *
     * @param mesh Mesh that should be rendered
     */
    @Override
    public void assemble(ReadableMesh mesh) {
        int meshList = gl.glGenLists(1);
        this.objects.add(meshList);
        gl.glNewList(meshList, GL2.GL_COMPILE);
        {
            for (Mesh.Face face : mesh.getFaces()) {
                /* Extract normals and vertices. */
                List<Mesh.Vertex> vertices = face.getVertices();

                /* Determine gl_draw_type. */
                int gl_draw_type = GL.GL_TRIANGLES;
                if (face.getType() == Mesh.FaceType.QUAD) {
                  gl_draw_type = GL2ES3.GL_QUADS;
                }

                /* Drawing is handled differently depending on whether its a TRI or QUAD mesh. */
                gl.glBegin(gl_draw_type);
                {
                    for (Mesh.Vertex vertex : vertices) {
                        gl.glColor3f(vertex.getColor().x(), vertex.getColor().y(), vertex.getColor().z());
                        gl.glVertex3f(vertex.getPosition().x(), vertex.getPosition().y(), vertex.getPosition().z());
                        gl.glNormal3f(vertex.getNormal().x(), vertex.getNormal().y(), vertex.getNormal().z());
                    }
                }
                gl.glEnd();
            }
        }
        gl.glEndList();
    }

    /**
     * Assembles a new VoxelGrid object and thereby adds it to the list of objects that
     * should be rendered.
     *
     * @param grid VoxelGrid that should be rendered.
     */
    @Override
    public void assemble(VoxelGrid grid) {
        int meshList = gl.glGenLists(1);
        this.objects.add(meshList);
        gl.glNewList(meshList, GL2.GL_COMPILE);
        {
            boolean[] visible = {true, true, true, true, true, true};

            for (int i = 0; i < grid.getSizeX(); i++) {
                for (int j = 0; j < grid.getSizeY(); j++) {
                    for (int k = 0; k < grid.getSizeZ(); k++) {
                        /* Skip Voxel if its inactive. */
                        if (grid.get(i,j,k) == VoxelGrid.Voxel.INVISIBLE) {
                          continue;
                        }

                        Vector3f voxelCenter = grid.getVoxelCenter(i,j,k);

                        /* Extract center of the voxel. */
                        float x = voxelCenter.x;
                        float y = voxelCenter.y;
                        float z = voxelCenter.z;

                        /* Determine which faces to draw: Faced that are covered by another active voxel are switched off. */
                        if(i > 0) {
                          visible[0] = !grid.isVisible(i-1,j,k);
                        }
                        if(i < grid.getSizeX()-1) {
                          visible[1] = !grid.isVisible(i+1,j,k);
                        }
                        if(j > 0) {
                          visible[2] = !grid.isVisible(i,j-1,k);
                        }
                        if(j < grid.getSizeY()-1) {
                          visible[3] = !grid.isVisible(i,j+1,k);
                        }
                        if(k > 0) {
                          visible[4] = !grid.isVisible(i,j,k-1);
                        }
                        if(k < grid.getSizeZ()-1) {
                          visible[5] = !grid.isVisible(i,j,k+1);
                        }

                        /* Draw the cube. */
                        gl.glBegin(GL2ES3.GL_QUADS);
                        {
                            final float halfresolution = grid.getResolution()/2.0f;

                            /* 1 */
                            if (visible[0]) {
                                gl.glVertex3f(x + halfresolution, y - halfresolution, z - halfresolution);
                                gl.glVertex3f(x - halfresolution, y - halfresolution, z - halfresolution);
                                gl.glVertex3f(x - halfresolution, y + halfresolution, z - halfresolution);
                                gl.glVertex3f(x + halfresolution, y + halfresolution, z - halfresolution);
                            }

                            /* 2 */
                            if (visible[1]) {
                                gl.glVertex3f(x - halfresolution, y - halfresolution, z + halfresolution);
                                gl.glVertex3f(x + halfresolution, y - halfresolution, z + halfresolution);
                                gl.glVertex3f(x + halfresolution, y + halfresolution, z + halfresolution);
                                gl.glVertex3f(x - halfresolution, y + halfresolution, z + halfresolution);
                            }

                            /* 3 */
                            if (visible[2]) {
                                gl.glVertex3f(x + halfresolution, y - halfresolution, z + halfresolution);
                                gl.glVertex3f(x + halfresolution, y - halfresolution, z - halfresolution);
                                gl.glVertex3f(x + halfresolution, y + halfresolution, z - halfresolution);
                                gl.glVertex3f(x + halfresolution, y + halfresolution, z + halfresolution);
                            }

                            /* 4 */
                            if (visible[3]) {
                                gl.glVertex3f(x - halfresolution, y - halfresolution, z - halfresolution);
                                gl.glVertex3f(x - halfresolution, y - halfresolution, z + halfresolution);
                                gl.glVertex3f(x - halfresolution, y + halfresolution, z + halfresolution);
                                gl.glVertex3f(x - halfresolution, y + halfresolution, z - halfresolution);
                            }

                            /* 5 */
                            if (visible[4]) {
                                gl.glVertex3f(x - halfresolution, y - halfresolution, z - halfresolution);
                                gl.glVertex3f(x + halfresolution, y - halfresolution, z - halfresolution);
                                gl.glVertex3f(x + halfresolution, y - halfresolution, z + halfresolution);
                                gl.glVertex3f(x - halfresolution, y - halfresolution, z + halfresolution);
                            }

                            /* 6 */
                            if (visible[5]) {
                                gl.glVertex3f(x + halfresolution, y + halfresolution, z - halfresolution);
                                gl.glVertex3f(x - halfresolution, y + halfresolution, z - halfresolution);
                                gl.glVertex3f(x - halfresolution, y + halfresolution, z + halfresolution);
                                gl.glVertex3f(x + halfresolution, y + halfresolution, z + halfresolution);
                            }
                        }
                        gl.glEnd();
                    }
                }
            }
        }
        gl.glEndList();
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
     * @param upx x-direction of the camera's UP position.
     * @param upy y-direction of the camera's UP position.
     * @param upz z-direction of the camera's UP position.
     */
    @Override
    public final void positionCamera(double ex, double ey, double ez, double cx, double cy, double cz, double upx, double upy, double upz) {
        /* Check context. */
        if (!this.checkContext()) {
          return;
        }

        /* Switch matrix mode to projection. */
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();

        /* Set default perspective. */
        glu.gluPerspective(45.0f, this.aspect, 0.01f, 100.0f);

        /* Update camera position. */
        glu.gluLookAt(ex,ey,ez,cx,cy,cz,upx,upy,upz);
    }

    /**
     * Clears buffers to preset-values.
     */
    @Override
    public final void clear() {
        this.clear(Color.BLACK);
    }

    /**
     * Clears buffers to preset-values and applies a user-defined background colour.
     *
     * @param color The background colour to be used.
     */
    @Override
    public void clear(Color color) {
        if (!this.checkContext()) {
          return;
        }
        for (Integer handle : this.objects) {
            gl.glDeleteLists(handle, 1);
        }
        gl.glClearColorIi(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        this.objects.clear();
    }

    /**
     * Obtains and returns a BufferedImage in AWT orientation from the current JOGLOffscreenRenderer.
     *
     * @return BufferedImage containing a snapshot of the current render-buffer.
     */
    @Override
    public final BufferedImage obtain() {
        /* Create and return a BufferedImage from buffer. */
        if (!this.checkContext()) {
          return null;
        }
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
    @Override
    public final boolean retain() {
        this.lock.lock();
        int result = this.gl.getContext().makeCurrent();
        if (result == GLContext.CONTEXT_CURRENT_NEW || result == GLContext.CONTEXT_CURRENT) {
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
    @Override
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
