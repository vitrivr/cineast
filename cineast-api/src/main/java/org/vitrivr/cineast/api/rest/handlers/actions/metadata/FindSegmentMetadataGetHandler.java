package org.vitrivr.cineast.api.rest.handlers.actions.metadata;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.OBJECT_ID_NAME;
import static org.vitrivr.cineast.api.util.APIConstants.ID_QUALIFIER;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.Map;
import org.vitrivr.cineast.api.messages.result.MediaSegmentMetadataQueryResult;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

public class FindSegmentMetadataGetHandler implements GetRestHandler<MediaSegmentMetadataQueryResult> {

  public static final String ROUTE = "find/metadata/by/segmentid/:" + ID_QUALIFIER;

  @Override
  public MediaSegmentMetadataQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String segmentId = parameters.get(ID_QUALIFIER);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaSegmentMetadataQueryResult("", service.lookupSegmentMetadata(segmentId));
  }

  @Override
  public Class<MediaSegmentMetadataQueryResult> outClass() {
    return MediaSegmentMetadataQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.operationId("findSegMetaById");
          op.description("Find metadata by the given segment id");
          op.summary("Find metadata for the given segment id");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(OBJECT_ID_NAME, String.class,
            p -> p.description("The segment id to find metadata of"))
        .json("200", outClass());

  }
}
