package org.vitrivr.cineast.core.data.query.containers;

import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.util.mesh.MeshTransformUtil;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.MeshParser;

import java.awt.image.BufferedImage;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.03.17
 */
public class ModelQueryContainer extends QueryContainer {

    /** Original Mesh as transferred by the client. */
    private final Mesh mesh;

    /** KHL transformed version of the original Mesh. */
    private final Mesh normalizedMesh;

    /** Image containing a 2D sketch of the 3D model in question. */
    private final MultiImage image;

    /**
     * Constructs an {@link ModelQueryContainer} from base 64 encoded JSON data. The constructor assumes either the JSV4 JSON format
     * for Meshes OR a valid image (for 2D sketch to 3D model lookup).
     *
     * @param data The 3D model data that should be converted.
     * @param factory The {@link MultiImageFactory} used to create images.
     */
    public ModelQueryContainer(String data, MultiImageFactory factory) {
        if (MeshParser.isValidThreeJSV4Geometry(data)) {
            this.mesh = MeshParser.parseThreeJSV4Geometry(data);
            this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
            this.image = MultiImage.EMPTY_MULTIIMAGE;
        } else if (ImageParser.isValidImage(data)) {
            final BufferedImage img = ImageParser.dataURLtoBufferedImage(data);
            this.image = factory.newMultiImage(img);
            this.mesh = Mesh.EMPTY;
            this.normalizedMesh = Mesh.EMPTY;
        } else {
            throw new IllegalArgumentException("The provided data could not be converted to a Mesh.");
        }
    }

    /**
     * Constructor for {@link ModelQueryContainer} with a Mesh. Used for Query-by-Example.
     *
     * @param mesh Mesh for which to create a {@link ModelQueryContainer}.
     */
    public ModelQueryContainer(Mesh mesh) {
        this.mesh = new Mesh(mesh);
        this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
        this.image = MultiImage.EMPTY_MULTIIMAGE;
    }

    /**
     * Constructor for {@link ModelQueryContainer} with ab image. Used for Query-by-Sketch (2d sketch to 3d model).
     *
     * @param image BufferedImage for which to create a {@link ModelQueryContainer}.
     */
    public ModelQueryContainer(BufferedImage image, MultiImageFactory factory) {
        this.image = factory.newMultiImage(image);
        this.mesh = Mesh.EMPTY;
        this.normalizedMesh = Mesh.EMPTY;
    }

    /**
     * Constructor for {@link ModelQueryContainer} constructor with an image (treated as 2D sketch). Used for Query-by-2D-Sketch.
     *
     * @param image Image for which to create a {@link ModelQueryContainer}.
     */
    public ModelQueryContainer(MultiImage image) {
        this.image = image;
        this.mesh = Mesh.EMPTY;
        this.normalizedMesh = Mesh.EMPTY;
    }

    @Override
    public Mesh getMesh() {
        return this.mesh;
    }

    @Override
    public Mesh getNormalizedMesh() {
        return this.normalizedMesh;
    }

    @Override
    public MultiImage getAvgImg() {
        return this.image;
    }

    @Override
    public MultiImage getMedianImg() {
        return this.image;
    }
}
