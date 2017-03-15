package org.vitrivr.cineast.core.data.query.containers;

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

    /** */
    private float weight = 1.0f;

    /**
     *
     * @param mesh
     */
    public ModelQueryContainer(Mesh mesh) {
        this.mesh = new Mesh(mesh);
        this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
    }


    /**
     * @return a unique id of this
     */
    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getSuperId() {
        return null;
    }

    /**
     * @param id
     * @return a unique id of this
     */
    @Override
    public void setId(String id) {

    }

    /**
     * @param id
     */
    @Override
    public void setSuperId(String id) {

    }

    /**
     *
     * @return
     */
    public Mesh getMesh() {
        return this.mesh;
    }

    /**
     *
     * @return
     */
    public Mesh getNormalizedMesh() {
        return this.normalizedMesh;
    }

    /**
     * weight used for relevance feedback
     */
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
