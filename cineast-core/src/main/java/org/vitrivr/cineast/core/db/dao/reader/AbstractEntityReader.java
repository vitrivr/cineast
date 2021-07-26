package org.vitrivr.cineast.core.db.dao.reader;

import org.vitrivr.cineast.core.db.DBSelector;

import java.io.Closeable;


public abstract class AbstractEntityReader implements Closeable {

    /** DBSelector instance used to perform the DB lookup. */
    protected final DBSelector selector;

    /**
     * Constructor for AbstractEntityReader
     *
     * @param selector DBSelector to use for the MediaObjectMetadataReader instance.
     */
    public AbstractEntityReader(DBSelector selector) {
        this.selector = selector;
    }

    /**
     * Closes the selector, relinquishing associated resources.
     */
    @Override
    public void close(){
        this.selector.close();
    }


}
