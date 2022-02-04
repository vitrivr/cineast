package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import georegression.struct.point.Point2D_F32;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang3.NotImplementedException;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatArrayIterable;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.Skeleton;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.HungarianAlgorithm;
import org.vitrivr.cottontail.client.SimpleClient;
import org.vitrivr.cottontail.client.iterators.Tuple;
import org.vitrivr.cottontail.client.iterators.TupleIterator;
import org.vitrivr.cottontail.grpc.CottontailGrpc;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Expression;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Function;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionElement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Expression;
import org.vitrivr.cottontail.grpc.CottontailGrpc.FloatVector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.From;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Function;
import org.vitrivr.cottontail.grpc.CottontailGrpc.FunctionName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Order;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Order.Component;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Order.Direction;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionElement;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Query;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Scan;
import org.vitrivr.cottontail.grpc.CottontailGrpc.SchemaName;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

public class SkeletonPose extends AbstractFeatureModule {

    private static final String PERSON_ID_COL = "person";
    private static final String FEATURE_COL = "skeleton";
    private static final String WEIGHT_COL = "weights";

    public SkeletonPose() {
        super("feature_skeletonpose", 1, 8);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        super.init(phandlerSupply, batchSize);
        this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, PERSON_ID_COL, FEATURE_COL, WEIGHT_COL);
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(this.tableName, false,
                new AttributeDefinition(PERSON_ID_COL, AttributeDefinition.AttributeType.INT),
                new AttributeDefinition(FEATURE_COL, AttributeDefinition.AttributeType.VECTOR, this.vectorLength),
                new AttributeDefinition(WEIGHT_COL, AttributeDefinition.AttributeType.VECTOR, this.vectorLength)
        );
    }

    protected void persist(Collection<Pair<String,Skeleton>> skeletons) {
        if (skeletons == null || skeletons.isEmpty()) {
            return;
        }
        final List<PersistentTuple> tuples = new ArrayList<>(skeletons.size());
        int i = 0;
        for (Pair<String,Skeleton> skeleton : skeletons) {
            final Pair<float[], float[]> pair = getAnglesandWeights(skeleton.second);
            tuples.add(this.phandler.generateTuple(skeleton.first, i++, pair.first, pair.second));
        }
        this.phandler.persist(tuples);
    }

    private List<Skeleton> detectSkeletons(MultiImage img) {
        throw new NotImplementedException("not currently available");
    }

    @Override
    public void processSegment(SegmentContainer segmentContainer) {

        VideoFrame representativeFrame = segmentContainer.getMostRepresentativeFrame();

        if (representativeFrame == VideoFrame.EMPTY_VIDEO_FRAME) {
            return;
        }

        this.persist(detectSkeletons(representativeFrame.getImage()).stream().map(it -> new Pair<>(segmentContainer.getId(), it)).collect(Collectors.toList()));
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

        List<Skeleton> skeletons = sc.getSkeletons();

        if (skeletons == null || skeletons.isEmpty()) {
            return Collections.emptyList();
        }

        if (!(this.selector instanceof CottontailSelector)) {
            return Collections.emptyList();
        }

        SimpleClient client = ((CottontailSelector) this.selector).cottontail.client;

        HashMap<String, TObjectDoubleHashMap<Pair<Integer, Integer>>> segmentDistancesMap = new HashMap<>(qc.getRawResultsPerModule() * skeletons.size());

        int queryPersonId = 0;

        //query all skeletons
        for (Skeleton skeleton : skeletons) {

            Pair<float[], float[]> pair = getAnglesandWeights(skeleton);

//            List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getNearestNeighbourRows(qc.getRawResultsPerModule(), pair.first, FEATURE_COL, QueryConfig.clone(qc)
//                    .setDistanceWeights(pair.second)
//                    .setDistance(ReadableQueryConfig.Distance.manhattan));


            TupleIterator tuples = client.query(buildQuery(pair.first, pair.second, qc.getRawResultsPerModule()));


            int i = 0;
            while (tuples.hasNext()) {
                i++;
                Tuple tuple = tuples.next();

                String segment = tuple.asString(GENERIC_ID_COLUMN_QUALIFIER);

                if (!segmentDistancesMap.containsKey(segment)) {
                    segmentDistancesMap.put(segment, new TObjectDoubleHashMap<>());
                }

                segmentDistancesMap.get(segment).put(new Pair<>(queryPersonId, tuple.asInt(PERSON_ID_COL)), tuple.asFloat(DB_DISTANCE_VALUE_QUALIFIER));

            }

            ++queryPersonId;

        }

        ArrayList<ScoreElement> results = new ArrayList<>(segmentDistancesMap.size());

        //compute assignment
        if (queryPersonId == 1) { //only one query skeleton
            for (String segment : segmentDistancesMap.keySet()) {
                TObjectDoubleHashMap<Pair<Integer, Integer>> distances = segmentDistancesMap.get(segment);
                double minDist = Arrays.stream(distances.values()).min().orElse(Double.MAX_VALUE);
                results.add(new SegmentScoreElement(segment, this.correspondence.applyAsDouble(minDist)));
            }
            results.sort(SegmentScoreElement.SCORE_COMPARATOR);
            return results.subList(0, Math.min(results.size(), qc.getRawResultsPerModule()) - 1);
        }


        //more than query skeleton
        for (String segment : segmentDistancesMap.keySet()) {

            TObjectDoubleHashMap<Pair<Integer, Integer>> distances = segmentDistancesMap.get(segment);

            if (distances.isEmpty()) {
                continue; //should never happen
            }

            Set<Integer> personIds = distances.keySet().stream().map(p -> p.second).collect(Collectors.toSet());

            if (personIds.size() == 1) { //only one retrieved skeleton
                double minDist = Arrays.stream(distances.values()).min().orElse(Double.MAX_VALUE);
                results.add(new SegmentScoreElement(segment, this.correspondence.applyAsDouble(minDist) / skeletons.size()));
                continue;
            }

            //more than one retrieved skeletons

            double[][] costs = new double[skeletons.size()][personIds.size()];
            TIntIntHashMap inversePersonIdMapping = new TIntIntHashMap(personIds.size());
            int i = 0;
            for (int personId : personIds) {
                inversePersonIdMapping.put(personId, i++);
            }

            for (Pair<Integer, Integer> p : distances.keySet()) {
                costs[p.first][inversePersonIdMapping.get(p.second)] = -distances.get(p);
            }

            HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costs);
            int[] assignment = hungarianAlgorithm.execute();

            double scoreSum = 0;

            for (i = 0; i < Math.min(personIds.size(), skeletons.size()); ++i) {
                scoreSum += this.correspondence.applyAsDouble(-costs[i][assignment[i]]);
            }

            results.add(new SegmentScoreElement(segment, scoreSum / skeletons.size()));

        }

        return results;
    }

    private Pair<float[], float[]> getAnglesandWeights(Skeleton skeleton) {
        float[] angles = new float[]{
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_ANKLE), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_KNEE), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_HIP)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_KNEE), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_HIP), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_HIP)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_HIP), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_HIP), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_KNEE)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_HIP), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_KNEE), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_ANKLE)),

                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_WRIST), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_ELBOW), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_SHOULDER)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_ELBOW), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_SHOULDER), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_SHOULDER)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_SHOULDER), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_SHOULDER), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_ELBOW)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_SHOULDER), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_ELBOW), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_WRIST))
        };

        float[] weights = new float[]{
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_ANKLE), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_KNEE), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_HIP)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_KNEE), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_HIP), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_HIP)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_HIP), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_HIP), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_KNEE)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_HIP), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_KNEE), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_ANKLE)),

                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_WRIST), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_ELBOW), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_SHOULDER)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_ELBOW), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_SHOULDER), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_SHOULDER)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_SHOULDER), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_SHOULDER), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_ELBOW)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_SHOULDER), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_ELBOW), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_WRIST)),
        };

        return new Pair<>(angles, weights);

    }

    private CottontailGrpc.QueryMessage buildQuery(float[] query, float[] weights, int limit) {

        float queryWeightSum = 0f;

        for (float w : weights) {
            queryWeightSum += w;
        }
        // FIXME: Make it work
        // TODO: Cleanup and document
        Expression vectorDifference = Expression.newBuilder().setFunction(/* Nested, min() function */
                CottontailGrpc.Function.newBuilder().setName(CottontailGrpc.FunctionName.newBuilder().setName("vmin")).addArguments(
                        Expression.newBuilder().setColumn(CottontailGrpc.ColumnName.newBuilder().setName(WEIGHT_COL))
                ).addArguments(
                        Expression.newBuilder().setLiteral(CottontailGrpc.Literal.newBuilder().setVectorData(CottontailGrpc.Vector.newBuilder().setFloatVector(
                                CottontailGrpc.FloatVector.newBuilder().addAllVector(new FloatArrayIterable(weights))
                        )))
                )
        ).build();

        Expression correctionTerm = Expression.newBuilder().setFunction(
                Function.newBuilder().setName(CottontailGrpc.FunctionName.newBuilder().setName("mul")
                ).addArguments( //constant
                        Expression.newBuilder().setLiteral(CottontailGrpc.Literal.newBuilder().setFloatData((float) Math.PI))
                ).addArguments( //sub-expression
                        Expression.newBuilder().setFunction(
                                Function.newBuilder().setName(CottontailGrpc.FunctionName.newBuilder().setName("sub")
                                ).addArguments(
                                        Expression.newBuilder().setLiteral(CottontailGrpc.Literal.newBuilder().setFloatData(queryWeightSum))
                                ).addArguments(
                                        Expression.newBuilder().setFunction(CottontailGrpc.Function.newBuilder().setName(CottontailGrpc.FunctionName.newBuilder().setName("vsum")).addArguments(vectorDifference))
                                )
                        )
                )

        ).build();


        ProjectionElement distanceFunction = ProjectionElement.newBuilder().setFunction(/* Distance function */

                Function.newBuilder().setName(FunctionName.newBuilder().setName("add")).addArguments(
                        Expression.newBuilder().setFunction(Function.newBuilder().setName(FunctionName.newBuilder().setName("manhattanw")
                        ).addArguments(
                                Expression.newBuilder().setColumn(ColumnName.newBuilder().setName(FEATURE_COL))
                        ).addArguments(
                                Expression.newBuilder().setLiteral(Literal.newBuilder().setVectorData(CottontailGrpc.Vector.newBuilder().setFloatVector(
                                        FloatVector.newBuilder().addAllVector(new FloatArrayIterable(query))
                                )))
                        ).addArguments(
                                vectorDifference
                        ))
                ).addArguments(
                        correctionTerm
                )
        ).setAlias(ColumnName.newBuilder().setName("distance").build()).build();


        return QueryMessage.newBuilder().setQuery(
                Query.newBuilder().setFrom(
                        From.newBuilder().setScan(Scan.newBuilder().setEntity(EntityName.newBuilder()
                                .setName(this.tableName).setSchema(SchemaName.newBuilder().setName("cineast"))))
                ).setProjection(
                        Projection.newBuilder()
                                .addElements(ProjectionElement.newBuilder().setColumn(ColumnName.newBuilder().setName(GENERIC_ID_COLUMN_QUALIFIER)))
                                .addElements(ProjectionElement.newBuilder().setColumn(ColumnName.newBuilder().setName(PERSON_ID_COL)))
                                .addElements(distanceFunction)
                ).setOrder(
                    Order.newBuilder()
                        .addComponents(Component.newBuilder().setColumn(ColumnName.newBuilder().setName(DB_DISTANCE_VALUE_QUALIFIER))
                        .setDirection(Direction.ASCENDING).build()).build()
                ).setLimit(limit).build()).build();
    }

    private static float angle(Point2D_F32 p1, Point2D_F32 c, Point2D_F32 p2) {
        return (float) (Math.atan2(p2.y - c.y, p2.x - c.x) - Math.atan2(p1.y - c.y, p1.x - c.x));
    }

    private static float min(float f, float g, float h) {
        return Math.min(f, Math.min(g, h));
    }

    // TODO or FIXME: Remove
    public static void main(String[] args) throws IOException {

        File baseDir = new File("/Users/rgasser/Downloads/VBS2022/");
        File[] folders = baseDir.listFiles(File::isDirectory);

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<SkeletonEntry>> typeRef = new TypeReference<>() {
        };

        DatabaseConfig config = new DatabaseConfig();
        config.setHost("localhost");
        config.setPort(1865);

        CottontailWrapper ctWrapper = new CottontailWrapper(config, true);


        SkeletonPose sp = new SkeletonPose();

        int batchSize = 10000;
        boolean insert = false;
        if (insert) {
            sp.initalizePersistentLayer(() -> new CottontailEntityCreator(ctWrapper));
            sp.init(() -> new CottontailWriter(ctWrapper), 100);
            final List<Pair<String,Skeleton>> skeletons = new LinkedList<>();
            for (File folder : folders) {
                for (File file : folder.listFiles(f -> f.getName().endsWith(".json"))) {
                    final String segmentId = "v_" + file.getName().replaceAll("shot", "").replaceAll("_RKF.json", "");
                    for (SkeletonEntry e : mapper.readValue(file, typeRef)) {
                        skeletons.add(new Pair<>(segmentId, e.toSkeleton()));
                    }
                }
                if (skeletons.size() >= batchSize) {
                    sp.persist(skeletons);
                    System.out.println("Persisted " + skeletons.size() + " entries...");
                    skeletons.clear();
                }
            }

            /* Final persist. */
            if (skeletons.size() > 0) {
                sp.persist(skeletons);
                System.out.println("Persisted " + skeletons.size() + " entries...");
            }
        }


        sp.init(() -> new CottontailSelector(ctWrapper));

        Skeleton skeleton = mapper.readValue(new File(baseDir, "00006/shot00006_22_RKF.json"), typeRef).get(0).toSkeleton();

        SegmentContainer container = new SegmentContainer() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getSuperId() {
                return null;
            }

            @Override
            public void setId(String id) {

            }

            @Override
            public void setSuperId(String id) {

            }

            @Override
            public List<Skeleton> getSkeletons() {
                return Collections.singletonList(skeleton);
            }
        };

        List<ScoreElement> results = sp.getSimilar(container, new QueryConfig(null).setResultsPerModule(100));

        int i = 0;

        for (ScoreElement element : results) {
            System.out.println(i++ + ": " + element);
        }

    }

    static class SkeletonEntry {
        public int person_id;
        public List<List<Float>> pose_keypoints;

        public Skeleton toSkeleton() {
            float[] weights = new float[17];
            float[] coordinates = new float[34];

            for (int i = 0; i < 17; ++i) {
                coordinates[2 * i] = pose_keypoints.get(i).get(0);
                coordinates[2 * i + 1] = pose_keypoints.get(i).get(1);
                weights[i] = pose_keypoints.get(i).get(2);
            }

            return new Skeleton(coordinates, weights);

        }

    }

}
