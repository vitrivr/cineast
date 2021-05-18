package org.vitrivr.cineast.api.rest.handlers.actions.mediaobject;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class FindObjectByIdPostHandler implements ParsingPostRestHandler<IdList, MediaObjectQueryResult> {

  public static final String ROUTE = "find/object/by/id";

  @Override
  public MediaObjectQueryResult performPost(IdList context, Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    if (context == null || context.getIds().length == 0) {
      return new MediaObjectQueryResult("", new ArrayList<>(0));
    }
    final MediaObjectReader ol = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    final Map<String, MediaObjectDescriptor> objects = ol.lookUpObjects(Arrays.asList(context.getIds()));
    ol.close();
    return new MediaObjectQueryResult("", new ArrayList<>(objects.values()));
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

  @Override
  public Class<MediaObjectQueryResult> outClass() {
    return MediaObjectQueryResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find objects by id");
          op.description("Find objects by id");
          op.operationId("findObjectsByIdBatched");
          op.addTagsItem("Object");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
