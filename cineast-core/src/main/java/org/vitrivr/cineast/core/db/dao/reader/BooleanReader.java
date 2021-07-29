package org.vitrivr.cineast.core.db.dao.reader;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.*;

public class BooleanReader extends AbstractEntityReader{


    private String entity;
    private String attribute;
    private String value;
    private RelationalOperator operator;
    private List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList;

/*
    public BooleanReader(DBSelector selector, String entity, String attribute) {
        super(selector);
        this.selector.open(MediaObjectMetadataDescriptor.ENTITY);
        this.entity = entity;
        this.attribute = attribute;

    }
    public BooleanReader(DBSelector selector, String entity, String attribute, String value, RelationalOperator operator) {
        super(selector);
        this.selector.open(entity);
        this.entity = entity;
        this.attribute = attribute;
        this.value = value;
        this.operator = operator;

    }
*/

    public BooleanReader(DBSelector selector, String entity, List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList) {
        super(selector);
        this.selector.open(entity);
        this.entity = entity;
        this.queryList = queryList;

    }



    /** Returns the total amount of elements in the database*/
    public int getTotalElements() {
        selector.open(entity);
        System.out.println(selector.existsEntity(entity));
        List<PrimitiveTypeProvider> results = selector.getUniqueValues(queryList.get(0).getLeft());
        System.out.println(selector.getAll("key"));
        System.out.println(results.size());
        return results.size();
    }
    /** Returns the total amount of elements for a specific boolean value
     * @return*/

    public int getElementsForAttribute() {
        List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(attribute, this.operator, PrimitiveTypeProvider.fromObject(this.value));
        return results.size();
    }

    public int getElementsAND() {
         /*List<PrimitiveTypeProvider> li = new ArrayList<>();
         li.add(PrimitiveTypeProvider.fromObject(this.value));
        MutableTriple<String, RelationalOperator, List<PrimitiveTypeProvider>> c = new MutableTriple<>(this.attribute, this.operator, Arrays.asList(PrimitiveTypeProvider.fromObject(this.value)));
        MutableTriple<String, RelationalOperator, List<PrimitiveTypeProvider>> b = new MutableTriple<>("id", this.operator, Arrays.asList(PrimitiveTypeProvider.fromObject("i_7_1")));
        List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> test = new ArrayList<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>>();
        test.add(c);
        test.add(b);*/
        List<Map<String, PrimitiveTypeProvider>> results = selector.getRowsAND(this.queryList,null, Arrays.asList(),null);
        return results.size();
/*        HashMap<String, Map<String, PrimitiveTypeProvider>> relevant = new HashMap<>();
         for (Triple<String, RelationalOperator, String> query : queryElements) {
             List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(query.getLeft(), query.getMiddle(), PrimitiveTypeProvider.fromObject(query.getRight()));
        }*/
    }
}