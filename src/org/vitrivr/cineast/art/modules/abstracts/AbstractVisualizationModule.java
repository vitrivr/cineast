package org.vitrivr.cineast.art.modules.abstracts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
  protected final String moviesTable;
  protected final String shotsTable;
  protected Map<String, DBSelector> selectors;

  protected static final Logger LOGGER = LogManager.getLogger();

  protected AbstractVisualizationModule(String tableName){
    this.tableName = tableName;
    this.moviesTable = "cineast_multimediaobject";
    this.shotsTable = "cineast_segment";
  }

  public void init(DBSelectorSupplier supplier){
    selectors = new HashMap();

    DBSelector selector = supplier.get();
    selector.open(tableName);
    selectors.put(tableName, selector);

    selector = supplier.get();
    selector.open(moviesTable);
    selectors.put(moviesTable, selector);

    selector = supplier.get();
    selector.open(shotsTable);
    selectors.put(shotsTable, selector);
  }

  public String visualizeSegment(String segmentId){
    return null;
  }
  public String visualizeMultimediaobject(String multimediaobjectId){
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