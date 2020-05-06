package org.vitrivr.cineast.api.rest.routes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.vitrivr.cineast.api.rest.resolvers.ResolutionResult;
import org.vitrivr.cineast.api.rest.resolvers.Resolver;

import io.javalin.http.Context;
import io.javalin.http.Handler;

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

		ctx.seekableStream(rresult.stream, rresult.mimeType);
		ctx.status(200);
	}
}
