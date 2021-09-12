package org.vitrivr.cineast.core.db.dao.reader;

import org.apache.commons.lang3.tuple.Triple;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;


import java.util.*;
/**
 * Data access object that facilitates lookups in Cineast's for a Boolean Query.
 * Methods designed to return the number of elements or all elements for a Boolean Query
 */
public class BooleanReader extends AbstractEntityReader{


    private String entity;
    private String attribute;
    private List<PrimitiveTypeProvider> values = new ArrayList<>();
    private List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList;



    public BooleanReader(DBSelector selector, String entity, List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList) {
        super(selector);
        this.selector.open(entity);
        this.entity = entity;
        this.queryList = queryList;

    }

    public BooleanReader(DBSelector selector, String entity, String attribute) {
        super(selector);
        this.selector.open(entity);
        this.entity = entity;
        this.attribute = attribute;

    }


    /** Returns the total amount of elements in the database
     * * @return Number of elements(int) */
    public int getTotalElements() {
        selector.open(entity);
        System.out.println(selector.existsEntity(entity));
        List<PrimitiveTypeProvider> results = selector.getUniqueValues(queryList.get(0).getLeft());
        System.out.println(results.size());
        return results.size();
    }
    /** Returns the number of elements to be returned for a Boolean Query
     * * @return Number of elements(int) */
    public int getElementsAND() {
        List<Map<String, PrimitiveTypeProvider>> results = selector.getRowsAND(this.queryList,null, Arrays.asList(),null);
        return results.size();
    }

    /** Returns all values for an attribute in an entity
     * * @return All elements for an attribute */
    public List<PrimitiveTypeProvider> getAllValues() {
        List<PrimitiveTypeProvider> results = selector.getAll(this.attribute);
        return results;
    }
}