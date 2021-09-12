package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.graalvm.compiler.nodes.calc.ObjectEqualsNode;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BooleanLookup implements Message {


    /**
     * The type of Boolean Lookup
     */

    private String type;

    /**
     * Entity for the BooleanQuery
     */
    private String entity;
    /**
     * List consisting of single Boolean Queries
     */
    private List<BooleanLookupQuery> queries;
    /**
     * QueryList to be used in the Boolean Feature
     */
    private List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> queryList;
    /**
     * ComponentID to map a query to a BoolTerm in the frontend
     */
    private int componentID;

    /**
     * Constructor for the BooleanLookup object.
     *
     */

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
            queryList.add(new MutableTriple<>(query.getAttribute(), query.getOperator(), query.getValue()));
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

    private List<Object> values;

    private String entity;

    private RelationalOperator operator;

    @JsonCreator
    public BooleanLookupQuery(@JsonProperty("table_name") String table, @JsonProperty("attribute") String
            attribute, @JsonProperty("values") List<Object> values, @JsonProperty("entity") String entity,
                         @JsonProperty("operator") RelationalOperator operator) {
        this.table_name = table;
        this.attribute = attribute;
        this.values = values;
        this.entity = entity;
        this.operator = operator;
    }

    public String getTable_name() {
        return table_name;
    }

    public String getAttribute() {
        return attribute;
    }

    public List<PrimitiveTypeProvider> getValue() {
        List<PrimitiveTypeProvider> results = this.values.stream().map(item -> PrimitiveTypeProvider.fromObject(item)).collect(Collectors.toList());;
        return results;
    }

    public String getEntity() {
        return entity;
    }

    public RelationalOperator getOperator() { return operator; }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}