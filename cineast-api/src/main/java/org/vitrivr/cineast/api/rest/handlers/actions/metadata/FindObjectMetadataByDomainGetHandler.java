package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.DOMAIN_NAME;
import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;

/**
 * Finds metadata of a given object id list (REST) / object id (Web) and returns only items in a certain domain. * * <p>
 * * The action should contain an id and a domain, e.g. {@code /metadata/in/:domain/by/id/:id}. The response is JSON *
 * encoded and basically identical to a response from {@link FindObjectMetadataFullyQualifiedGetHandler}: A list of
 * {@link * MediaObjectMetadataDescriptor}s with only entries of the specified domain.
 */
public class FindObjectMetadataByDomainGetHandler implements GetRestHandler<MediaObjectMetadataQueryResult> {
  
  
  public static final String ROUTE = "find/metadata/in/:" + DOMAIN_NAME + "/by/id/:" + FindObjectMetadataFullyQualifiedGetHandler.DOMAIN_NAME;
  
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
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find metadata for specific object id in given domain");
          op.description("Find metadata for specific object id in given domain");
          op.operationId("findMetadataByDomain");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(DOMAIN_NAME, String.class, p -> p.description("The domain of the metadata to find"))
        .pathParam(OBJECT_ID_NAME, String.class, p -> p.description("The object id of the multimedia object to find metadata for"))
        .json("200", outClass());
  }
}
