package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.CATEGORY_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.messages.result.AllFeaturesByCategoryQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

/**
 * Handler for the API call to retrieve all features for all objects for a given category.
 */
public class FindFeaturesByCategoryGetHandler implements GetRestHandler<AllFeaturesByCategoryQueryResult> {

  public static final String ROUTE = "find/feature/all/by/category/:" + CATEGORY_NAME;

  @Override
  public AllFeaturesByCategoryQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String cat = parameters.get(CATEGORY_NAME);
    return QueryUtil.retrieveAllFeaturesByCategory(cat);
  }

  @Override
  public Class<AllFeaturesByCategoryQueryResult> outClass() {
    return AllFeaturesByCategoryQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findAllFeatByCat");
          op.description("Find all features for the given category for all indexed objects");
          op.summary("Find features for the given category");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(CATEGORY_NAME, String.class,
            p -> p.description("The category to find features for all objects for"))
        .json("200", outClass());
  }

}
