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
    _provider = new CottontailIntegrationDBProvider();
  }

  @Override
  public void finishInitialSetup() {
    final CottontailWrapper wrapper = _provider.getWrapper();
    final String obj = wrapper.fqnInput(this.getTestObjMetaTableName());
    final String seg = wrapper.fqnInput(this.getTestSegMetaTableName());
    wrapper.client.optimize(new OptimizeEntity(obj));
    wrapper.client.optimize(new OptimizeEntity(seg));
    wrapper.close();
  }

  @Override
  protected void cleanup() {
    this._provider.close();
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
