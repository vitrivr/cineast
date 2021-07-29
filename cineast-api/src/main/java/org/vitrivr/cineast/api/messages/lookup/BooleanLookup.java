package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BooleanLookup implements Message {


    /**
     * List of object ID's for which metadata should be looked up.
     */

    private String type;

    private String entity;

    private List<BooleanLookupQuery> queries;

    private List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList;

    private int componentID;

    /**
     * List of metadata domains that should be considered. If empty, all domains are considered!
     */

    /**
     * Constructor for the MetadataLookup object.
     *
     */
/*    @JsonCreator
    public BooleanLookup(@JsonProperty("table_name") String table, @JsonProperty("attribute") String
            attribute, @JsonProperty("value") String value, @JsonProperty("entity") String entity,
                         @JsonProperty("operator") RelationalOperator operator, @JsonProperty("type") String type) {
        this.table_name = table;
        this.attribute = attribute;
        this.value = value;
        this.entity = entity;
        this.type = type;
        this.operator = operator;
    }*/

    @JsonCreator
    public BooleanLookup(@JsonProperty("boolQueries") List<BooleanLookupQuery> queries, @JsonProperty("type") String type,
                         @JsonProperty("componentID") int componentID) {
        this.queries = queries;
        this.type = type;
        this.componentID = componentID;
        this.entity = queries.get(0).getEntity();
        this.queryList = new ArrayList<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>>();
        createTriple();
    }

    private void createTriple() {
        for (BooleanLookupQuery query: queries) {
            queryList.add(new MutableTriple<>(query.getAttribute(), query.getOperator(), Arrays.asList(PrimitiveTypeProvider.fromObject(query.getValue()))));
        }
}
    /**
     * {@inheritDoc}
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.B_LOOKUP;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

    public List<BooleanLookupQuery> getQueries() { return queries; }

    public String getType() { return type; }

    public String getEntity() {
        return entity;
    }

    public int getComponentID() { return componentID; }

    public List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> getQueryList() { return queryList; }

}
class BooleanLookupQuery {

    private String table_name;

    private String attribute;

    private String value;

    private String entity;

    private RelationalOperator operator;
        @JsonCreator
    public BooleanLookupQuery(@JsonProperty("table_name") String table, @JsonProperty("attribute") String
            attribute, @JsonProperty("value") String value, @JsonProperty("entity") String entity,
                         @JsonProperty("operator") RelationalOperator operator) {
        this.table_name = table;
        this.attribute = attribute;
        this.value = value;
        this.entity = entity;
        this.operator = operator;
    }

    public String getTable_name() {
        return table_name;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    public String getEntity() {
        return entity;
    }

    public RelationalOperator getOperator() { return operator; }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}