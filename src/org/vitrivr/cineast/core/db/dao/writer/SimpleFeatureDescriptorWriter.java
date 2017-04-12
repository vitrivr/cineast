package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.SimpleFeatureDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 28.01.17
 */
public class SimpleFeatureDescriptorWriter extends AbstractBatchedEntityWriter<SimpleFeatureDescriptor>  {

    private final String entityname;

    /**
     * @param writer
     * @param batchsize
     */
    public SimpleFeatureDescriptorWriter(PersistencyWriter<?> writer, String entityname, int batchsize) {
        super(writer, batchsize, false);
        this.entityname = entityname;
        this.init();
    }

    /**
     *
     */
    @Override
    public void init() {
       this.writer.open(this.entityname);
    }

    /**
     * @param entity
     * @return
     */
    @Override
    protected PersistentTuple generateTuple(SimpleFeatureDescriptor entity) {
        float[] array = new float[ entity.getFeature().getElementCount()];
        entity.getFeature().toArray(array);
        return this.writer.generateTuple(entity.getSegmentId(), array);
    }
}
