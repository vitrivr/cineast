package org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab;

public class DeepLabCityscapes extends DeepLab {

  public DeepLabCityscapes() {
    super(load("resources/DeepLab/cityscapes.pb"),
        new String[]{
            "dummy", "road", "sidewalk", "parking", "rail track", "person", "rider", "car", "truck",
            "bus", "on rails", "motorcycle", "bicycle", "caravan", "trailer", "building", "wall",
            "fence", "guard rail", "bridge", "tunnel", "pole", "pole group", "traffic sign",
            "traffic light", "vegetation", "terrain", "sky", "ground", "dynamic", "static"
        }
    );
  }
}
