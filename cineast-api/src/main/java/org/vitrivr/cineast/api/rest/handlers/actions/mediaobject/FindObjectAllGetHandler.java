package org.vitrivr.cineast.api.rest.handlers.actions.mediaobject;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
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
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find all objects for a certain type");
          op.description("Find all objects for a certain type");
          op.operationId("findObjectsAll");
          op.addTagsItem("Object");
        })
//        .pathParam(TYPE_NAME, String.class, p -> p.description("The type the objects should have"))
        .json("200", outClass());
  }
}
