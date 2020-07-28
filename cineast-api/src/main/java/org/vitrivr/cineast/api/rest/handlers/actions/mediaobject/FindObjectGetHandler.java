package org.vitrivr.cineast.api.rest.handlers.actions.mediaobject;

import com.google.common.collect.Lists;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.Map;

public class FindObjectGetHandler implements GetRestHandler<MediaObjectQueryResult> {
  
  public static final String ATTRIBUTE_NAME = "attribute";
  public static final String VALUE_NAME = "value";
  
  public static final String ROUTE = "find/object/by/:"+ATTRIBUTE_NAME+"/:"+VALUE_NAME;
  
  private static final Logger LOGGER = LogManager.getLogger(FindObjectGetHandler.class);
  
  
  
  @Override
  public MediaObjectQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
  
    final String attribute = parameters.get(ATTRIBUTE_NAME);
    final String value = parameters.get(VALUE_NAME);
  
    final MediaObjectReader ol = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    MediaObjectDescriptor object = null;
  
    switch (attribute.toLowerCase()) {
      case "id": {
        object = ol.lookUpObjectById(value);
        break;
      }
      case "name": {
        object = ol.lookUpObjectByName(value);
        break;
      }
      case "path": {
        object = ol.lookUpObjectByPath(value);
        break;
      }
      default: {
        LOGGER.error("Unknown attribute '{}' in FindObjectByActionHandler", attribute);
      }
    }
  
    ol.close();
    return new MediaObjectQueryResult("", Lists.newArrayList(object));
  }
  
  @Override
  public Class<MediaObjectQueryResult> outClass() {
    return MediaObjectQueryResult.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @OpenApi(
      summary = "Find object by specified attribute value. I.e by id, name or path",
      path = ROUTE, method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name = ATTRIBUTE_NAME, description = "The attribute type of the value. One of: id, name, path")
      },
      tags= {"Object"},
      responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = MediaObjectQueryResult.class))}
  
  )
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find object by specified attribute value. I.e by id, name or path");
          op.description("Find object by specified attribute value. I.e by id, name or path");
          op.operationId("findObjectsByAttribute");
          op.addTagsItem("Object");
        })
        .pathParam(ATTRIBUTE_NAME, String.class, p -> p.description("The attribute type of the value. One of: id, name, path"))
        .json("200", outClass());
  }
}
