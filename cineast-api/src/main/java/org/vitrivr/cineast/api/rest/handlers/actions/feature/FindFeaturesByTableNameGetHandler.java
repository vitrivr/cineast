package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.TABLE_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Arrays;
import java.util.Map;
import org.vitrivr.cineast.api.messages.result.FeaturesByTableNameQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

/**
 * Handler for the API call to retrieve all features for all objects for a given table name.
 */
public class FindFeaturesByTableNameGetHandler implements ParsingPostRestHandler<String[], FeaturesByTableNameQueryResult> {

  // TODO Add a TABLE_NAME constant to APIConstants.
  public static final String ROUTE = "find/feature/all/by/tablename/:" + TABLE_NAME;

  @Override
  public FeaturesByTableNameQueryResult performPost(String[] input, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();

    // Table name from path params.
    final String cat = parameters.get(TABLE_NAME);

    return QueryUtil.queryFeaturesForTableName(cat, Arrays.asList(input));
  }

  @Override
  public Class<String[]> inClass() {
    return String[].class;
  }

  @Override
  public Class<FeaturesByTableNameQueryResult> outClass() {
    return FeaturesByTableNameQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findFeaturesByTableName");
          op.description("Find features for the given feature table name for all (or specific) IDs");
          op.summary("Find features for the given feature table name for all (or specific) IDs");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(TABLE_NAME, String.class)
        .body(inClass())
        .json("200", outClass());
  }

}
