package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.ArrayList;
import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.DOMAIN_NAME;

/**
 * Finds metadata of a given object id list (REST) / object id (Web) and returns only items in a certain domain.
 * <p>
 * The action should contain a domain, e.g. {@code /metadata/in/:domain}. The post body is an {@link IdList} and the *
 * response contains metadata for each id in that list, belonging to the specified domain. The response is JSON encoded
 * * and basically identical to a response from {@link FindObjectMetadataFullyQualifiedGetHandler}: * A list of {@link *
 * MediaObjectMetadataDescriptor}s with only entries of the specified domain.
 */
public class FindObjectMetadataByDomainPostHandler implements ParsingPostRestHandler<IdList, MediaObjectMetadataQueryResult> {
  
  public static final String ROUTE = "find/metadata/in/:" + DOMAIN_NAME;
  
  @Override
  public MediaObjectMetadataQueryResult performPost(IdList ids, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    if (ids == null || ids.getIds().length == 0) {
      return new MediaObjectMetadataQueryResult("", new ArrayList<>(0));
    }
    final String domain = parameters.get(DOMAIN_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("",
        service.findByDomain(ids.getIdList(), domain));
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
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find metadata in the specified domain for all the given ids");
          op.description("Find metadata in the specified domain for all the given ids");
          op.operationId("findMetadataByDomainBatched");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(DOMAIN_NAME, String.class, p -> p.description("The domain of the metadata to find"))
        .body(inClass())
        .json("200", outClass());
  }
}
