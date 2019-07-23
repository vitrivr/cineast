package org.vitrivr.cineast.core.data.entities;

public class SimpleFulltextFeatureDescriptor {

  public static final String[] FIELDNAMES = {"id", "feature"};

  /**
   * ID of the {@link MediaSegmentDescriptor} this {@link SimpleFulltextFeatureDescriptor} belongs to.
   */
  public final String segmentId;

  /**
   * Text that is contained in this {@link SimpleFulltextFeatureDescriptor}.
   */
  public final String feature;

  public SimpleFulltextFeatureDescriptor(String segmentId, String feature) {
    this.segmentId = segmentId;
    this.feature = feature;
  }

  public String getSegmentId() {
    return this.segmentId;
  }

  public String getFeature() {
    return this.feature;
  }
}
