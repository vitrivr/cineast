package org.vitrivr.cineast.core.db.polyphenydb;

import java.sql.PreparedStatement;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.db.DBIntegrationTest;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailIntegrationDBProvider;
import org.vitrivr.cottontail.client.language.dml.Insert;

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
}
