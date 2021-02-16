package org.vitrivr.cineast.core.util;

public class CineastConstants {

  /**
   * Almost ALL columns where features (vectors, text etc.) are stored have this name.
   */
  public static final String FEATURE_COLUMN_QUALIFIER = "feature";

  /**
   * Use this column qualifier if it might refer to an object OR segment id
   */
  public static final String GENERIC_ID_COLUMN_QUALIFIER = "id";

  /**
   * Use this column qualifier if it might refer to a segment id
   */
  public static final String SEGMENT_ID_COLUMN_QUALIFIER = "segmentid";

  /**
   * In some legacy, adampro return "ap_distance" instead of the true distance.
   * In an effort to unify the codebase, use this constant whenever you are expecting a distance or score from a query to the db
   */
  public static final String DB_DISTANCE_VALUE_QUALIFIER = "distance";

}
