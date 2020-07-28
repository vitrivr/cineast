package org.vitrivr.cineast.api.rest.handlers.actions.tag;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.vitrivr.cineast.api.messages.result.TagsQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindTagsAllGetHandler implements GetRestHandler<TagsQueryResult> {
  
  // TODO CAUTION: This route has a breaking change in response signature
  
  public static final String ROUTE = "find/tags/all";
  
  private static final TagReader tagReader = new TagReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  
  @Override
  public TagsQueryResult doGet(Context ctx) {
    return new TagsQueryResult("", tagReader.getAllCached());
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
          op.summary("Find all tags");
          op.operationId("findAllTags");
          op.addTagsItem("Tag");
        })
        .json("200", outClass());
    
  }
}
