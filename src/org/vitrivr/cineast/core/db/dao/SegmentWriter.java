package org.vitrivr.cineast.core.db.dao;

import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class SegmentWriter {
    /** PersistencyWriter instance used to persist changes. */
    private PersistencyWriter<?> writer;

    /**
     * Default constructor.
     */
    public SegmentWriter(PersistencyWriter<?> writer) {
        this.writer = writer;
        this.writer.setFieldNames(SegmentDescriptor.FIELDNAMES);
        this.writer.open(SegmentDescriptor.ENTITY);
    }

    /**
     * Persists the provided SegmentDescriptor in the database.
     *
     * @param descriptor SegmentDescriptor to persist
     */
    public void write(SegmentDescriptor descriptor) {
        PersistentTuple tuple = this.writer.generateTuple(descriptor.getSegmentId(), descriptor.getObjectId(), descriptor.getSequenceNumber(), descriptor.getStart(), descriptor.getEnd());
        this.writer.persist(tuple);
    }

    /**
     * Closes the writer.
     */
    public void close() {
        this.writer.close();
    }

    public void finalize() {
        this.close();
    }

}
