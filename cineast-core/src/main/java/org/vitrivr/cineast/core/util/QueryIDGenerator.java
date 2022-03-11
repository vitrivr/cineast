package org.vitrivr.cineast.core.util;

import org.apache.commons.lang3.RandomStringUtils;

public class QueryIDGenerator {

  public static String generateQueryID() {
    return generateQueryID("");
  }

  public static String generateQueryID(String infix) {
    return "q-" + infix + RandomStringUtils.randomNumeric(3);
  }
}
