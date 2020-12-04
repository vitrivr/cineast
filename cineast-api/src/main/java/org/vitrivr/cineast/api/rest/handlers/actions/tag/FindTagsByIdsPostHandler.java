package org.vitrivr.cineast.api.rest.handlers.actions.tag;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.TagsQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.Collections;
import java.util.List;

public class FindTagsByIdsPostHandler implements ParsingPostRestHandler<IdList, TagsQueryResult> {
  
  // TODO CAUTION: This route has a breaking change in response signature
  
  public static final String ROUTE = "tags/by/id"; // TODO only route not prefixed by find?
  
  private static final TagReader tagReader = new TagReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());

  @Override
  public TagsQueryResult performPost(IdList context, Context ctx) {
    List<Tag> list = Collections.emptyList();
    if (context == null || context.getIds().length == 0) {
      // nothing
    } else {
      list = tagReader.getTagsById(context.getIds());
    }
    return new TagsQueryResult("", list);
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
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find all tags by ids");
          op.addTagsItem("Tag");
          op.operationId("findTagsById");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
