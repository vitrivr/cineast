package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.vitrivr.cineast.core.util.MimeTypeHelper;

public class ResolutionResult {

  public final String mimeType;
  public final InputStream stream;

  public ResolutionResult(String mimeType, InputStream stream){
    this.mimeType = mimeType;
    this.stream = stream;
  }

  public ResolutionResult(File file) throws FileNotFoundException {
    this(
        MimeTypeHelper.getContentType(file),
        new FileInputStream(file)
    );
  }

}
