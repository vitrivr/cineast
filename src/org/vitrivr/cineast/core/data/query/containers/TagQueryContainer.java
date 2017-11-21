package org.vitrivr.cineast.core.data.query.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vitrivr.cineast.core.data.tag.Tag;

public class TagQueryContainer extends QueryContainer {

  private final List<Tag> tags;
  
  public TagQueryContainer(Collection<Tag> tags){
    ArrayList<Tag> tmp = new ArrayList<>(tags != null ? tags.size() : 0);
    if(tags != null){
      tmp.addAll(tags);
    }
    this.tags = Collections.unmodifiableList(tmp);
  }

  @Override
  public List<Tag> getTags() {
    return this.tags;
  }
  
  
  
}
