package org.vitrivr.cineast.core.util;

import org.apache.commons.lang3.RandomStringUtils;

public class QueryIDGenerator {

  public static String generateQueryID() {
    return "q-"+RandomStringUtils.randomNumeric(3);
  }
}
