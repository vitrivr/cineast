package org.vitrivr.cineast.core.db.polyphenydb;


import java.sql.PreparedStatement;
import java.util.HashMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.db.DBIntegrationTest;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;

public class PolyphenyIntegrationTest extends DBIntegrationTest<PreparedStatement> {


  private final PolyphenyIntegrationDBProvider _provider;

  public PolyphenyIntegrationTest() {
    try {
      _provider = new PolyphenyIntegrationDBProvider();
    } catch (Throwable e) {
      LOGGER.error("Error occurred while starting and connecting to Polypheny: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Create both a table for vector retrieval & text retrieval
   */
  @Override
  protected void createTables() {
    final HashMap<String, String> hints = new HashMap<>();
    hints.put("pk", "true");
    this.ec.createEntity(testTextTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.STRING, hints), new AttributeDefinition(TEXT_COL_NAME, AttributeType.TEXT));
    this.ec.createEntity(testVectorTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.STRING, hints), new AttributeDefinition(FEATURE_VECTOR_COL_NAME, AttributeType.VECTOR, 3));
  }

  @Override
  public void finishSetup() {
    //no-op
  }

  @Test
  protected void simpleTest() {
    //no-op
  }

  @Override
  protected IntegrationDBProvider<PreparedStatement> provider() {
    return _provider;
  }

  /**
   * This test verifies that a simple "hello" query retrieves exact and partial matches, but no fuzziness
   */
  @Test
  @DisplayName("Text: One el, no quotes")
  @Disabled
  @Override
  public void textRetrievalSingleLike() {
    /* TODO: Not supported by Polypheny DB yet. */
  }

  @Test
  @DisplayName("Text: two words, inverted, no quotes")
  @Disabled
  @Override
  public void textRetrievalSingleTwoWordsLike() {
    /* TODO: Not supported by Polypheny DB yet. */
  }

  @Test
  @DisplayName("Text: One el (two words), quotes")
  @Disabled
  @Override
  public void textRetrievalSingleTwoWordsQuotedLike() {
    /* TODO: Not supported by Polypheny DB yet. */
  }

  @Test
  @DisplayName("Text: One el, one word, Fuzzy")
  @Disabled
  @Override
  public void testRetrievalSingleFuzzy() {
    /* TODO: Not supported by Polypheny DB yet. */
  }


  @Test
  @DisplayName("Text: Two elements w/ single word")
  @Disabled
  @Override
  public void testRetrievalTwo() {
    /* TODO: Not supported by Polypheny DB yet. */
  }

  @Test
  @DisplayName("Text: Three elements, two are a match for the same id")
  @Disabled
  @Override
  public void testRetrievalThreeDouble() {
    /* TODO: Not supported by Polypheny DB yet. */
  }

  @Test
  @DisplayName("Text: Three els, one of those with quotes")
  @Disabled
  @Override
  public void testRetrievalThree() {
    /* TODO: Not supported by Polypheny DB yet. */
  }
}
