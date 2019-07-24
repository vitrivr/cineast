package org.vitrivr.cineast.standalone.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Helper class that can be used to track evaluation results. It tracks the total number of relevant documents
 * per class, the number of retrieved documents and the number of hits at every rank k.
 *
 * @author rgasser
 * @version 1.0
 * @created 06.05.17
 */
public class EvaluationResult {
    /** ID of the reference document that was used to perform the query. */
    private final String docID;

    /** Name of reference object's class labels. */
    private final String cl;

    /** Number of relevant documents in the class according to the ground truth. */
    private final int relevant;

    /** Number of retrieved results. */
    private int retrieved = 0;

    /** Number of retrieved and relevant documents. */
    private int intersection = 0;

    /** List of docID / precision / recall triples for each rank. */
    private List<Triple<String,Float,Float>> pk;

    /**
     * Creates a new EvaluationResult object for the provided reference document
     *
     * @param docID ID of the reference document.
     * @param groundtruth Ground truth object used to construct this evaluation result.
     */
    public EvaluationResult(String docID, Groundtruth groundtruth) throws EvaluationException {
        this.docID = docID;
        this.cl = groundtruth.classForDocId(docID).orElseThrow(() -> new EvaluationException(String.format("The provided document ID '%s' is not registered in the ground truth.", docID)));
        this.relevant = groundtruth.numberOfRelevant(this.cl);
        this.pk = new ArrayList<>();
    }

    /**
     * Registers a new document from the resultset alongside with the information whether it was a hit or not.
     * Updates the retrieval statistics.
     *
     * @param docID ID of the document that was retrieved.
     * @param k The rank of the retrieved document.
     * @param relevant Boolean that indicates whether the document was relevant (true) or not (false).
     */
    public final void documentAvailable(String docID, int k, boolean relevant) {
        if (k < 1) {
          throw new IllegalArgumentException(String.format("The value k must be greater than 0 (is: %d).", k));
        }
        if (k < this.pk.size()) {
          throw new IllegalArgumentException(String.format("The provided rank %d has already been evaluated.", k));
        }
        if (relevant) {
          this.intersection += 1;
        }
        this.retrieved += 1;
        Triple<String,Float,Float> triple = new ImmutableTriple<>(docID, (float)this.intersection/(float)k,  (float)this.intersection/(float)this.relevant);
        this.pk.add(triple);
    }

    /**
     * Getter for query object ID.
     *
     * @return
     */
    public final String getDocId() {
        return this.docID;
    }

    /**
     * Getter for query object class.
     *
     * @return
     */
    public final String getCl() {
        return cl;
    }

    /**
     * Getter for relevant.
     *
     * @return Number of relevant documents as per ground truth.
     */
    public int getRelevant() {
        return relevant;
    }

    /**
     * Getter for retrieved.
     *
     * @return Number of retrieved documents.
     */
    public final int getRetrieved() {
        return retrieved;
    }

    /**
     * Getter for intersection
     *
     * @return Number of retrieved & relevant documents.
     */
    public final int getIntersection() {
        return intersection;
    }

    /**
     * Returns true, if the number of retrieved & relevant documents equals the
     * total number of relevant documents.
     *
     * @return
     */
    public boolean done() {
        return this.intersection == this.relevant;
    }

    /**
     *
     * @return
     */
    public final String toString(String delimiter) {
        StringBuilder builder = new StringBuilder();

        /* Create header. */
        builder.append("ID");
        builder.append(delimiter);
        builder.append("Hit");
        builder.append(delimiter);
        builder.append("Precision");
        builder.append(delimiter);
        builder.append("Recall");
        builder.append("\n");

        int i = 0;
        /* Append data. */
        for (Triple<String,Float,Float> triple : this.pk) {
            builder.append(triple.getLeft());
            builder.append(delimiter);

            /* Check if entry was a documentAvailable. */
            boolean hit = false;
            if (i == 0 && triple.getRight() > 0) {
                hit = true;
            } else if (i > 0 && triple.getRight() > this.pk.get(i-1).getRight()) {
                hit = true;
            }
            builder.append(hit ? 1 : 0);
            builder.append(delimiter);

            builder.append(triple.getMiddle());
            builder.append(delimiter);
            builder.append(triple.getRight());
            builder.append("\n");
            i++;
        }

        return builder.toString();
    }
}
