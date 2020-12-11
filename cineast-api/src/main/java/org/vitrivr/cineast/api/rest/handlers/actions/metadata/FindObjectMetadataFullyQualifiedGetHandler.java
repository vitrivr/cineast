package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.Map;

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
public class FindObjectMetadataFullyQualifiedGetHandler implements
    GetRestHandler<MediaObjectMetadataQueryResult> {
  
  public static final String OBJECT_ID_NAME = "id";
  public static final String DOMAIN_NAME = "domain";
  public static final String KEY_NAME = "key";
  
  public static final String ROUTE = "find/metadata/of/:" + OBJECT_ID_NAME + "/in/:" + DOMAIN_NAME + "/with/:" + KEY_NAME;
  
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
  
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.description("The description");
          op.summary("Find metadata for specific object id in given domain with given key");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
          op.operationId("findMetaFullyQualified");
        })
        .pathParam(OBJECT_ID_NAME, String.class, param -> {
          param.description("The object id");
        })
        .pathParam(DOMAIN_NAME, String.class, param -> {
          param.description("The domain name");
        })
        .pathParam(KEY_NAME, String.class, param -> param.description("Metadata key"))
        .json("200", outClass());
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @Override
  public Class<MediaObjectMetadataQueryResult> outClass() {
    return MediaObjectMetadataQueryResult.class;
  }
  
  /* TODO Actually, there is a lot of refactoring potential in this entire package */
}
