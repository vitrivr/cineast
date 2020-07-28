package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import com.sun.mail.imap.protocol.ID;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.List;
import java.util.Map;

import static org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler.KEY_NAME;

public class FindSegmentsByObjectIdGetHandler implements GetRestHandler<MediaSegmentQueryResult> {
  
  // TODO CAUTION: This route has a breaking change in response signature
  
  public static final String ID_NAME = "id";
  public static final String ROUTE = "find/segments/all/object/:" + ID_NAME;
  
  
  
  @Override
  public MediaSegmentQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String objectId = parameters.get(ID_NAME);
    final MediaSegmentReader sl = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    final List<MediaSegmentDescriptor> list = sl.lookUpSegmentsOfObject(objectId);
    sl.close();
    return new MediaSegmentQueryResult("", list);
  }
  
  @Override
  public Class<MediaSegmentQueryResult> outClass() {
    return MediaSegmentQueryResult.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @OpenApi(
      summary = "Find segments by their media object's id",
      path = ROUTE, method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name = ID_NAME, description = "The id of the media object to find segments of")
      },
      tags = {"Segment"},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaSegmentQueryResult.class))
      }
  )
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find segments by their media object's id");
          op.description("Find segments by their media object's id");
          op.operationId("findSegmentByObjectId");
          op.addTagsItem("Segment");
        })
        .pathParam(ID_NAME, String.class, p -> p.description("The id of the media object to find segments fo"))
        .json("200", outClass());
  }
}
