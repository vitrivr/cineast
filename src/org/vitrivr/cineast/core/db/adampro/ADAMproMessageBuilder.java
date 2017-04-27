package org.vitrivr.cineast.core.db.adampro;

import org.vitrivr.adampro.grpc.AdamGrpc;

import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DistanceMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DistanceMessage.DistanceType;
import org.vitrivr.adampro.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.NearestNeighbourQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ProjectionMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.VectorMessage;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.db.DataMessageConverter;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class ADAMproMessageBuilder {

    /** Default projection message.. */
    public static final ProjectionMessage DEFAULT_PROJECTION_MESSAGE;

    /** Default query hint used in absence of any explicit hint. */
    public static final Collection<String> DEFAULT_HINT = new ArrayList<String>(1);

    /** Supported distance messages. */
    private static final DistanceMessage chisquared, correlation, cosine, hamming, jaccard,
            kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, spannorm, haversine;

    static {
        DEFAULT_PROJECTION_MESSAGE = AdamGrpc.ProjectionMessage.newBuilder()
                .setAttributes(
                        AdamGrpc.ProjectionMessage.AttributeNameMessage.newBuilder().addAttribute("ap_distance").addAttribute("id")
                ).build();

        DEFAULT_HINT.add(ReadableQueryConfig.Hints.exact.name());

        DistanceMessage.Builder dmBuilder = DistanceMessage.newBuilder();

        chisquared = dmBuilder.clear().setDistancetype(DistanceType.chisquared).build();
        correlation = dmBuilder.clear().setDistancetype(DistanceType.correlation).build();
        cosine = dmBuilder.clear().setDistancetype(DistanceType.cosine).build();
        hamming = dmBuilder.clear().setDistancetype(DistanceType.hamming).build();
        jaccard = dmBuilder.clear().setDistancetype(DistanceType.jaccard).build();
        kullbackleibler = dmBuilder.clear().setDistancetype(DistanceType.kullbackleibler).build();
        chebyshev = dmBuilder.clear().setDistancetype(DistanceType.chebyshev).build();
        euclidean = dmBuilder.clear().setDistancetype(DistanceType.euclidean).build();
        squaredeuclidean = dmBuilder.clear().setDistancetype(DistanceType.squaredeuclidean).build();
        manhattan = dmBuilder.clear().setDistancetype(DistanceType.manhattan).build();
        spannorm = dmBuilder.clear().setDistancetype(DistanceType.spannorm).build();
        haversine = dmBuilder.clear().setDistancetype(DistanceType.haversine).build();
    }


    private final AdamGrpc.FromMessage.Builder fromBuilder = AdamGrpc.FromMessage.newBuilder();
    private final AdamGrpc.QueryMessage.Builder qmBuilder = AdamGrpc.QueryMessage.newBuilder();
    private final AdamGrpc.BatchedQueryMessage.Builder baqmBuilder = AdamGrpc.BatchedQueryMessage.newBuilder();
    private final AdamGrpc.NearestNeighbourQueryMessage.Builder nnqmBuilder = AdamGrpc.NearestNeighbourQueryMessage.newBuilder();
    private final AdamGrpc.BooleanQueryMessage.Builder bqmBuilder = AdamGrpc.BooleanQueryMessage.newBuilder();
    private final AdamGrpc.BooleanQueryMessage.WhereMessage.Builder wmBuilder = AdamGrpc.BooleanQueryMessage.WhereMessage.newBuilder();
    private final AdamGrpc.DistanceMessage.Builder dmBuilder = AdamGrpc.DistanceMessage.newBuilder();

    /**
     * Constructs and returns a BatchedQueryMessage from the provided query-messages.
     *
     * @param queries
     * @return
     */
    public BatchedQueryMessage buildBatchedQueryMessage(List<QueryMessage> queries) {
        synchronized (bqmBuilder) {
            baqmBuilder.clear();
            baqmBuilder.addAllQueries(queries);
            return baqmBuilder.build();
        }
    }

    /**
     *
     * @param hints
     * @param fb
     * @param bqMessage
     * @param pMessage
     * @param nnqMessage
     * @return
     */
    public QueryMessage buildQueryMessage(Collection<String> hints, FromMessage.Builder fb,
                                           BooleanQueryMessage bqMessage, ProjectionMessage pMessage,
                                           NearestNeighbourQueryMessage nnqMessage) {
        synchronized (qmBuilder) {
            qmBuilder.clear();
            qmBuilder.setFrom(fb);
            if (hints != null && !hints.isEmpty()) {
                qmBuilder.addAllHints(hints);
            }
            if (bqMessage != null) {
                qmBuilder.setBq(bqMessage);
            }
            if (pMessage != null) {
                qmBuilder.setProjection(pMessage);
            }
            if (nnqMessage != null) {
                qmBuilder.setNnq(nnqMessage);
            }
            return qmBuilder.build();
        }
    }

    /**
     *
     * @param hints
     * @param bqMessage
     * @param pMessage
     * @param nnqMessage
     * @return
     */
    public QueryMessage buildQueryMessage(Collection<String> hints, FromMessage fromMessage, BooleanQueryMessage bqMessage, ProjectionMessage pMessage, NearestNeighbourQueryMessage nnqMessage) {
        synchronized (this.qmBuilder) {
            this.qmBuilder.clear();
            this.qmBuilder.setFrom(fromMessage);
            if (hints != null && !hints.isEmpty()) {
                qmBuilder.addAllHints(hints);
            }
            if (bqMessage != null) {
                this.qmBuilder.setBq(bqMessage);
            }
            if (pMessage != null) {
                this.qmBuilder.setProjection(pMessage);
            }
            if (nnqMessage != null) {
                this.qmBuilder.setNnq(nnqMessage);
            }
            return qmBuilder.build();
        }
    }

    /**
     *
     * @param entity
     * @return
     */
    public FromMessage buildFromMessage(String entity) {
        synchronized (this.fromBuilder) {
            this.fromBuilder.clear();
            this.fromBuilder.setEntity(entity);
            return this.fromBuilder.build();
        }
    }

    /**
     *
     * @param where
     * @param whereMessages
     * @return
     */
    public BooleanQueryMessage buildBooleanQueryMessage(WhereMessage where,
                                                         WhereMessage... whereMessages) {
        ArrayList<WhereMessage> tmp = new ArrayList<>(
                1 + (whereMessages == null ? 0 : whereMessages.length));
        tmp.add(where);
        if (whereMessages != null) {
            Collections.addAll(tmp, whereMessages);
        }
        synchronized (bqmBuilder) {
            bqmBuilder.clear();
            return bqmBuilder.addAllWhere(tmp).build();
        }
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public WhereMessage buildWhereMessage(String key, String value) {
        return buildWhereMessage(key, Collections.singleton(value));
    }

    /**
     *
     * @param key
     * @param values
     * @return
     */
    public WhereMessage buildWhereMessage(String key, Iterable<String> values) {
        synchronized (wmBuilder) {
            wmBuilder.clear();
            DataMessage.Builder damBuilder = DataMessage.newBuilder();

            wmBuilder.setAttribute(key);

            for (String value : values) {
                wmBuilder.addValues(damBuilder.setStringData(value).build());
            }

            return wmBuilder.build();
        }
    }

    /**
     *
     * @param column
     * @param fvm
     * @param k
     * @param qc
     * @return
     */
    public NearestNeighbourQueryMessage buildNearestNeighbourQueryMessage(String column, VectorMessage fvm, int k, ReadableQueryConfig qc) {
        synchronized (nnqmBuilder) {
            this.nnqmBuilder.clear();
            this.nnqmBuilder.setAttribute(column).setQuery(fvm).setK(k);
            this.nnqmBuilder.setDistance(buildDistanceMessage(qc));
            if (qc != null) {
                Optional<float[]> weights = qc.getDistanceWeights();
                if (weights.isPresent()) {
                    nnqmBuilder.setWeights(DataMessageConverter.convertVectorMessage(weights.get()));
                }
            }
            return nnqmBuilder.build();
        }
    }

    /**
     *
     * @param qc
     * @return
     */
    public DistanceMessage buildDistanceMessage(ReadableQueryConfig qc) {
        if (qc == null) {
            return manhattan;
        }
        Optional<ReadableQueryConfig.Distance> distance = qc.getDistance();
        if (!distance.isPresent()) {
            return manhattan;
        }
        switch (distance.get()) {
            case chebyshev:
                return chebyshev;
            case chisquared:
                return chisquared;
            case correlation:
                return correlation;
            case cosine:
                return cosine;
            case euclidean:
                return euclidean;
            case hamming:
                return hamming;
            case jaccard:
                return jaccard;
            case kullbackleibler:
                return kullbackleibler;
            case manhattan:
                return manhattan;
            case minkowski: {

                float norm = qc.getNorm().orElse(1f);

                if (Math.abs(norm - 1f) < 1e6f) {
                    return manhattan;
                }

                if (Math.abs(norm - 2f) < 1e6f) {
                    return euclidean;
                }

                HashMap<String, String> tmp = new HashMap<>();
                tmp.put("norm", Float.toString(norm));

                synchronized (dmBuilder) {
                    return dmBuilder.clear().setDistancetype(DistanceType.minkowski).putAllOptions(tmp)
                            .build();
                }

            }
            case spannorm:
                return spannorm;
            case squaredeuclidean:
                return squaredeuclidean;
            case haversine:
                return haversine;
            default:
                return manhattan;
        }
    }
}
