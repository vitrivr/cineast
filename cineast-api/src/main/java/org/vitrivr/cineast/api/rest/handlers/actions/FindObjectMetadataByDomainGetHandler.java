package org.vitrivr.cineast.api.rest.handlers.actions;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.FindObjectMetadataFullyQualifiedGetHandler.*;

/**
 * Finds metadata of a given object id list (REST) / object id (Web) and returns only items in a certain domain.
 *  *
 *  * <p>
 *  * The action should contain an id and a domain, e.g. {@code /metadata/in/:domain/by/id/:id}. The response is JSON
 *  * encoded and basically identical to a response from {@link FindMetadataByObjectIdActionHandler}: A list of {@link
 *  * MediaObjectMetadataDescriptor}s with only entries of the specified domain.
 */
public class FindObjectMetadataByDomainGetHandler implements GetRestHandler<MediaObjectMetadataQueryResult> {
  
  
  public static final String ROUTE = "find/metadata/in/:" + DOMAIN_NAME + "/by/id/:" + FindObjectMetadataFullyQualifiedGetHandler.DOMAIN_NAME;
  
  @OpenApi(
      summary = "Find metadata for specific object id in given domain",
      path = ROUTE, method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name = OBJECT_ID_NAME, description = "The object id of the multimedia object to find metadata for"),
          @OpenApiParam(name = DOMAIN_NAME, description = "The domain of the metadata to find")
      },
      tags = {APIEndpoint.METADATA_OAS_TAG},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaObjectMetadataQueryResult.class))
      }
  )
  @Override
  public MediaObjectMetadataQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String objectId = parameters.get(OBJECT_ID_NAME);
    final String domain = parameters.get(DOMAIN_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("",
        service.findByDomain(objectId, domain));
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
