package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MultimediaMetadataSegmentDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MultimediaMetadataSegmentWriter extends AbstractBatchedEntityWriter<MultimediaMetadataSegmentDescriptor> {

    public MultimediaMetadataSegmentWriter(PersistencyWriter<?> writer, int batchsize) {
        super(writer, batchsize, true);
    }

    @Override
    protected void init() {
        this.writer.setFieldNames(MultimediaMetadataSegmentDescriptor.FIELDNAMES);
        this.writer.open(MultimediaMetadataSegmentDescriptor.ENTITY);
    }

    @Override
    protected PersistentTuple generateTuple(MultimediaMetadataSegmentDescriptor entity) {
        return this.writer.generateTuple(entity.getSegmentId(), entity.getDomain(), entity.getKey(),
                entity.getValue());
    }

}
