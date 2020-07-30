package org.vitrivr.cineast.standalone.importer.redhen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class PoseHdf5Importer implements Importer<Pair<String, float[]>> {
  private final PoseHdf5Generator inner;
  private final String poseSpecName;
  private boolean exhausted;

  public PoseHdf5Importer(PoseHdf5Generator inner, String poseSpecName) {
    this.inner = inner;
    this.poseSpecName = poseSpecName;
    this.exhausted = false;
  }

  @Override
  public Pair<String, float[]> readNext() {
    if (this.exhausted) {
      return null;
    }
    try {
      Optional<Pair<String, float[]>> result = this.inner.takeFrom(this.poseSpecName);
      if (!result.isPresent()) {
        this.exhausted = true;
        return null;
      }
      return result.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, float[]> data) {
    HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put("id", new StringTypeProvider(data.getLeft()));
    map.put("feature", new FloatArrayTypeProvider(data.getRight()));
    return map;
  }
}
