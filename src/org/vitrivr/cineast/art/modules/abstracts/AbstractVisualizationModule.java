package org.vitrivr.cineast.art.modules.abstracts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.art.modules.visualization.VisualizationCache;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 26.08.16.
 */
public abstract class AbstractVisualizationModule implements Visualization {
  protected final Map<String, String> tableNames;
  protected final String multimediaobjectTable;
  protected final String segmentTable;
  protected Map<String, DBSelector> selectors;
  protected VisualizationCache visualizationCache;

  protected static final Logger LOGGER = LogManager.getLogger();

  protected AbstractVisualizationModule() {
    this.tableNames = new HashMap();
    this.multimediaobjectTable = "cineast_multimediaobject";
    this.segmentTable = "cineast_segment";
  }

  public void init(DBSelectorSupplier supplier) {
    visualizationCache = new VisualizationCache();
    selectors = new HashMap();

    DBSelector selector;
    for (Map.Entry<String, String> entry : tableNames.entrySet()) {
      selector = supplier.get();
      selector.open(entry.getValue());
      selectors.put(entry.getKey(), selector);
    }

    selector = supplier.get();
    selector.open(multimediaobjectTable);
    selectors.put(multimediaobjectTable, selector);

    selector = supplier.get();
    selector.open(segmentTable);
    selectors.put(segmentTable, selector);
  }

  public String visualizeSegment(String segmentId) {
    return null;
  }

  public String visualizeMultimediaobject(String multimediaobjectId) {
    return null;
  }

  public abstract List<VisualizationType> getVisualizations();

  public void finish() {
    if (selectors != null) {
      for (Map.Entry<String, DBSelector> entry : selectors.entrySet()) {
        entry.getValue().close();
      }
      selectors = null;
    }
  }
}