package org.vitrivr.cineast.api.rest.handlers.actions.segment;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindSegmentByIdPostHandler implements ParsingPostRestHandler<IdList, MediaSegmentQueryResult> {

  public static final String ROUTE = "find/segments/by/id";


  @Override
  public MediaSegmentQueryResult performPost(IdList ids, Context ctx) {
    if (ids == null || ids.getIds().length == 0) {
      return new MediaSegmentQueryResult("", new ArrayList<>(0));
    }
    final MediaSegmentReader sl = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    final Map<String, MediaSegmentDescriptor> segments = sl.lookUpSegments(Arrays.asList(ids.getIds()));
    sl.close();
    return new MediaSegmentQueryResult("", new ArrayList<>(segments.values()));
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
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
          op.summary("Finds segments for specified ids");
          op.description("Finds segments for specified ids");
          op.operationId("findSegmentByIdBatched");
          op.addTagsItem("Segment");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
