package org.vitrivr.cineast.core.util.pose;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

public class PoseSpec {
  private TreeSet<Integer> nodes;

  private void collectNumbers(JsonNode node) {
    if (node.isObject()) {
      for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
        JsonNode child = it.next().getValue();
        collectNumbers(child);
      }
    } else {
      for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
        JsonNode child = it.next();
        this.nodes.add(child.asInt());
      }
    }
  }

  public PoseSpec(JsonNode node) {
    this.nodes = new TreeSet<>();
    this.collectNumbers(node.get("lines"));
  }

  public boolean hasAll(float[][] pose) {
    for (Integer nodeIdx : this.nodes) {
      if (pose[nodeIdx][2] <= 0) {
        System.out.printf("idx %d not present %s\n", nodeIdx, Arrays.toString(pose[nodeIdx]));
        return false;
      }
    }
    return true;
  }

  public float[][] subset(float[][] pose) {
    float[][] result = new float[this.numNodes()][];
    int resIdx = 0;
    for (int idx : this.nodes) {
      result[resIdx] = pose[idx];
      resIdx++;
    }
    return result;
  }

  public int numNodes() {
    return this.nodes.size();
  }
}
