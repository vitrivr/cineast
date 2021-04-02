package org.vitrivr.cineast.core.db;

import org.vitrivr.cineast.core.db.setup.EntityCreator;

public interface IntegrationDBProvider<R> {


  /**
   * @return an {@link PersistencyWriter} which will be used to fill data into the underlying database
   */
  PersistencyWriter<R> getPersistencyWriter();

  /**
   * As implementing test, you are responsible on whether this returns a new selector instance or a previously reused selector instance.
   */
  DBSelector getSelector();

  EntityCreator getEntityCreator();

}
