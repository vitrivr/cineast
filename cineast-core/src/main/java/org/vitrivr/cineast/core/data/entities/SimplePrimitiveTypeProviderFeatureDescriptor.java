package org.vitrivr.cineast.core.data.entities;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public class SimplePrimitiveTypeProviderFeatureDescriptor {

  public static final String[] FIELDNAMES = {GENERIC_ID_COLUMN_QUALIFIER, FEATURE_COLUMN_QUALIFIER};

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
