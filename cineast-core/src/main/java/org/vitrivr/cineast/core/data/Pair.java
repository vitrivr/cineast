package org.vitrivr.cineast.core.data;

import java.util.Objects;

public class Pair<K, V> {

  public K first;
  public V second;

  public Pair(K first, V second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public String toString() {
    return "Pair(" + this.first + ", " + this.second + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }
}
