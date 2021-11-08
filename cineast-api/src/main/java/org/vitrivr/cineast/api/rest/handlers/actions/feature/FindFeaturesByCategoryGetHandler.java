package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.CATEGORY_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.FeaturesByCategoryQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

/**
 * Handler for the API call to retrieve all features for all objects for a given category.
 */
public class FindFeaturesByCategoryGetHandler implements ParsingPostRestHandler<IdList, FeaturesByCategoryQueryResult> {

  public static final String ROUTE = "find/feature/all/by/category/{" + CATEGORY_NAME + "}";

  @Override
  public FeaturesByCategoryQueryResult performPost(IdList idList, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();

    // Category name from path params.
    final String cat = parameters.get(CATEGORY_NAME);

    return QueryUtil.retrieveFeaturesForCategory(cat, idList.getIdList());
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

  @Override
  public Class<FeaturesByCategoryQueryResult> outClass() {
    return FeaturesByCategoryQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findFeaturesByCategory");
          op.description("Find features for the given category for all (or specific) IDs");
          op.summary("Find features for the given category for all (or specific) IDs");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(CATEGORY_NAME, String.class)
        .body(inClass())
        .json("200", outClass());
  }

}
