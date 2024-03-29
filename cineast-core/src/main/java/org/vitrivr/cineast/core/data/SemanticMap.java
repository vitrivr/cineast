package org.vitrivr.cineast.core.data;

import com.carrotsearch.hppc.IntObjectHashMap;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabLabel;

public record SemanticMap(DeepLabLabel[][] labels) {


  public SemanticMap(BufferedImage image, Map<String, String> classes) {
    this(toLabels(image, classes));
  }

  private static DeepLabLabel[][] toLabels(BufferedImage image, Map<String, String> classes) {
    IntObjectHashMap<DeepLabLabel> intToLabelMap = new IntObjectHashMap<>(classes.size());
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

    var labels = new DeepLabLabel[image.getHeight()][image.getWidth()];

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
    return labels;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SemanticMap that = (SemanticMap) o;
    return Arrays.deepEquals(labels, that.labels);
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(labels);
  }
}
