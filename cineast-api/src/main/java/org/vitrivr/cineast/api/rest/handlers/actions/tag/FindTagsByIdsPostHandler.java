package org.vitrivr.cineast.api.rest.handlers.actions.tag;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.TagsQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindTagsByIdsPostHandler implements ParsingPostRestHandler<IdList, TagsQueryResult> {

  public static final String ROUTE = "tags/by/id"; // TODO only route not prefixed by find?
  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public TagsQueryResult performPost(IdList context, Context ctx) {
    if (context == null || context.ids().isEmpty()) {
      LOGGER.warn("no ids provided, returning empty list");
      return new TagsQueryResult("", new ArrayList<>(0));
    }
    try (var tr = new TagReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get())) {
      return new TagsQueryResult("", tr.getTagsById(context.ids()));
    }
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
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
    return OpenApiBuilder.document().operation(op -> {
      op.summary("Find all tags by ids");
      op.addTagsItem("Tag");
      op.operationId("findTagsById");
    }).body(inClass()).json("200", outClass());
  }
}
