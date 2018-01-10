package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class MultimediaMetadataWriter extends AbstractBatchedEntityWriter<MultimediaMetadataDescriptor> {

    /**
     * @param writer
     * @param batchsize
     */
    public MultimediaMetadataWriter(PersistencyWriter<?> writer, int batchsize) {
        super(writer, batchsize, true);
    }

    /**
     *
     */
    @Override
    protected void init() {
        this.writer.setFieldNames(MultimediaMetadataDescriptor.FIELDNAMES);
        this.writer.open(MultimediaMetadataDescriptor.ENTITY);
    }

    /**
     * @param entity
     * @return
     */
    @Override
    protected PersistentTuple generateTuple(MultimediaMetadataDescriptor entity) {
        return this.writer.generateTuple(entity.getObjectId(), entity.getDomain(), entity.getKey(), entity.getValue().toString());
    }
}
