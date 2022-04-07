package org.vitrivr.cineast.api.rest.handlers.actions.mediaobject;

import static org.vitrivr.cineast.api.util.APIConstants.LIMIT_NAME;
import static org.vitrivr.cineast.api.util.APIConstants.SKIP_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindObjectPaginationGetHandler implements GetRestHandler<MediaObjectQueryResult> {

  public static final String ROUTE = "find/object/all/{" + SKIP_NAME + "}/{" + LIMIT_NAME + "}";

  private static final Logger LOGGER = LogManager.getLogger(FindObjectPaginationGetHandler.class);

  @Override
  public MediaObjectQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();

    final var skip = Integer.parseInt(parameters.get(SKIP_NAME));
    final var limit = Integer.parseInt(parameters.get(LIMIT_NAME));

    try (final MediaObjectReader ol = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get())) {
      return new MediaObjectQueryResult("", ol.getAllObjects(limit, skip));
    }
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
          op.summary("Get a fixed amount of objects from the sorted list");
          op.description("Equivalent to calling SELECT * FROM multimediaobject ORDER BY objectid ASC LIMIT limit SKIP skip. Mostly used for pagination when wanting to retrieve all objects");
          op.operationId("findObjectsPagination");
          op.addTagsItem("Object");
        })
        .pathParam(LIMIT_NAME, Integer.class, p -> p.description("How many object at most should be fetched"))
        .pathParam(SKIP_NAME, Integer.class, p -> p.description("How many objects should be skipped"))
        .json("200", outClass());
  }
}
