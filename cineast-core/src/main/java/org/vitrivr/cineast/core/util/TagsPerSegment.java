package org.vitrivr.cineast.core.util;

import java.util.Set;

public class TagsPerSegment {

  public Set<String> tags;
  public String couldSegmentID;

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  public String getCouldSegmentID() {
    return couldSegmentID;
  }


  public TagsPerSegment(String couldSegmentID, Set<String> tags) {
    this.couldSegmentID = couldSegmentID;
    this.tags = tags;
  }


  public boolean addTags(String tag) {
    return this.tags.add(tag);
  }


}


