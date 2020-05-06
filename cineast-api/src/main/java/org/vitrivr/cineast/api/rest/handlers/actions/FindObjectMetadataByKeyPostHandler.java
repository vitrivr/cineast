package org.vitrivr.cineast.api.rest.handlers.actions;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.ArrayList;
import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.FindObjectMetadataFullyQualifiedGetHandler.*;

public class FindObjectMetadataByKeyPostHandler implements ParsingPostRestHandler<IdList, MediaObjectMetadataQueryResult> {
  
  public static final String ROUTE = "find/metadata/with/:" + KEY_NAME;
  
  @OpenApi(
      summary = "Find metadata for a given object id with specified key",
      path = ROUTE, method = HttpMethod.POST,
      pathParams = {
          @OpenApiParam(name = KEY_NAME, description = "The key of the metadata to find")
      },
      requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = IdList.class)),
      tags = {APIEndpoint.METADATA_OAS_TAG},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaObjectMetadataQueryResult.class))
      }
  )
  @Override
  public MediaObjectMetadataQueryResult performPost(IdList ids, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    if (ids == null || ids.getIds().length == 0) {
      return new MediaObjectMetadataQueryResult("", new ArrayList<>(0));
    }
    final String key = parameters.get(KEY_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("", service.findByKey(ids.getIdList(), key));
  }
  
  @Override
  public Class<IdList> inClass() {
    return IdList.class;
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
