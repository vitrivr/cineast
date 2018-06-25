package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class MultimediaObjectWriter extends AbstractBatchedEntityWriter<MultimediaObjectDescriptor> {
    /**
     * @param writer
     */
    public MultimediaObjectWriter(PersistencyWriter<?> writer) {
        super(writer, 1, true);
    }

    /**
     *
     */
    @Override
    protected void init() {
        this.writer.setFieldNames(MultimediaObjectDescriptor.FIELDNAMES);
        this.writer.open(MultimediaObjectDescriptor.ENTITY);
    }

    /**
     * @param entity
     * @return
     */
    @Override
    protected PersistentTuple generateTuple(MultimediaObjectDescriptor entity) {
        return this.writer.generateTuple(entity.getObjectId(), entity.getMediatypeId(), entity.getName(), entity.getPath());
    }
}
