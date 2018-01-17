package org.vitrivr.cineast.api.rest.routes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import org.vitrivr.cineast.api.rest.resolvers.ResolutionResult;
import org.vitrivr.cineast.api.rest.resolvers.Resolver;
import spark.Request;
import spark.Response;
import spark.Route;


public class ResolvedContentRoute implements Route {

  private Resolver resolver;

  public ResolvedContentRoute(Resolver resolver){
    this.resolver = resolver;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {

    Map<String, String> params = request.params();

    String id = null;

    if (params != null && params.containsKey(":id")){
      id = params.get(":id");
    }else{
      response.status(404);
      return 404;
    }

    ResolutionResult rresult = this.resolver.resolve(id);

    if(rresult == null){
      response.status(404);
      return 404;
    }


    response.type(rresult.mimeType);
    response.header("Transfer-Encoding", "identity");
    InputStream inStream = rresult.stream;

    OutputStream out = response.raw().getOutputStream();

    final ReadableByteChannel inputChannel = Channels.newChannel(inStream);
    final WritableByteChannel outputChannel = Channels.newChannel(out);

    fastCopy(inputChannel, outputChannel);

    out.flush();

    return null;
  }

  public static void fastCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
    final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

    while(src.read(buffer) != -1) {
      buffer.flip();
      dest.write(buffer);
      buffer.compact();
    }

    buffer.flip();

    while(buffer.hasRemaining()) {
      dest.write(buffer);
    }

  }
}
