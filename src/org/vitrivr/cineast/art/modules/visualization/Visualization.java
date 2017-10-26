package org.vitrivr.cineast.art.modules.visualization;

import java.util.List;

import org.vitrivr.cineast.core.db.DBSelectorSupplier;

/**
 * Created by sein on 30.08.16.
 */
public interface Visualization {
  void init(DBSelectorSupplier supplier);

  String getDisplayName();

  List<VisualizationType> getVisualizations();

  VisualizationResult getResultType();

  String visualizeSegment(String segmentId);

  String visualizeMultipleSegments(List<String> segmentIds);

  String visualizeMultimediaobject(String multimediaobjectId);

  void finish();
}
