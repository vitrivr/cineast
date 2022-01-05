package org.vitrivr.cineast.core.db.polyphenydb;

import java.sql.PreparedStatement;
import org.vitrivr.cineast.core.db.DBBooleanIntegrationTest;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailIntegrationDBProvider;
import org.vitrivr.cottontail.client.language.dml.Insert;

public class PolyphenyBooleanIntegrationTest extends DBBooleanIntegrationTest<PreparedStatement> {

  private final PolyphenyIntegrationDBProvider _provider;

  public PolyphenyBooleanIntegrationTest() {
    try {
      _provider = new PolyphenyIntegrationDBProvider();
    } catch (Throwable e) {
      LOGGER.error("Error occurred while starting and connecting to Cottontail DB: " + e.getMessage());
      throw e;
    }
  }

  @Override
  protected void finishSetup() {
  }

  @Override
  protected IntegrationDBProvider<PreparedStatement> provider() {
    return _provider;
  }
}
