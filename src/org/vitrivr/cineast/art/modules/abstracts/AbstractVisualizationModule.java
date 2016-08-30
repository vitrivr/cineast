package org.vitrivr.cineast.art.modules.abstracts;

import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 26.08.16.
 */
public abstract class AbstractVisualizationModule implements Visualization{
  protected final String tableName;
  protected Map<String, DBSelector> selectors;

  protected AbstractVisualizationModule(String tableName){
    this.tableName = tableName;
  }

  public void init(DBSelectorSupplier supplier){
    selectors = new HashMap();
    DBSelector selector = supplier.get();
    selector.open(tableName);
    selectors.put(tableName, selector);
  }

  public String visualizeShot(String shotId){
    return null;
  }
  public String visualizeVideo(String videoId){
    return null;
  }

  public abstract List<VisualizationType> getVisualizations();

  public void finish(){
    if(selectors != null){
      for(Map.Entry<String, DBSelector> entry: selectors.entrySet()){
        entry.getValue().close();
      }
      selectors = null;
    }
  }
}