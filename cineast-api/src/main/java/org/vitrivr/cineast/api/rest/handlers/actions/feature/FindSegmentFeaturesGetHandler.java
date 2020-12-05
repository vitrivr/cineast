package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.ID_QUALIFIER;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.FeaturesAllCategoriesQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

public class FindSegmentFeaturesGetHandler implements
    GetRestHandler<FeaturesAllCategoriesQueryResult> {

  public static final String ROUTE = "find/feature/all/by/id/:" + ID_QUALIFIER;

  @Override
  public FeaturesAllCategoriesQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String id = parameters.get(ID_QUALIFIER);
    return QueryUtil.retrieveFeaturesForAllCategories(id);
  }

  @Override
  public Class<FeaturesAllCategoriesQueryResult> outClass() {
    return FeaturesAllCategoriesQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findSegFeatById");
          op.description("Find features by the given id");
          op.summary("Find features for the given id");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(ID_QUALIFIER, String.class,
            p -> p.description("The id to find features of"))
        .json("200", outClass());

  }
}
