package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class FindSegmentSimilarPostHandler implements ParsingPostRestHandler<SimilarityQuery, SimilarityQueryResultBatch> {

  public static final String ROUTE = "find/segments/similar";
  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public FindSegmentSimilarPostHandler(ContinuousRetrievalLogic continuousRetrievalLogic) {
    this.continuousRetrievalLogic = continuousRetrievalLogic;
  }

  @Override
  public SimilarityQueryResultBatch performPost(SimilarityQuery query, Context ctx) {
    ConstrainedQueryConfig config = ConstrainedQueryConfig.getApplyingConfig(query.getQueryConfig());

    var returnMap = QueryUtil.findSegmentsSimilar(continuousRetrievalLogic, query.getTerms(), config);

    return new SimilarityQueryResultBatch(returnMap, config.getQueryId().toString());
  }

  @Override
  public Class<SimilarityQuery> inClass() {
    return SimilarityQuery.class;
  }

  @Override
  public Class<SimilarityQueryResultBatch> outClass() {
    return SimilarityQueryResultBatch.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find similar segments based on the given query");
          op.description("Performs a similarity search based on the formulated query");
          op.operationId("findSegmentSimilar");
          op.addTagsItem("Segments");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
