package org.vitrivr.cineast.api.rest.handlers.actions.feature;

import static org.vitrivr.cineast.api.util.APIConstants.ID_QUALIFIER;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.TagIDsForElementQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

public class FindTagsForElementGetHandler implements GetRestHandler<TagIDsForElementQueryResult> {

  public static final String ROUTE = "find/feature/tags/by/id/:" + ID_QUALIFIER;

  @Override
  public TagIDsForElementQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String id = parameters.get(ID_QUALIFIER);
    List<String> tagIDs = QueryUtil.retrieveTagIDs(Collections.singletonList(id));
    return new TagIDsForElementQueryResult("", tagIDs, id);
  }

  @Override
  public Class<TagIDsForElementQueryResult> outClass() {
    return TagIDsForElementQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findTagsById");
          op.description("Find tag ids for the given id");
          op.summary("Find tag ids for the given id");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(ID_QUALIFIER, String.class, p -> p.description("The id to find tagids of"))
        .json("200", outClass());

  }
}
