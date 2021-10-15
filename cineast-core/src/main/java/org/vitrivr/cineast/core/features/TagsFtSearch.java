package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

public class TagsFtSearch extends AbstractTextRetriever {

  public static final String TAGS_FT_TABLE_NAME = "features_tagsft";

  /**
   * Default constructor for {@link TagsFtSearch}.
   */
  public TagsFtSearch() {
    super(TagsFtSearch.TAGS_FT_TABLE_NAME);
  }
}