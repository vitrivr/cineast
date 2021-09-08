package org.vitrivr.cineast.core.db.dao.reader;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.abstracts.BooleanRetriever;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.*;

public class BooleanReader extends AbstractEntityReader{


    private String entity;
    private String attribute;
    private List<PrimitiveTypeProvider> values = new ArrayList<>();
    private RelationalOperator operator;
    private List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList;
    private BooleanRetriever retriever;



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
    public BooleanReader(DBSelector selector, String entity, String attribute, BooleanRetriever retriever) {
        super(selector);
        this.selector.open(entity);
        this.entity = entity;
        this.attribute = attribute;
        this.retriever = retriever;

    }



    /** Returns the total amount of elements in the database*/
    public int getTotalElements() {
        selector.open(entity);
        System.out.println(selector.existsEntity(entity));
        List<PrimitiveTypeProvider> results = selector.getUniqueValues(queryList.get(0).getLeft());
        System.out.println(results.size());
        return results.size();
    }
    /** Returns the total amount of elements for a specific boolean value
     * @return*/

    public int getElementsForAttribute() {
        List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(attribute, this.operator, PrimitiveTypeProvider.fromObject(this.values));
        return results.size();
    }

    public int getElementsAND() {
        List<Map<String, PrimitiveTypeProvider>> results = selector.getRowsAND(this.queryList,null, Arrays.asList(),null);
        return results.size();
    }


    public List<PrimitiveTypeProvider> getAllValues() {
        List<PrimitiveTypeProvider> results = selector.getAll(this.attribute);
        return results;
    }
}