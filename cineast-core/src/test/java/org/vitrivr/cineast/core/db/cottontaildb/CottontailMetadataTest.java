package org.vitrivr.cineast.core.db.cottontaildb;

import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.db.DBIntegrationTest;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.dao.MetadataTest;
import org.vitrivr.cottontail.client.language.ddl.OptimizeEntity;
import org.vitrivr.cottontail.client.language.dml.Insert;

public class CottontailMetadataTest extends MetadataTest<Insert> {


  private final CottontailIntegrationDBProvider _provider;

  public CottontailMetadataTest() {
    try {
      _provider = new CottontailIntegrationDBProvider();
    } catch (Throwable e) {
      LOGGER.error("Error occurred while starting and connecting to Cottontail DB: " + e.getMessage());
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
  protected IntegrationDBProvider<Insert> provider() {
    return _provider;
  }
}
