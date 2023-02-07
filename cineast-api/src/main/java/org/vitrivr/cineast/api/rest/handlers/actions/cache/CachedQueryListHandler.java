package org.vitrivr.cineast.api.rest.handlers.actions.cache;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.lookup.QueryCacheInfoList;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryResultCache;

public class CachedQueryListHandler implements GetRestHandler<QueryCacheInfoList> {

    public static final String ROUTE = "find/segments/cached";

    @Override
    public QueryCacheInfoList doGet(Context ctx) {
        return new QueryCacheInfoList(
                QueryResultCache.getQueryCacheInfoList()
        );
    }

    @Override
    public Class<QueryCacheInfoList> outClass() {
        return QueryCacheInfoList.class;
    }

    @Override
    public String route() {
        return ROUTE;
    }

    @Override
    public OpenApiDocumentation docs() {
        return OpenApiBuilder.document()
                .operation(op -> {
                    op.summary("Lists cached queries");
                    op.description("Lists cached queries");
                    op.operationId("listCachedQueries");
                    op.addTagsItem("Cache");

                })
                .json("200", outClass());
    }
}
