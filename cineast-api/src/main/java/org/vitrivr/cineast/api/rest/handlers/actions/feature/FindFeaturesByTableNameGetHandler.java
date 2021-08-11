package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.CATEGORY_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.messages.result.AllFeaturesByTableNameQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

/**
 * Handler for the API call to retrieve all features for all objects for a given table name.
 */
public class FindFeaturesByTableNameGetHandler implements GetRestHandler<AllFeaturesByTableNameQueryResult> {

  // TODO Add a TABLE_NAME constant to APIConstants.
  public static final String ROUTE = "find/feature/all/by/tablename/:" + CATEGORY_NAME;

  @Override
  public AllFeaturesByTableNameQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String cat = parameters.get(CATEGORY_NAME);
    return QueryUtil.retrieveAllFeaturesForTableName(cat);
  }

  @Override
  public Class<AllFeaturesByTableNameQueryResult> outClass() {
    return AllFeaturesByTableNameQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findAllFeatByTableName");
          op.description("Find all features for the given table name for all indexed objects");
          op.summary("Find features for the given table name");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(CATEGORY_NAME, String.class,
            p -> p.description("The table name to find features for all objects for"))
        .json("200", outClass());
  }

}
