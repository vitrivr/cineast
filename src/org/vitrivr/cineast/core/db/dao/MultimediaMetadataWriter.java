package org.vitrivr.cineast.core.db.dao;

import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class MultimediaMetadataWriter {
    /** PersistencyWriter instance used to persist changes. */
    private PersistencyWriter<?> writer;

    /**
     * Default constructor.
     */
    public MultimediaMetadataWriter(PersistencyWriter<?> writer) {
        this.writer = writer;
        this.writer.setFieldNames(MultimediaMetadataDescriptor.FIELDNAMES);
        this.writer.open(MultimediaMetadataDescriptor.ENTITY);
    }

    /**
     * Persists the provided MultimediaMetadataDescriptor in the database.
     *
     * @param descriptor MultimediaMetadataDescriptor to persist
     */
    public void write(MultimediaMetadataDescriptor descriptor) {
        PersistentTuple tuple = this.writer.generateTuple(descriptor.getObjectId(), descriptor.getKey(), descriptor.getValue());
        this.writer.persist(tuple);
    }

    /**
     * Closes the writer.
     */
    public void close() {
        this.writer.close();
    }

    /**
     *
     */
    public void finalize() {
        this.close();
    }
}
