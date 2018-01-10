package org.vitrivr.cineast.api.rest.resolvers;

import java.io.InputStream;

public class ResolutionResult {

  public final String mimeType;
  public final InputStream stream;

  public ResolutionResult(String mimeType, InputStream stream){
    this.mimeType = mimeType;
    this.stream = stream;
  }

}
