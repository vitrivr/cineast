package org.vitrivr.cineast.api.rest.routes;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.vitrivr.cineast.api.rest.resolvers.ResolutionResult;
import org.vitrivr.cineast.api.rest.resolvers.Resolver;

import java.util.Map;

public class ResolvedContentRoute implements Handler {

	private Resolver resolver;

	public ResolvedContentRoute(Resolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void handle(Context ctx) throws Exception {
		Map<String, String> params = ctx.pathParamMap();

		String id;

		if (params.containsKey("id")) {
			id = params.get("id");
		} else {
			ctx.status(400);
			ctx.result("Bad request");
			return;
		}

		ResolutionResult rresult = this.resolver.resolve(id);

		if (rresult == null) {
			ctx.status(400);
			ctx.result("Bad request");
			return;
		}

		ctx.contentType(rresult.mimeType); // Can be removed with the next javalin release
		ctx.seekableStream(rresult.stream, rresult.mimeType);
		ctx.status(200);
	}
}
