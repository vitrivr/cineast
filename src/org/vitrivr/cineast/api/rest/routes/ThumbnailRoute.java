package org.vitrivr.cineast.api.rest.routes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import org.vitrivr.cineast.api.rest.resolvers.ThumbnailResolver;
import spark.Request;
import spark.Response;
import spark.Route;


public class ThumbnailRoute implements Route {

  private ThumbnailResolver resolver;

  public ThumbnailRoute(ThumbnailResolver resolver){
    this.resolver = resolver;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {

    //TODO content type

    Map<String, String> params = request.params();

    String id = null;

    if (params != null && params.containsKey(":id")){
      id = params.get(":id");
    }else{
      return 404;
    }

    InputStream inStream = this.resolver.resolve(id);

    if(inStream == null){
      return 404;
    }

    OutputStream out = response.raw().getOutputStream();

    final ReadableByteChannel inputChannel = Channels.newChannel(inStream);
    final WritableByteChannel outputChannel = Channels.newChannel(out);

    fastCopy(inputChannel, outputChannel);

    return 200;
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
