package org.vitrivr.cineast.core.data.query.containers;

import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.mesh.MeshTransformUtil;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.03.17
 */
public class ModelQueryContainer implements QueryContainer {

    /** Original Mesh as transferred by the client. */
    private final Mesh mesh;

    /** KHL transformed version of the original Mesh. */
    private final Mesh normalizedMesh;

    /** Image containing a 2D sketch of the 3D model in question. */
    private final MultiImage image;

    /** Weight used for relevance feedback */
    private float weight = 1.0f;

    /**
     * ModelQueryContainer constructor with a Mesh. Used for Query-by-Example.
     *
     * @param mesh Mesh for which to create a ModelQueryContainer.
     */
    public ModelQueryContainer(Mesh mesh) {
        this.mesh = new Mesh(mesh);
        this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
        this.image = MultiImage.EMPTY_MULTIIMAGE;
    }

    /**
     * ModelQueryContainer constructor with an image (treated as 2D sketch). Used
     * for Query-by-2D-Sketch.
     *
     * @param image Image for which to create a ModelQueryContainer.
     */
    public ModelQueryContainer(MultiImage image) {
        this.image = image;
        this.mesh = Mesh.EMPTY;
        this.normalizedMesh = Mesh.EMPTY;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getSuperId() {
        return null;
    }

    @Override
    public void setId(String id) {}

    @Override
    public void setSuperId(String id) {}

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

    public float getWeight(){
        return this.weight;
    }

    public void setWeight(float weight){
        if(Float.isNaN(weight)){
            this.weight = 0f;
            return;
        }
        this.weight = MathHelper.limit(weight, -1f, 1f);
    }
}
