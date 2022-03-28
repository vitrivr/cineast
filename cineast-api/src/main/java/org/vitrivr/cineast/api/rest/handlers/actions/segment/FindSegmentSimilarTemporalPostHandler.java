package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.query.TemporalQuery;
import org.vitrivr.cineast.api.messages.result.TemporalQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class FindSegmentSimilarTemporalPostHandler implements ParsingPostRestHandler<TemporalQuery, TemporalQueryResult> {

  public static final String ROUTE = "find/segments/similar/temporal";
  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public FindSegmentSimilarTemporalPostHandler(ContinuousRetrievalLogic continuousRetrievalLogic) {
    this.continuousRetrievalLogic = continuousRetrievalLogic;
  }


  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find similar segments based on the given temporal query");
          op.description("Performs a similarity search based on the formulated query stages in the given temporal order, scoring final results by their similarity to the specified temporal context");
          op.operationId("findSegmentSimilarTemporal");
          op.addTagsItem("Segments");
        })
        .body(inClass())
        .json("200", outClass());
  }

  @Override
  public TemporalQueryResult performPost(TemporalQuery query, Context ctx) {
    ConstrainedQueryConfig config = ConstrainedQueryConfig.getApplyingConfig(query.getQueryConfig());

    var temporalResults = QueryUtil.findSegmentsSimilarTemporal(continuousRetrievalLogic, query, config);

    return new TemporalQueryResult(config.getQueryId(), temporalResults);
  }

  @Override
  public Class<TemporalQuery> inClass() {
    return TemporalQuery.class;
  }

  @Override
  public Class<TemporalQueryResult> outClass() {
    return TemporalQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }
}
