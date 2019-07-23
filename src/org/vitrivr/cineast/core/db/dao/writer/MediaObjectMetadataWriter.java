package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class MediaObjectMetadataWriter extends AbstractBatchedEntityWriter<MediaObjectMetadataDescriptor> {

    /**
     * @param writer
     * @param batchsize
     */
    public MediaObjectMetadataWriter(PersistencyWriter<?> writer, int batchsize) {
        super(writer, batchsize, true);
    }

    /**
     *
     */
    @Override
    protected void init() {
        this.writer.setFieldNames(MediaObjectMetadataDescriptor.FIELDNAMES);
        this.writer.open(MediaObjectMetadataDescriptor.ENTITY);
    }

    /**
     * @param entity
     * @return
     */
    @Override
    protected PersistentTuple generateTuple(MediaObjectMetadataDescriptor entity) {
        return this.writer.generateTuple(entity.getObjectId(), entity.getDomain(), entity.getKey(),
            entity.getValue());
    }
}
