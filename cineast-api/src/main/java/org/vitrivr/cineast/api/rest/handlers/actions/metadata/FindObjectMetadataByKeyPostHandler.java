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

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.KEY_NAME;

public class FindObjectMetadataByKeyPostHandler implements ParsingPostRestHandler<IdList, MediaObjectMetadataQueryResult> {
  
  public static final String ROUTE = "find/metadata/with/:" + KEY_NAME;
  
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
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find metadata for a given object id with specified key");
          op.description("Find metadata with a the speicifed key from the path across all domains and for the provided ids");
          op.operationId("findMetadataByKeyBatched");
          op.addTagsItem(OpenApiCompatHelper.METADATA_OAS_TAG);
        })
        .pathParam(KEY_NAME, String.class, p -> p.description("The key of the metadata to find"))
        .body(inClass())
        .json("200", outClass());
  }
}
