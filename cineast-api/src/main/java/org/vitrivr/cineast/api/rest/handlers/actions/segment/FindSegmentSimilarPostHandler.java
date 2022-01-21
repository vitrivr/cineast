package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.standalone.config.Config;
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

    HashMap<String, List<StringDoublePair>> returnMap = new HashMap<>();
    /*
     * Prepare map that maps categories to QueryTerm components.
     */
    HashMap<String, ArrayList<AbstractQueryTermContainer>> categoryMap = QueryUtil.groupComponentsByCategory(query.getComponents());

    QueryConfig config = query.getQueryConfig();
    ConstrainedQueryConfig qconf = new ConstrainedQueryConfig(config);
    if (config == null) {
      final int max = Math.min(qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults()), Config.sharedConfig().getRetriever().getMaxResults());
      qconf.setMaxResults(max);
      final int resultsPerModule = Math.min(qconf.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever().getMaxResultsPerModule() : qconf.getResultsPerModule(), Config.sharedConfig().getRetriever().getMaxResultsPerModule());
      qconf.setResultsPerModule(resultsPerModule);
    }

    for (String category : categoryMap.keySet()) {
      List<Pair<AbstractQueryTermContainer, ReadableQueryConfig>> containerList = categoryMap.get(category).stream().map(x -> new Pair<>(x, (ReadableQueryConfig) qconf)).collect(Collectors.toList());
      returnMap.put(category, QueryUtil.retrieveCategory(continuousRetrievalLogic, containerList, category));
    }

    return new SimilarityQueryResultBatch(returnMap, qconf.getQueryId().toString());
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
