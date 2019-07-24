package org.vitrivr.cineast.standalone.evaluation;

/**
 * An exception that indicates something went wrong during an Evaluation-Run.
 *
 * @author rgasser
 * @version 1.0
 * @created 06.05.17
 */
public class EvaluationException extends Exception {

    /**
     * Constructor for EvaluationException
     *
     * @param message Error message.
     */
    EvaluationException(String message) {
        super(message);
    }
}
