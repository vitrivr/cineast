package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.MediaSegmentAllFeaturesQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.util.QueryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;

public class FindSegmentFeaturesGetHandler implements GetRestHandler<MediaSegmentAllFeaturesQueryResult> {
  
  public static final String ROUTE = "find/segment/metadata/by/id/:" + OBJECT_ID_NAME;
  
  @Override
  public MediaSegmentAllFeaturesQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String segmentId = parameters.get(OBJECT_ID_NAME);

    List<String[]> result = new ArrayList<>();
    result.add(QueryUtil.retrieveTagsBySegmentId(segmentId).toArray(new String[0]));
    result.add(QueryUtil.retrieveCaptionBySegmentId(segmentId).toArray(new String[0]));
    result.add(QueryUtil.retrieveOCRBySegmentId(segmentId).toArray(new String[0]));
    result.add(QueryUtil.retrieveASRBySegmentId(segmentId).toArray(new String[0]));
    return new MediaSegmentAllFeaturesQueryResult("", result);
  }
  
  @Override
  public Class<MediaSegmentAllFeaturesQueryResult> outClass() {
    return MediaSegmentAllFeaturesQueryResult.class;
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
          op.description("Find features by the given segment id");
          op.summary("Find features for the given segment id");
          op.addTagsItem(APIEndpoint.METADATA_OAS_TAG);
        })
        .pathParam(OBJECT_ID_NAME, String.class, p -> p.description("The segment id to find features of"))
        .json("200", outClass());
    
  }
}
