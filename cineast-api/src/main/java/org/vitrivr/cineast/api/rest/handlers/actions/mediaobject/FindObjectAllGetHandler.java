package org.vitrivr.cineast.api.rest.handlers.actions.mediaobject;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.List;

public class FindObjectAllGetHandler implements GetRestHandler<MediaObjectQueryResult> {
  
  // TODO CAUTION: This route has a breaking change in response signature
  
  public static final String TYPE_NAME = "type";
  
  public static final String ROUTE = "find/objects/all/"; // The more honest route
//  public static final String ROUTE = "find/objects/all/:"+TYPE_NAME;
  
  @OpenApi(
      summary = "Find all objects for a certain type",
      path= ROUTE, method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name = TYPE_NAME, description = "The type the objects should have")
      },
      tags={"Object"},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaObjectQueryResult.class))
      }
  )
  @Override
  public MediaObjectQueryResult doGet(Context ctx) {
    // TODO :type is not being used
    final MediaObjectReader ol = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    final List<MediaObjectDescriptor> multimediaobjectIds = ol.getAllObjects();
    ol.close();
    return new MediaObjectQueryResult("", multimediaobjectIds);
  }
  
  @Override
  public Class<MediaObjectQueryResult> outClass() {
    return MediaObjectQueryResult.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
}
