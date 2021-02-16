package org.vitrivr.cineast.api.rest.handlers.actions.tag;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.result.TagsQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindTagsGetHandler implements GetRestHandler<TagsQueryResult> {
  
  // TODO CAUTION: This route has a breaking change in response signature
  
  public static final String ID_NAME = "id";
  public static final String NAME_NAME = "name";
  public static final String MATCHING_NAME = "matchingname";
  
  public static final String ATTRIBUTE_NAME = "attribute";
  public static final String VALUE_NAME = "value";
  
  public static final String ROUTE = "find/tags/by/:" + ATTRIBUTE_NAME + "/:" + VALUE_NAME;
  
  private static final Logger LOGGER = LogManager.getLogger(FindTagsGetHandler.class);
  
  private static final TagReader tagReader = new TagReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());

  @Override
  public TagsQueryResult doGet(Context ctx) {
    final Map<String, String> parameters = ctx.pathParamMap();
    final String attribute = parameters.get(ATTRIBUTE_NAME);
    final String value = parameters.get(VALUE_NAME);
    List<Tag> list = new ArrayList<>(1);
    switch (attribute.toLowerCase()) {
      case ID_NAME:
        list.add(tagReader.getTagById(value));
        break;
      case NAME_NAME:
        list = tagReader.getTagsByName(value);
        break;
      case MATCHING_NAME:
        list = tagReader.getTagsByMatchingName(value);
        break;
      default:
        LOGGER.error("Unknown attribute '{}' in FindTagsByActionHandler", attribute);
        list = new ArrayList<>(0);
    }
    return new TagsQueryResult("", list);
  }
  
  @Override
  public Class<TagsQueryResult> outClass() {
    return TagsQueryResult.class;
  }
  
  @Override
  public String route() {
    return ROUTE;
  }
  
  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find all tags specified by attribute value");
          op.description("Find all tags by attributes id, name or matchingname and filter value");
          op.operationId("findTagsBy");
          op.addTagsItem("Tag");
        })
        .pathParam(ATTRIBUTE_NAME, String.class, p -> p.description("The attribute to filter on. One of: id, name, " + MATCHING_NAME))
        .pathParam(VALUE_NAME, String.class, p -> p.description("The value of the attribute to filter"))
        .json("200", outClass());
    
  }
}
