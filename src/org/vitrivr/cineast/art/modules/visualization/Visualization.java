package org.vitrivr.cineast.art.modules.visualization;

import org.vitrivr.cineast.core.db.DBSelectorSupplier;

import java.util.List;

/**
 * Created by sein on 30.08.16.
 */
public interface Visualization {
  void init(DBSelectorSupplier supplier);
  String getDisplayName();
  List<VisualizationType> getVisualizations();
  VisualizationResult getResultType();
  String visualizeShot(String shotId);
  String visualizeVideo(String videoId);
  void finish();
}
