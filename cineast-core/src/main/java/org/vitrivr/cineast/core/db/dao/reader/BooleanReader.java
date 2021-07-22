package org.vitrivr.cineast.core.db.dao.reader;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;

import java.util.List;

public class BooleanReader extends AbstractEntityReader{


    private String entity;
    private String attribute;

    public BooleanReader(DBSelector selector, String entity, String attribute) {
        super(selector);
        this.selector.open(MediaObjectMetadataDescriptor.ENTITY);
        this.entity = entity;
        this.attribute = attribute;

    }


    /** Returns the total amount of elements in the database*/
    public int getTotalElements() {
        selector.open(entity);
        System.out.println(selector.existsEntity(entity));
        List<PrimitiveTypeProvider> results = selector.getAll(attribute);
        System.out.println(selector.getAll("key"));
        System.out.println(results.size());
        return results.size();
    }
    /** Returns the total amount of elements for a specific boolean value*/
   /* public int getElementsforAttribute{}*/
}
