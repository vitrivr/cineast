package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;



public class MediaObjectWriter extends AbstractBatchedEntityWriter<MediaObjectDescriptor> {
    public MediaObjectWriter(PersistencyWriter<?> writer) {
        super(writer, 1, true);
    }

    /**
     *
     */
    @Override
    protected void init() {
        this.writer.setFieldNames(MediaObjectDescriptor.FIELDNAMES);
        this.writer.open(MediaObjectDescriptor.ENTITY);
    }

    @Override
    protected PersistentTuple generateTuple(MediaObjectDescriptor entity) {
        return this.writer.generateTuple(entity.getObjectId(), entity.getMediatypeId(), entity.getName(), entity.getPath());
    }
}
