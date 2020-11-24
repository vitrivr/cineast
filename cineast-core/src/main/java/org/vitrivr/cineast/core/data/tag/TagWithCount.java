package org.vitrivr.cineast.core.data.tag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object that extends a tag with its number of occurrences
 */
public class TagWithCount extends CompleteTag {

  @JsonProperty
  public int getCount() {
    return count;
  }

  private int count;

  public TagWithCount(String id, String name, String description,
      Preference preference, int count) {
    super(id, name, description, preference);
    this.count = count;
  }

  public TagWithCount(Tag tag, int count) {
    super(tag.getId(), tag.getName(), tag.getDescription(), tag.getPreference());
    this.count = count;
  }


}
