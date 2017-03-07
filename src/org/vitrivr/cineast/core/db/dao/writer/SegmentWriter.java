package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class SegmentWriter extends AbstractBatchedEntityWriter<SegmentDescriptor> {
    /**
     * @param writer
     */
    public SegmentWriter(PersistencyWriter<?> writer) {
        super(writer, 1, true);
    }

    /**
     * @param writer
     * @param batchsize
     */
    public SegmentWriter(PersistencyWriter<?> writer, int batchsize) {
        super(writer, batchsize, true);
    }

    /**
     *
     */
    @Override
    protected void init() {
        this.writer.setFieldNames(SegmentDescriptor.FIELDNAMES);
        this.writer.open(SegmentDescriptor.ENTITY);
    }

    /**
     *
     * @param entity
     * @return
     */
    @Override
    protected PersistentTuple generateTuple(SegmentDescriptor entity) {
        return this.writer.generateTuple(entity.getSegmentId(), entity.getObjectId(), entity.getSequenceNumber(), entity.getStart(), entity.getEnd(), entity.getStartabs(), entity.getEndabs());
    }
}
