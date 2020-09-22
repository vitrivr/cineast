package org.vitrivr.cineast.core.data.entities;

import org.vitrivr.cineast.core.data.tag.Tag;

public class TagInstance {

  public final Tag tag;
  public final String id;

  public TagInstance(String id, Tag tag) {
    this.id = id;
    this.tag = tag;
}

}
