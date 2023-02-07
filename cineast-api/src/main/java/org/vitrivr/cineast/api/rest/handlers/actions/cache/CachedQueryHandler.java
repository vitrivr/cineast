package org.vitrivr.cineast.api.rest.handlers.actions.cache;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryResultCache;
import org.vitrivr.cineast.core.data.StringDoublePair;

import java.util.List;
import java.util.Map;

public class CachedQueryHandler implements GetRestHandler<SimilarityQueryResultBatch> {

    public static final String ID_NAME = "queryId";
    public static final String ROUTE = "find/segments/cached/{" + ID_NAME + "}";

    @Override
    public SimilarityQueryResultBatch doGet(Context ctx) {
        String queryId = ctx.pathParam(ID_NAME);
        Map<String, List<StringDoublePair>> results = QueryResultCache.getCachedResult(queryId);
        return new SimilarityQueryResultBatch(results, queryId);
    }

    @Override
    public String route() {
        return ROUTE;
    }

    @Override
    public Class<SimilarityQueryResultBatch> outClass() {
        return SimilarityQueryResultBatch.class;
    }

    @Override
    public OpenApiDocumentation docs() {
        return OpenApiBuilder.document()
                .operation(op -> {
                    op.summary("Finds segments for specified cached query id");
                    op.description("Finds segments for specified cached query id");
                    op.operationId("findSegmentByCachedQueryId");
                    op.addTagsItem("Segment");
                })
                .pathParam(ID_NAME, String.class, p -> p.description("The id of the query"))
                .json("200", outClass());
    }
}
