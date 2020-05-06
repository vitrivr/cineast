package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.Map;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

/**
 * This class handles GET requests with an object id, domain and key and returns all matching metadata descriptors.
 * <p>
 * <h3>GET</h3>
 * This action's resource should have the following structure: {@code find/metadata/of/:id/in/:domain/with/:key}. It
 * returns then all metadata of the object with this id, belonging to that domain with the specified key.
 * </p>
 *
 * @author loris.sauter
 */
public class FindMetadataByDomainWithKeyByObjectIdActionHandler implements
    GetRestHandler<MediaObjectMetadataQueryResult> {
  
  public static final String OBJECT_ID_NAME = "id";
  public static final String DOMAIN_NAME = "domain";
  public static final String KEY_NAME = "key";
  
  public static final String ROUTE = "find/metadata/of/:" + OBJECT_ID_NAME + "/in/:" + DOMAIN_NAME + "/with/:" + KEY_NAME;
  
  @OpenApi(
      summary = "Find metadata for specific object id in given domain with given key",
      path = ROUTE, method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name = OBJECT_ID_NAME, description = "The object id"),
          @OpenApiParam(name = DOMAIN_NAME, description = "The domain name"),
          @OpenApiParam(name = KEY_NAME, description = "The key of the metadata")
      },
      tags = {"metadata"},
      responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaObjectMetadataQueryResult.class))}
      // TODO Other responses in error case
  )
  @Override
  public MediaObjectMetadataQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String objectId = parameters.get(OBJECT_ID_NAME);
    final String domain = parameters.get(DOMAIN_NAME);
    final String key = parameters.get(KEY_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("", service.find(objectId, domain, key)
    );
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @Override
  public Class<MediaObjectMetadataQueryResult> outClass() {
    return MediaObjectMetadataQueryResult.class;
  }
}
