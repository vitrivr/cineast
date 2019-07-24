package org.vitrivr.cineast.api.rest.routes;

import org.vitrivr.cineast.api.rest.resolvers.ResolutionResult;
import org.vitrivr.cineast.api.rest.resolvers.Resolver;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.GzipUtils;
import spark.utils.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class ResolvedContentRoute implements Route {

  private Resolver resolver;

  public ResolvedContentRoute(Resolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {

    Map<String, String> params = request.params();

    String id;

    if (params != null && params.containsKey(":id")) {
      id = params.get(":id");
    } else {
      response.status(400);
      response.raw().getWriter().write("Bad request");
      response.raw().getWriter().flush();
      return 404;
    }

    ResolutionResult rresult = this.resolver.resolve(id);

    if (rresult == null) {
      response.status(400);
      response.raw().getWriter().write("Bad request");
      response.raw().getWriter().flush();
      return 404;
    }

    response.type(rresult.mimeType);

    try (InputStream inputStream = rresult.stream;
        OutputStream wrappedOutputStream =
            GzipUtils.checkAndWrap(request.raw(), response.raw(), false)) {
      IOUtils.copy(inputStream, wrappedOutputStream);
      wrappedOutputStream.flush();
      response.raw().getOutputStream().close();
    }

    return 200;
  }
}
