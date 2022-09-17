package org.vitrivr.cineast.core.iiif;

import java.io.Serial;

public class UnsupportedIIIFAPIException extends Exception {

  @Serial
  private static final long serialVersionUID = -7000364400585662926L;

  public UnsupportedIIIFAPIException(String error) {
    super(error);
  }
}
