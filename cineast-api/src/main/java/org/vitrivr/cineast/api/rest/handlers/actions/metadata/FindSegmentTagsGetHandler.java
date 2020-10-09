package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.MediaSegmentFeatureQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;

public class FindSegmentTagsGetHandler implements GetRestHandler<MediaSegmentFeatureQueryResult> {
  
  public static final String ROUTE = "find/segment/tags/by/id/:" + OBJECT_ID_NAME;
  
  @Override
  public MediaSegmentFeatureQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String segmentId = parameters.get(OBJECT_ID_NAME);

    return new MediaSegmentFeatureQueryResult("", QueryUtil.retrieveTagsBySegmentId(segmentId));
  }
  
  @Override
  public Class<MediaSegmentFeatureQueryResult> outClass() {
    return MediaSegmentFeatureQueryResult.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findSegTagsById");
          op.description("Find tags by the given segment id");
          op.summary("Find tags for the given segment id");
          op.addTagsItem(APIEndpoint.METADATA_OAS_TAG);
        })
        .pathParam(OBJECT_ID_NAME, String.class, p -> p.description("The segment id to find tags of"))
        .json("200", outClass());
    
  }
}
