package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.ENTITY_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.FeaturesByEntityQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

/**
 * Handler for the API call to retrieve all features for all objects for a given table/entity name.
 */
public class FindFeaturesByEntityGetHandler implements ParsingPostRestHandler<IdList, FeaturesByEntityQueryResult> {

  public static final String ROUTE = "find/feature/all/by/entity/{" + ENTITY_NAME + "}";

  @Override
  public FeaturesByEntityQueryResult performPost(IdList idList, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();

    // Entity name from path params.
    final String entity = parameters.get(ENTITY_NAME);

    return QueryUtil.retrieveFeaturesForEntity(entity, idList.getIdList());
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

  @Override
  public Class<FeaturesByEntityQueryResult> outClass() {
    return FeaturesByEntityQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findFeaturesByEntity");
          op.description("Find features for the given entity name for all (or specific) IDs");
          op.summary("Find features for the given entity name for all (or specific) IDs");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(ENTITY_NAME, String.class)
        .body(inClass())
        .json("200", outClass());
  }

}
