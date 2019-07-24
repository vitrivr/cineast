package org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab;

public class DeepLabPascalVoc extends DeepLab {

  public DeepLabPascalVoc() {
    super(load("resources/DeepLab/pascalvoc.pb"),
        new String[]{
            "dummy", "Person", "Car", "Bicycle", "Bus", "Motorbike", "Train", "Aeroplane", "Chair",
            "Bottle", "Dining Table", "Potted Plant", "TV/Monitor", "Sofa", "Bird", "Cat", "Cow",
            "Dog", "Horse", "Sheep"
        }
    );
  }
}
