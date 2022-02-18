package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.query.StagedSimilarityQuery;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class FindSegmentSimilarStagedPostHandler implements ParsingPostRestHandler<StagedSimilarityQuery, SimilarityQueryResultBatch> {

  public static final String ROUTE = "find/segments/similar/staged";
  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public FindSegmentSimilarStagedPostHandler(ContinuousRetrievalLogic continuousRetrievalLogic) {
    this.continuousRetrievalLogic = continuousRetrievalLogic;
  }


  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find similar segments based on the given staged query");
          op.description("Performs a similarity search based on the formulated query stages, executing each subsequent stage on the results of the previous stage");
          op.operationId("findSegmentSimilarStaged");
          op.addTagsItem("Segments");
        })
        .body(inClass())
        .json("200", outClass());
  }

  @Override
  public SimilarityQueryResultBatch performPost(StagedSimilarityQuery query, Context ctx) {
    ConstrainedQueryConfig config = ConstrainedQueryConfig.getApplyingConfig(query.getConfig());

    var results = QueryUtil.findSegmentsSimilarStaged(continuousRetrievalLogic, query.getStages(), config);

    return new SimilarityQueryResultBatch(results, config.getQueryId().toString());
  }

  @Override
  public Class<StagedSimilarityQuery> inClass() {
    return StagedSimilarityQuery.class;
  }

  @Override
  public Class<SimilarityQueryResultBatch> outClass() {
    return SimilarityQueryResultBatch.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }
}
