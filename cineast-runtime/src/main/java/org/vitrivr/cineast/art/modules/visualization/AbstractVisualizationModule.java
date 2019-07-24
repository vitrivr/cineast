package org.vitrivr.cineast.art.modules.visualization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

/**
 * Created by sein on 26.08.16.
 */
public abstract class AbstractVisualizationModule implements Visualization {
  protected final Map<String, String> tableNames;
  protected final String multimediaobjectTable;
  protected final String segmentTable;
  protected Map<String, DBSelector> selectors;

  protected static final Logger LOGGER = LogManager.getLogger();

  protected AbstractVisualizationModule() {
    this.tableNames = new HashMap<>();
    this.multimediaobjectTable = "cineast_multimediaobject";
    this.segmentTable = "cineast_segment";
  }

  @Override
  public void init(DBSelectorSupplier supplier) {
    selectors = new HashMap<>();

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

  @Override
  public String visualizeSegment(String segmentId) {
    return null;
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){ return null; }

  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){ return null; }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return null;
  }

  @Override
  public abstract List<VisualizationType> getVisualizations();

  @Override
  public void finish() {
    if (selectors != null) {
      for (Map.Entry<String, DBSelector> entry : selectors.entrySet()) {
        entry.getValue().close();
      }
      selectors = null;
    }
  }
}