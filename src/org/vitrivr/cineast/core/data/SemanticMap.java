package org.vitrivr.cineast.core.data;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabLabel;

public class SemanticMap {

  private final DeepLabLabel[][] labels;

  public SemanticMap(BufferedImage image, Map<String, String> classes) {

    TIntObjectHashMap<DeepLabLabel> intToLabelMap = new TIntObjectHashMap<>(classes.size());
    for (String className : classes.keySet()) {

      DeepLabLabel label;

      try {
        label = DeepLabLabel.valueOf(className);
      } catch (IllegalArgumentException e) {
        continue;
      }

      int color = ReadableRGBContainer.fromColorString(classes.get(className)).toIntColor();

      intToLabelMap.put(color, label);

    }

    labels = new DeepLabLabel[image.getHeight()][image.getWidth()];

    for (int x = 0; x < image.getWidth(); ++x) {
      for (int y = 0; y < image.getHeight(); ++y) {
        int c = image.getRGB(x, y);
        DeepLabLabel label = intToLabelMap.get(c);
        if (label == null) {
          label = DeepLabLabel.NOTHING;
        }
        labels[y][x] = label;

      }
    }

  }

  public DeepLabLabel[][] getLabels() {
    return this.labels;
  }

}
