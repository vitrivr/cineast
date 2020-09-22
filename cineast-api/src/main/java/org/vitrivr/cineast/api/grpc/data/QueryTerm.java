package org.vitrivr.cineast.api.grpc.data;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryTerm {

    private final QueryContainer container;
    private final ReadableQueryConfig queryConfig;
    private final float weight;
    private final List<String> categories = new ArrayList<>();

    public QueryTerm(QueryContainer container, ReadableQueryConfig queryConfig, float weight, Collection<String> categories){
        this.container = container;
        this.queryConfig = queryConfig;
        this.weight = weight;
        this.categories.addAll(categories);
    }

    public QueryContainer getContainer() {
        return container;
    }

    public ReadableQueryConfig getQueryConfig() {
        return queryConfig;
    }

    public float getWeight() {
        return weight;
    }

    public List<String> getCategories() {
        return categories;
    }

}
