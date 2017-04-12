package org.vitrivr.cineast.core.data.score;

/**
 * Instances of this class denote a specific score for a particular document, i.e., a document id
 * paired together with a score. Note that implementations should stay immutable.
 */
public interface ScoreElement {
  String getId();

  double getScore();

  // Note: withScore returning the same type is not enforced through generics for sake of brevity and readability
  /**
   * @param newScore Score value for the duplicated {@link ScoreElement}
   * @return a new instance of the same type of {@link ScoreElement} based of {@code this} with the
   * given {@code newScore}.
   */
  ScoreElement withScore(double newScore);
}
