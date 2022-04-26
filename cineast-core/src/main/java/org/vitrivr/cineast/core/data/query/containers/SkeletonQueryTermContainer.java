package org.vitrivr.cineast.core.data.query.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.vitrivr.cineast.core.data.Skeleton;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.web.DataURLParser;

public class SkeletonQueryTermContainer extends AbstractQueryTermContainer {

  private final List<Skeleton> skeletons = new ArrayList<>();

  public SkeletonQueryTermContainer(String data) {
    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
    final String converted = DataURLParser.dataURLtoString(data, "application/json");
    final Skeleton[] skeletons = jsonProvider.toObject(converted, Skeleton[].class);
    if (skeletons != null) {
      this.skeletons.addAll(Arrays.asList(skeletons));
    }
  }

  public SkeletonQueryTermContainer(Collection<Skeleton> skeletons) {
    this.skeletons.addAll(skeletons);
  }

  @Override
  public List<Skeleton> getSkeletons() {
    return this.skeletons;
  }
}
