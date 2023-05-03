package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;

public interface ModelProvider {

  /**
   * Returns a 3D Model. Defaults to the empty Model, if not implemented.
   *
   * @return Model
   */
  default IModel getModel() {
    return Model.EMPTY;
  }
}
