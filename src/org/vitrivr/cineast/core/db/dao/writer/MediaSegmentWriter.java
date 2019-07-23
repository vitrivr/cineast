package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class MediaSegmentWriter extends AbstractBatchedEntityWriter<MediaSegmentDescriptor> {
    /**
     * @param writer
     */
    public MediaSegmentWriter(PersistencyWriter<?> writer) {
        super(writer, 1, true);
    }

    /**
     * @param writer
     * @param batchsize
     */
    public MediaSegmentWriter(PersistencyWriter<?> writer, int batchsize) {
        super(writer, batchsize, true);
    }

    /**
     *
     */
    @Override
    protected void init() {
        this.writer.setFieldNames(MediaSegmentDescriptor.FIELDNAMES);
        this.writer.open(MediaSegmentDescriptor.ENTITY);
    }

    /**
     *
     * @param entity
     * @return
     */
    @Override
    protected PersistentTuple generateTuple(MediaSegmentDescriptor entity) {
        return this.writer.generateTuple(entity.getSegmentId(), entity.getObjectId(), entity.getSequenceNumber(), entity.getStart(), entity.getEnd(), entity.getStartabs(), entity.getEndabs());
    }
}
