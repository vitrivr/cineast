package org.vitrivr.cineast.core.db.dao;

import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class MultimediaObjectWriter {

    /** PersistencyWriter instance used to persist changes. */
    private PersistencyWriter<?> writer;

    /**
     * Default constructor.
     */
    public MultimediaObjectWriter(PersistencyWriter<?> writer) {
        this.writer = writer;
        this.writer.setFieldNames(MultimediaObjectDescriptor.FIELDNAMES);
        this.writer.open(MultimediaObjectDescriptor.ENTITY);
    }

    /**
     * Persists the provided MultimediaObjectDescriptor in the database.
     *
     * @param descriptor MultimediaObjectDescriptor to persist
     */
    public void write(MultimediaObjectDescriptor descriptor) {
        PersistentTuple tuple = this.writer.generateTuple(descriptor.getObjectId(), descriptor.getMediatypeId(), descriptor.getName(), descriptor.getPath());
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
