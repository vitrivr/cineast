package org.vitrivr.cineast.core.iiif.presentationapi;

import java.util.List;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.Sequence;

public interface Manifest {

  List<Sequence> getSequences();

  String getSummary();

  String getId();

  String getRequiredStatement();

  List<Object> getMetadata();
}
