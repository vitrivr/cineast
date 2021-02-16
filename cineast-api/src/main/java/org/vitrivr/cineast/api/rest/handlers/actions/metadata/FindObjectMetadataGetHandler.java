package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;

public class FindObjectMetadataGetHandler implements GetRestHandler<MediaObjectMetadataQueryResult> {

  public static final String ROUTE = "find/metadata/by/id/:" + OBJECT_ID_NAME;

  @Override
  public MediaObjectMetadataQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String objectId = parameters.get(OBJECT_ID_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("", service.lookupMultimediaMetadata(objectId));
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
          op.operationId("findMetaById");
          op.description("Find metadata by the given object id");
          op.summary("Find metadata for the given object id");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(OBJECT_ID_NAME, String.class,
            p -> p.description("The object id to find metadata of"))
        .json("200", outClass());

  }
}
