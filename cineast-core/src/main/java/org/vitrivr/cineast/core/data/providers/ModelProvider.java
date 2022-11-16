package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.render.lwjgl.model.IModel;
import org.vitrivr.cineast.core.render.lwjgl.model.Model;
import org.vitrivr.cineast.core.render.lwjgl.model.ModelLoader;

public interface ModelProvider {

  /**
   * Returns a 3D Model. Defaults to the empty Model, if not implemented.
   *
   * @return Model
   */
  default IModel getModel() {
    return lazyCreateModel();
  }
  // TODO Remove when Segment loader is implemented
  default  IModel lazyCreateModel() {
    var model = ModelLoader.loadModel("test",
        "C:\\Users\\rapha\\Documents\\myRepo\\ch.unibas\\Class\\vitrivr\\bsc-raphael-waltenspuel\\datasets_git\\TestSketchfab\\76a3f7bf75d049458dfaa48aa342e0b8\\baluster_vase_from_a_five-piece_garniture\\scene.gltf"
    );
    return model;
  }
}
