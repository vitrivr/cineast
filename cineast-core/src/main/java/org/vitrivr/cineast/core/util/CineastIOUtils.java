package org.vitrivr.cineast.core.util;

import java.io.Closeable;
import org.apache.commons.io.IOUtils;

/**
 * Since {@link IOUtils#closeQuietly(Closeable)} is deprecated, we're offering a convenience-method which also ignores exceptions
 */
public class CineastIOUtils {

  public static void closeQuietly(Closeable closeable) {
    IOUtils.closeQuietly(closeable, e -> {
    });
  }

  public static void closeQuietly(Closeable... closeables) {
    for (Closeable closeable : closeables) {
      closeQuietly(closeable);
    }
  }

}
