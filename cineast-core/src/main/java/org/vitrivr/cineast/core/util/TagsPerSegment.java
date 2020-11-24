package org.vitrivr.cineast.core.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TagsPerSegment {

  public Set<String> tags;
  public String segmentID;

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  public String getSegmentID() {
    return segmentID;
  }


  public TagsPerSegment(String segmentID, Set<String> tags) {
    this.segmentID = segmentID;
    this.tags = tags;
  }

  public TagsPerSegment(String segmentID, String tag) {
    this.segmentID = segmentID;
    this.tags = new HashSet<>(Collections.singleton(tag));
  }


  public boolean addTags(String tag) {
    return this.tags.add(tag);
  }


}


