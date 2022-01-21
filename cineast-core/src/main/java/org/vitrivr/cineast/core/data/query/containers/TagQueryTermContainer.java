package org.vitrivr.cineast.core.data.query.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.vitrivr.cineast.core.data.tag.IncompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.data.tag.WeightedTag;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.web.DataURLParser;


public class TagQueryTermContainer extends AbstractQueryTermContainer { // vitrivr pendant: TagQueryTerm

  /**
   * List of {@link Tag}s contained in this {@link TagQueryTermContainer}.
   */
  private final List<Tag> tags;

  /**
   * Constructs an {@link TagQueryTermContainer} from base 64 encoded JSON data.
   *
   * @param data The tag data that should be converted.
   */
  public TagQueryTermContainer(String data) {
    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
    final String converted = DataURLParser.dataURLtoString(data, "application/json");
    final WeightedTag[] tags = jsonProvider.toObject(converted, IncompleteTag[].class);
    if (tags != null) {
      this.tags = Arrays.asList(tags);
    } else {
      this.tags = new ArrayList<>(0);
    }
  }

  public TagQueryTermContainer(Collection<Tag> tags) {
    ArrayList<Tag> tmp = new ArrayList<>(tags != null ? tags.size() : 0);
    if (tags != null) {
      tmp.addAll(tags);
    }
    this.tags = Collections.unmodifiableList(tmp);
  }


  @Override
  public List<Tag> getTags() {
    return this.tags;
  }
}
