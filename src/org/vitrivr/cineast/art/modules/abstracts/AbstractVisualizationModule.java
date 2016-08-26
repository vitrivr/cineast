package org.vitrivr.cineast.art.modules.abstracts;

import org.vitrivr.cineast.core.db.DBSelector;

/**
 * Created by sein on 26.08.16.
 */
public abstract class AbstractVisualizationModule {
  protected final String tableName;
  protected DBSelector selector;

  protected AbstractVisualizationModule(String tableName){
    this.tableName = tableName;
  }

  public void init(DBSelector selector) {
    this.selector = selector;
    this.selector.open(this.tableName);
  }
}
