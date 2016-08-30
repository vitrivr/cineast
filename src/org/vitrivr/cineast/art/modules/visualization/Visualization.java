package org.vitrivr.cineast.art.modules.visualization;

import java.util.List;

/**
 * Created by sein on 30.08.16.
 */
public interface Visualization {
  public String getDisplayName();
  public List<VisualizationType> getVisualizations();
}
