package org.vitrivr.cineast.core.iiif.presentationapi;

import java.util.List;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.Sequence;

public abstract class Manifest {

  public abstract List<Sequence> getSequences();

  public abstract String getSummary();

  public abstract String getId();

  public abstract String getRequiredStatement();

  public abstract List<Object> getMetadata();
}
