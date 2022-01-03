package org.vitrivr.cineast.core.db.cottontaildb;

import org.vitrivr.cineast.core.db.DBBooleanIntegrationTest;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cottontail.client.language.dml.Insert;

public class CottontailBooleanIntegrationTest extends DBBooleanIntegrationTest<Insert> {

  private final CottontailIntegrationDBProvider _provider;

  public CottontailBooleanIntegrationTest() {
    try {
      _provider = new CottontailIntegrationDBProvider();
    } catch (Throwable e) {
      LOGGER.error("Error occurred while starting and connecting to Cottontail DB: " + e.getMessage());
      throw e;
    }
  }

  @Override
  protected void finishSetup() {
  }

  @Override
  protected IntegrationDBProvider<Insert> provider() {
    return _provider;
  }
}
