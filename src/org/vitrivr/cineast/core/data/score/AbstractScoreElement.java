package org.vitrivr.cineast.core.data.score;

import java.util.Objects;

abstract class AbstractScoreElement implements ScoreElement {
  private final String id;
  private final double score;

  protected AbstractScoreElement(String id, double score) {
    this.id = id;
    this.score = score;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public double getScore() {
    return this.score;
  }

  @Override
  public String toString() {
    return this.getId() + "=" + this.getScore();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractScoreElement that = (AbstractScoreElement) o;
    return Double.compare(that.score, score) == 0 &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, score);
  }
}
