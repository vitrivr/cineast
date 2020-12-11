package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;
import static org.vitrivr.cineast.api.util.APIConstants.CATEGORY_NAME;
import static org.vitrivr.cineast.api.util.APIConstants.ID_QUALIFIER;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.FeaturesTextCategoryQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

public class FindSegmentTextGetHandler implements GetRestHandler<FeaturesTextCategoryQueryResult> {

  public static final String ROUTE = "find/feature/text/by/:" + ID_QUALIFIER + "/:" + CATEGORY_NAME;

  @Override
  public FeaturesTextCategoryQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String id = parameters.get(ID_QUALIFIER);
    final String category = parameters.get(CATEGORY_NAME);
    return new FeaturesTextCategoryQueryResult("", QueryUtil.retrieveTextFeatureByID(id, category), category, id);
  }

  @Override
  public Class<FeaturesTextCategoryQueryResult> outClass() {
    return FeaturesTextCategoryQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findTextByIDAndCat");
          op.description("Find Text by the given id and retrieval category");
          op.summary("Find Text for the given id and retrieval category");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(ID_QUALIFIER, String.class, p -> p.description("The id to find text of"))
        .pathParam(CATEGORY_NAME, String.class, p -> p.description("The category for which retrieval shall be performed"))
        .json("200", outClass());

  }
}
