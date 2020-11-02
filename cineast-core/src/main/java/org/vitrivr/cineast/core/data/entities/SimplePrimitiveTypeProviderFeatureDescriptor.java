package org.vitrivr.cineast.core.data.entities;

import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public class SimplePrimitiveTypeProviderFeatureDescriptor {

  public static final String[] FIELDNAMES = {"id", "feature"};

  /**
   * ID of the {@link MediaSegmentDescriptor} this {@link SimplePrimitiveTypeProviderFeatureDescriptor} belongs to.
   */
  public final String segmentId;

  /**
   * Text that is contained in this {@link SimplePrimitiveTypeProviderFeatureDescriptor}.
   */
  public final PrimitiveTypeProvider feature;

  public SimplePrimitiveTypeProviderFeatureDescriptor(String segmentId, PrimitiveTypeProvider feature) {
    this.segmentId = segmentId;
    this.feature = feature;
  }

  public String getSegmentId() {
    return this.segmentId;
  }

  public PrimitiveTypeProvider getFeature() {
    return this.feature;
  }
}
