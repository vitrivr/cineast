package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindSegmentsByIdGetHandler implements GetRestHandler<MediaSegmentQueryResult> {
  
  public static final String ID_NAME = "id";
  
  public static final String ROUTE = "find/segments/by/id/:" + ID_NAME;
  
  @Override
  public MediaSegmentQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String segmentId = parameters.get(ID_NAME);
    final MediaSegmentReader sl = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    final List<MediaSegmentDescriptor> list = sl.lookUpSegment(segmentId).map(s -> {
      final List<MediaSegmentDescriptor> segments = new ArrayList<>(1);
      segments.add(s);
      return segments;
    }).orElse(new ArrayList<>(0));
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
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Finds segments for specified id");
          op.description("Finds segments for specified id");
          op.operationId("findSegmentById");
          op.addTagsItem("Segment");
        })
        .pathParam(ID_NAME, String.class, p -> p.description("The id of the segments"))
        .json("200", outClass());
  }
}
