package org.vitrivr.cineast.api.rest.handlers.actions;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.FindObjectMetadataFullyQualifiedGetHandler.*;

public class FindObjectMetadataByKeyGetHandler implements GetRestHandler<MediaObjectMetadataQueryResult> {
  
  public static final String ROUTE = "find/metadata/with/:"+KEY_NAME+"/by/id/:"+OBJECT_ID_NAME;
  
  @OpenApi(
      summary = "Find metadata for a given object id with specified key",
      path = ROUTE, method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name=KEY_NAME, description = "The key of the metadata to find"),
          @OpenApiParam(name=OBJECT_ID_NAME, description = "The object id of the multimedia object to find metadata of")
      },
      tags = {APIEndpoint.METADATA_OAS_TAG},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from=MediaObjectMetadataQueryResult.class))
      }
  )
  @Override
  public MediaObjectMetadataQueryResult doGet(Context ctx) {
    final Map<String,String> parameters = ctx.pathParamMap();
    final String objectId = parameters.get(OBJECT_ID_NAME);
    final String key = parameters.get(KEY_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("", service.findByKey(objectId, key));
  }
  
  @Override
  public Class<MediaObjectMetadataQueryResult> outClass() {
    return MediaObjectMetadataQueryResult.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
}
