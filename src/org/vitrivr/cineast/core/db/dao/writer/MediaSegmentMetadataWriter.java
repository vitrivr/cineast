package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MediaSegmentMetadataWriter extends AbstractBatchedEntityWriter<MediaSegmentMetadataDescriptor> {

    public MediaSegmentMetadataWriter(PersistencyWriter<?> writer, int batchsize) {
        super(writer, batchsize, true);
    }

    @Override
    protected void init() {
        this.writer.setFieldNames(MediaSegmentMetadataDescriptor.FIELDNAMES);
        this.writer.open(MediaSegmentMetadataDescriptor.ENTITY);
    }

    @Override
    public PersistentTuple generateTuple(MediaSegmentMetadataDescriptor entity) {
        return this.writer.generateTuple(entity.getSegmentId(), entity.getDomain(), entity.getKey(),
                entity.getValue());
    }

}
