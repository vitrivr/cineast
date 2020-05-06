package org.vitrivr.cineast.api.rest.handlers.actions;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.components.AbstractMetadataFilterDescriptor;
import org.vitrivr.cineast.api.messages.lookup.OptionallyFilteredIdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FindObjectMetadataPostHandler implements ParsingPostRestHandler<OptionallyFilteredIdList, MediaObjectMetadataQueryResult> {
  
  public static final String ROUTE = "find/metadata/by/id";
  
  @OpenApi(
      summary = "Find metadata for the given object id",
      path=ROUTE, method = HttpMethod.POST,
      requestBody = @OpenApiRequestBody(content = @OpenApiContent(from=OptionallyFilteredIdList.class)),
      tags={APIEndpoint.METADATA_OAS_TAG},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaObjectMetadataQueryResult.class))
      }
  )
  @Override
  public MediaObjectMetadataQueryResult performPost(OptionallyFilteredIdList ids, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    if (ids == null || ids.getIds().length == 0) {
      return new MediaObjectMetadataQueryResult("", new ArrayList<>(0));
    }
    final MetadataRetrievalService service = new MetadataRetrievalService();
    List<MediaObjectMetadataDescriptor> descriptors = service
        .lookupMultimediaMetadata(ids.getIdList());
    if (ids.hasFilters()) {
      final List<AbstractMetadataFilterDescriptor> filters = ids.getFilterList();
      for (AbstractMetadataFilterDescriptor filter : filters) {
        descriptors = descriptors.stream().filter(filter).collect(Collectors.toList());
      }
    }
    return new MediaObjectMetadataQueryResult("", descriptors);
  }
  
  @Override
  public Class<OptionallyFilteredIdList> inClass() {
    return OptionallyFilteredIdList.class;
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
