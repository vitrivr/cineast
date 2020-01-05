package org.vitrivr.cineast.core.features.abstracts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.query.containers.TextQueryContainer;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

public class AbstractTextRetrieverTest {

  private AbstractTextRetriever retriever = new AbstractTextRetriever("test-abstract") {
  };

  @DisplayName("quoted string is not split")
  @Test
  public void testQuotedStringNotSplit() {
    testMatch("\"hello world\"", "\"hello world\"");
  }

  @DisplayName("double quoted string is split")
  @Test
  public void testDoubleQuotedStringSplit() {
    testMatch("\"hello\" \"world\"", "\"hello\"", "\"world\"");
  }

  @DisplayName("non-quoted string is split")
  @Test
  public void testNonQuotedStringSplit() {
    testMatch("hello world", "hello", "world");
  }

  public void testMatch(String input, String... output) {
    org.junit.jupiter.api.Assertions.assertArrayEquals(output, retriever.generateQuery(new TextQueryContainer(input), new QueryConfig(null)));
  }

  public SegmentContainer generateSegmentContainerFromText(String text) {
    return new TextQueryContainer(text);
  }

}
