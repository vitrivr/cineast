package org.vitrivr.cineast.core.db.cottontaildb;

import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.db.DBIntegrationTest;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cottontail.client.language.ddl.OptimizeEntity;
import org.vitrivr.cottontail.client.language.dml.Insert;

public class CottontailIntegrationTest extends DBIntegrationTest<Insert> {


  private final CottontailIntegrationDBProvider _provider;

  public CottontailIntegrationTest() {
    _provider = new CottontailIntegrationDBProvider();
  }

  @Override
  public void finishSetup() {
    final CottontailWrapper wrapper = _provider.getWrapper();
    final String fqn = wrapper.fqnInput(this.getTestTextTableName());
    wrapper.client.optimize(new OptimizeEntity(fqn), null);
    wrapper.close();
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
