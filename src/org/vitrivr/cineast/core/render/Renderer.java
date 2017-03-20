package org.vitrivr.cineast.core.render;

import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This interface defines methods that a renderer for 3D models (e.g. Meshes or Voxels must implement). It
 * currently provides the following features:
 *
 * - Rendering of single Mesh or VoxelGrid
 * - Free positioning of the camera in terms of either cartesian or polar coordinate
 * - Snapshot of the rendered image can be obtained at any time.
 *
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public interface Renderer {
    /**
     * Renders the scene of the renderable using all the configured objects
     * and the configured camera position.
     */
    void render();

    /**
     * Assembles a new Mesh object and thereby adds it to the list of objects that
     * should be rendered.
     *
     * @param mesh Mesh that should be rendered
     */
    void assemble(Mesh mesh);

    /**
     * Assembles a new VoxelGrid object and thereby adds it to the list of objects that
     * should be rendered.
     *
     * @param grid VoxelGrid that should be rendered.
     */
    void assemble(VoxelGrid grid);

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
    void positionCamera(double ex, double ey, double ez, double cx, double cy, double cz);

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
    default void positionCamera(float ex, float ey, float ez, float cx, float cy, float cz) {
        this.positionCamera((double)ex,(double)ey,(double)ez,(double)cx,(double)cy,(double)cz);
    }

    /**
     * Changes the positionCamera of the camera. This method should make sure, that camera always points
     * towards the origin [0,0,0]
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     */
    default void positionCamera(double ex, double ey, double ez) {
        this.positionCamera(ex, ey, ez, 0.0,0.0,0.0);
    }

    /**
     * Changes the positionCamera of the camera. This method should make sure, that camera always points
     * towards the origin [0,0,0]
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     */
    default void positionCamera(float ex, float ey, float ez) {
        this.positionCamera(ex, ey, ez, 0.0f,0.0f,0.0f);
    }

    /**
     * Changes the positionCamera of the camera. The camera can be freely rotated around the origin [0,0,0]
     * (cartesian coordinates) and it can take any distance from that same origin.
     *
     * @param r Distance of the camera from the origin at [0,0,0]
     * @param theta Polar angle of the camera (i.e. angle between vector and z-axis) in degree
     * @param phi z Azimut angle of the camera (i.e. angle between vector and x-axis) in degree
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks).
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks).
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks).
     */
    default void positionCameraPolar(double r, double theta, double phi, double cx, double cy, double cz) {
        double theta_rad = Math.toRadians(theta);
        double phi_rad = Math.toRadians(phi);

        double x = r * Math.sin(theta_rad) * Math.cos(phi_rad);
        double y = r * Math.sin(theta_rad) * Math.sin(phi_rad);
        double z = r * Math.cos(theta_rad);

        positionCamera((float)x,(float)y,(float)z,cx,cy,cz);
    }

    /**
     * Changes the positionCamera of the camera. The camera can be freely rotated around the origin
     * [0,0,0] (cartesian coordinates) and it can take any distance from that same origin.
     *
     * @param r Distance of the camera from (0,0,0)
     * @param theta Polar angle of the camera (i.e. angle between vector and z-axis) in degree
     * @param phi z Azimut angle of the camera (i.e. angle between vector and x-axis) in degree
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks) in cartesian coordinates.
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks) in cartesian coordinates.
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks) in cartesian coordinates.
     */
    default void positionCameraPolar(float r, float theta, float phi, float cx, float cy, float cz) {
        this.positionCamera((double)r,(double)theta,(double)phi, (double)cx,(double)cy,(double)cz);
    }

    /**
     * Obtains and returns a BufferedImage from the current Renderer.
     *
     * @return BufferedImage containing a snapshot of the current render-buffer.
     */
    BufferedImage obtain();

    /**
     * Clears buffers to preset-values and applies a user-defined background colour.
     *
     * @param color The background colour to be used.
     */
    void clear(Color color);

    /**
     * Clears buffers to preset-values.
     */
    void clear();

    /**
     * Retains control of the Renderer. While a Thread retains a renderer, no other thread should
     * be allowed to use it!
     */
    boolean retain();

    /**
     * Releases control of the Renderer, making it usable by other Threads again.
     */
    void release();
}
