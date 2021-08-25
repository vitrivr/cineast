package org.vitrivr.cineast.core.db.polypheny;

import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

import java.util.List;

public final class PolyphenyWriter implements PersistencyWriter<Object> {

    /** Internal reference to the {@link PolyphenyWrapper} used by this {@link PolyphenyWriter}. */
    private final PolyphenyWrapper wrapper;

    public PolyphenyWriter(PolyphenyWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean open(String name) {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean idExists(String id) {
        return false;
    }

    @Override
    public boolean exists(String key, String value) {
        return false;
    }

    @Override
    public PersistentTuple generateTuple(Object... objects) {
        return null;
    }

    @Override
    public boolean persist(PersistentTuple tuple) {
        return false;
    }

    @Override
    public void setFieldNames(String... names) {

    }

    @Override
    public boolean persist(List<PersistentTuple> tuples) {
        return false;
    }

    @Override
    public Object getPersistentRepresentation(PersistentTuple tuple) {
        return null;
    }
}
