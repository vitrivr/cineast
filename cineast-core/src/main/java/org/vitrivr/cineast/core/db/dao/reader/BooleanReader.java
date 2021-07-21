package org.vitrivr.cineast.core.db.dao.reader;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;

import java.util.List;

public class BooleanReader extends AbstractEntityReader{
    public BooleanReader(DBSelector selector) {
        super(selector);
        this.selector.open(MediaObjectMetadataDescriptor.ENTITY);

    }


    /** Returns the total amount of elements in the database*/
    public int getTotalElements() {
        String entity = "test_table"; /*some random entity*/
        selector.open(entity);
        System.out.println(selector.existsEntity(entity));
        List<PrimitiveTypeProvider> z = selector.getAll("id");
        System.out.println(selector.getAll("key"));
        System.out.println("hello");
    }
    /** Returns the total amount of elements for a specific boolean value*/
    public int getElementsforAttribute
}
