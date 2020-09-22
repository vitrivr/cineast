package org.vitrivr.cineast.core.db.adampro;

import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.*;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DistanceMessage.DistanceType;
import org.vitrivr.adampro.grpc.AdamGrpc.ExpressionQueryMessage.Operation;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DataMessageConverter;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class ADAMproMessageBuilder {

    /** Default projection message.. */
    public static final ProjectionMessage DEFAULT_PROJECTION_MESSAGE;

    /** Default query hint used in absence of any explicit hint. */
    public static final Collection<ReadableQueryConfig.Hints> DEFAULT_HINT = new ArrayList<ReadableQueryConfig.Hints>(1);

    /** Supported distance messages. */
    private static final DistanceMessage chisquared, correlation, cosine, hamming, jaccard,
            kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, spannorm, haversine;

    static {
        DEFAULT_PROJECTION_MESSAGE = AdamGrpc.ProjectionMessage.newBuilder().setAttributes(AdamGrpc.ProjectionMessage.AttributeNameMessage.newBuilder().addAttribute("ap_distance").addAttribute("id")).build();
        DEFAULT_HINT.add(ReadableQueryConfig.Hints.exact);

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
    private final AdamGrpc.SubExpressionQueryMessage.Builder seqmBuilder = AdamGrpc.SubExpressionQueryMessage.newBuilder();
    private final AdamGrpc.ExpressionQueryMessage.Builder eqmBuilder = AdamGrpc.ExpressionQueryMessage.newBuilder();
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
    public QueryMessage buildQueryMessage(Collection<ReadableQueryConfig.Hints> hints, FromMessage.Builder fb,
                                           BooleanQueryMessage bqMessage, ProjectionMessage pMessage,
                                           NearestNeighbourQueryMessage nnqMessage) {
        synchronized (qmBuilder) {
            qmBuilder.clear();
            qmBuilder.setFrom(fb);
            if (hints != null && !hints.isEmpty()) {
                qmBuilder.addAllHints(hints.stream().map(Enum::name).collect(Collectors.toList()));
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
    public QueryMessage buildQueryMessage(Collection<ReadableQueryConfig.Hints> hints, FromMessage fromMessage, BooleanQueryMessage bqMessage, ProjectionMessage pMessage, NearestNeighbourQueryMessage nnqMessage) {
        synchronized (this.qmBuilder) {
            this.qmBuilder.clear();
            this.qmBuilder.setFrom(fromMessage);
            if (hints != null && !hints.isEmpty()) {
                qmBuilder.addAllHints(hints.stream().map(Enum::name).collect(Collectors.toList()));
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
     * Builds a FromMessage from a SubExpressionQueryMessage. This method can be used to select
     * from a set that has been built up previously
     *
     * @param message SubExpressionQueryMessage message
     * @return FromMessage
     */
    public FromMessage buildFromSubExpressionMessage(SubExpressionQueryMessage message) {
        synchronized (this.fromBuilder) {
            this.fromBuilder.clear();
            this.fromBuilder.setExpression(message);
            return this.fromBuilder.build();
        }
    }

    /**
     * Builds a SubExpressionQueryMessage from a QueryMessage.
     *
     * @param message
     */
    public SubExpressionQueryMessage buildSubExpressionQueryMessage(QueryMessage message) {
        synchronized (this.seqmBuilder) {
            this.seqmBuilder.clear();
            this.seqmBuilder.setQm(message);
            return this.seqmBuilder.build();
        }
    }

    /**
     * Builds a SubExpressionQueryMessage from a ExpressionQueryMessage.
     *
     * @param message
     */
    public SubExpressionQueryMessage buildSubExpressionQueryMessage(ExpressionQueryMessage message) {
        synchronized (this.seqmBuilder) {
            this.seqmBuilder.clear();
            this.seqmBuilder.setEqm(message);
            return this.seqmBuilder.build();
        }
    }

    /**
     * Builds an ExpressionQueryMesssage, that is a QueryMessage that combines the results of two
     * SubExpressionQuerymessages using a Set operation.
     *
     * @param left First SubExpressionQueryMessage to combine.
     * @param right Second SubExpressionQueryMessage to combine.
     * @param operation Set operation.
     * @param options Named options that should be passed to the ExpressionQueryMessage.
     * @return ExpressionQueryMessage
     */
    public AdamGrpc.ExpressionQueryMessage buildExpressionQueryMessage(SubExpressionQueryMessage left, SubExpressionQueryMessage right, Operation operation, Map<String,String> options) {
        synchronized (this.eqmBuilder) {
            this.eqmBuilder.clear();
            this.eqmBuilder.setLeft(left);
            this.eqmBuilder.setRight(right);
            this.eqmBuilder.setOperation(operation);
            this.eqmBuilder.setOrder(ExpressionQueryMessage.OperationOrder.PARALLEL);
            if (options != null && options.size() > 0) {
                this.eqmBuilder.putAllOptions(options);
            }
            return this.eqmBuilder.build();
        }
    }

    /**
     * This method recursively combines a list of SubExpressionQueryMessages into a single SubExpressionQueryMessage by
     * building corresponding ExpressionQueryMessage. Calling this method for a list of SubExpressionQueryMessage creates
     * a new SubExpressionQueryMessage that combines the query results of each SubExpressionQueryMessage under the
     * provided operation.
     *
     * @param expressions List of SubExpressionQueryMessages
     * @param operation Set operation used for combining partial results
     * @return
     */
    public SubExpressionQueryMessage mergeSubexpressions(List<SubExpressionQueryMessage> expressions, Operation operation, Map<String,String> options) {
        /* If list only contains one SubExpressionQueryMessage then return it. */
        if (expressions.size() == 1) {
          return expressions.get(0);
        }

        /* Take first and second message and remove them from the list. */
        SubExpressionQueryMessage m1 = expressions.get(0);
        SubExpressionQueryMessage m2 = expressions.get(1);
        expressions.remove(0);
        expressions.remove(0);

        /* Merge expressions into an ExpressionQueryMessage using the operation and add them to the list. */
        ExpressionQueryMessage eqm = buildExpressionQueryMessage(m1, m2, operation, options);
        SubExpressionQueryMessage sqm = buildSubExpressionQueryMessage(eqm);
        expressions.add(sqm);

        /* Call again. */
        return mergeSubexpressions(expressions, operation, options);
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
     * Builds a {@link WhereMessage} using the specified settings and the equals operator.
     *
     * @param key The name of the field (key).
     * @param value The values that should be compared against the field.
     * @return {@link WhereMessage}
     */
    public WhereMessage buildWhereMessage(String key, String value) {
        return buildWhereMessage(key, Collections.singleton(value));
    }

    /**
     * Builds a {@link WhereMessage} using the specified settings and the equals operator.
     *
     * @param key The name of the field (key).
     * @param values The list of values that should be compared against the field.
     * @return {@link WhereMessage}
     */
    public WhereMessage buildWhereMessage(String key, Iterable<String> values) {
        return this.buildWhereMessage(key, values, RelationalOperator.EQ);
    }

    /**
     * Builds a {@link WhereMessage} using the specified settings.
     *
     * @param key The name of the field (key).
     * @param values The list of values that should be compared against the field.
     * @param operator The {@link RelationalOperator} used to compare the field (key) and the values.
     * @return {@link WhereMessage}
     */
    public WhereMessage buildWhereMessage(String key, Iterable<String> values, RelationalOperator operator) {
        synchronized (this.wmBuilder) {
            this.wmBuilder.clear();
            final DataMessage.Builder damBuilder = DataMessage.newBuilder();
            final Stream<String> valueStream = StreamSupport.stream(values.spliterator(), false);
            switch (operator) {
                case IN:
                case EQ:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("=");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData(v).build()).collect(Collectors.toList()));
                    break;
                case NEQ:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("!=");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData(v).build()).collect(Collectors.toList()));
                    break;
                case GEQ:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp(">=");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData(v).build()).collect(Collectors.toList()));
                    break;
                case LEQ:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("<=");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData(v).build()).collect(Collectors.toList()));
                    break;
                case GREATER:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("<");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData(v).build()).collect(Collectors.toList()));
                    break;
                case LESS:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp(">");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData(v).build()).collect(Collectors.toList()));
                    break;
                case ILIKE:
                    this.wmBuilder.setAttribute("lower(" + key + ")");
                    this.wmBuilder.setOp("LIKE");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData("%" + v.toLowerCase() + "%").build()).collect(Collectors.toList()));
                    break;
                case LIKE:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("LIKE");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData("%" + v + "%").build()).collect(Collectors.toList()));
                    break;
                case NLIKE:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("NOT LIKE");
                    this.wmBuilder.addAllValues(valueStream.map(v -> damBuilder.setStringData("%" + v + "%").build()).collect(Collectors.toList()));
                    break;
                case RLIKE:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("RLIKE");
                    break;
                default:
                    this.wmBuilder.setAttribute(key);
                    this.wmBuilder.setOp("=");
                    break;
            }
            return this.wmBuilder.build();
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
                weights.ifPresent(floats -> nnqmBuilder
                    .setWeights(DataMessageConverter.convertVectorMessage(floats)));
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

                if (Math.abs(norm - 1f) < 1e-6f) {
                    return manhattan;
                }

                if (Math.abs(norm - 2f) < 1e-6f) {
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

    public BooleanQueryMessage inList(String attribute, Collection<String> elements) {

        if (elements == null || elements.isEmpty()) {
            return null;
        }

        final DataMessage.Builder damBuilder = DataMessage.newBuilder();

        return BooleanQueryMessage.newBuilder()
                .addWhere(
                        WhereMessage.newBuilder()
                                .setOp("IN")
                                .setAttribute(attribute)
                                .addAllValues(
                                        elements.stream().map(x -> damBuilder.clear().setStringData(x).build()).collect(Collectors.toList())
                                )
                                .build()
                ).build();

    }
}
