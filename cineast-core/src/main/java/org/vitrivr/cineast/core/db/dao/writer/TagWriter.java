package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;

import java.io.Closeable;

public class TagWriter implements Closeable {

    private final PersistencyWriter<?> writer;

    public TagWriter(PersistencyWriter<?> writer) {
        this.writer = writer;


        if (this.writer == null) {
            throw new NullPointerException("writer cannot be null");
        }

        this.writer.open(TagReader.TAG_ENTITY_NAME);
        this.writer.setFieldNames(TagReader.TAG_ID_COLUMNNAME, TagReader.TAG_NAME_COLUMNNAME, TagReader.TAG_DESCRIPTION_COLUMNNAME);
    }

    /**
     * Adds and persist a new {@link Tag} entry.
     *
     * @param id ID of the new entry.
     * @param name Name of the new entry.
     * @param description Description of the new entry.
     * @return True on success, false otherwise.
     */
    public boolean addTag(String id, String name, String description) {
        return this.writer.persist(this.writer.generateTuple(id, name, description));
    }

    /**
     * Adds and persist a new {@link Tag} entry.
     *
     * @param tag {@link Tag} that should be added.
     */
    public boolean addTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        return addTag(tag.getId(), tag.getName(), tag.getDescription());
    }

    @Override
    public void close() {
        this.writer.close();
    }

}
