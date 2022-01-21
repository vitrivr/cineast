package org.vitrivr.cineast.standalone.evaluation;

/**
 * An exception that indicates something went wrong during an Evaluation-Run.
 */
public class EvaluationException extends Exception {

  static final long serialVersionUID = 1L;

  /**
   * Constructor for EvaluationException
   *
   * @param message Error message.
   */
  EvaluationException(String message) {
    super(message);
  }
}
