package org.vitrivr.cineast.api.rest.handlers.actions.mediaobject;

import static org.vitrivr.cineast.api.util.APIConstants.LIMIT_NAME;
import static org.vitrivr.cineast.api.util.APIConstants.SKIP_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindObjectPaginationGetHandler implements GetRestHandler<MediaObjectQueryResult> {

  public static final String ROUTE = "find/object/all/{" + SKIP_NAME + "}/{" + LIMIT_NAME + "}";

  private static final Logger LOGGER = LogManager.getLogger(FindObjectPaginationGetHandler.class);

  @Override
  public MediaObjectQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();

    try (final MediaObjectReader ol = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get())) {
      final var skipParam = parameters.get(SKIP_NAME);
      final var skip = skipParam == null ? 0 : Integer.parseInt(skipParam);
      final var limitParam = parameters.get(LIMIT_NAME);
      final var limit = limitParam == null ? Integer.MAX_VALUE : Integer.parseInt(limitParam);

      var result = ol.getAllObjects(skip, limit);
      LOGGER.trace("returning {} elements for skip {} and limit {}", result.size(), skip, limit);
      return new MediaObjectQueryResult("", result);
    } catch (Exception e) {
      LOGGER.error("Error during request", e);
      return new MediaObjectQueryResult("", new ArrayList<>());
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
