package org.vitrivr.cineast.api.rest.handlers.actions.tag;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import org.vitrivr.cineast.api.messages.result.TagsQueryResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindTagsAllGetHandler implements GetRestHandler<TagsQueryResult> {
  
  // TODO CAUTION: This route has a breaking change in response signature
  
  public static final String ROUTE = "find/tags/all";
  
  private static TagReader tagReader = new TagReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  
  {
    tagReader.initCache();
  }
  
  @OpenApi(
      summary = "Find all tags",
      path = ROUTE, method = HttpMethod.GET,
      tags = {"Tag"},
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = TagsQueryResult.class))
      }
  )
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
}
