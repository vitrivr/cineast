package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import georegression.struct.point.Point2D_F32;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
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
import org.vitrivr.cineast.core.util.pose.MergingPoseDetector;
import org.vitrivr.cineast.core.util.pose.OpenPoseDetector;
import org.vitrivr.cineast.core.util.pose.PoseDetector;
import org.vitrivr.cottontail.client.SimpleClient;
import org.vitrivr.cottontail.client.iterators.Tuple;
import org.vitrivr.cottontail.client.iterators.TupleIterator;
import org.vitrivr.cottontail.grpc.CottontailGrpc;
import org.vitrivr.cottontail.grpc.CottontailGrpc.*;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Order.Component;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Order.Direction;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionElement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

public class SkeletonPose extends AbstractFeatureModule {

    private static final String PERSON_ID_COL = "person";
    private static final String FEATURE_COL = "skeleton";
    private static final String WEIGHT_COL = "weights";
    private PoseDetector detector = new MergingPoseDetector();

    public SkeletonPose() {
        super("features_skeletonpose", (float) (16 * Math.PI), 12);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);
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

    private synchronized List<Skeleton> detectSkeletons(MultiImage img) {
        return detector.detectPoses(img.getBufferedImage());
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

        SimpleClient client = ((CottontailSelector) this.selector).getWrapper().client;

        HashMap<String, TObjectDoubleHashMap<Pair<Integer, Integer>>> segmentDistancesMap = new HashMap<>(qc.getRawResultsPerModule() * skeletons.size());

        int queryPersonId = 0;

        //query all skeletons
        for (Skeleton skeleton : skeletons) {

            Pair<float[], float[]> pair = getAnglesandWeights(skeleton);
            TupleIterator tuples = client.query(buildQuery(pair.first, pair.second, qc.getRawResultsPerModule()));

            while (tuples.hasNext()) {
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
            results.sort(SegmentScoreElement.SCORE_COMPARATOR.reversed());
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
                angle(skeleton, Skeleton.SkeletonPointName.LEFT_ANKLE, Skeleton.SkeletonPointName.LEFT_KNEE, Skeleton.SkeletonPointName.LEFT_HIP),
                angle(skeleton, Skeleton.SkeletonPointName.LEFT_KNEE, Skeleton.SkeletonPointName.LEFT_HIP, Skeleton.SkeletonPointName.RIGHT_HIP),
                angle(skeleton, Skeleton.SkeletonPointName.LEFT_HIP, Skeleton.SkeletonPointName.RIGHT_HIP, Skeleton.SkeletonPointName.RIGHT_KNEE),
                angle(skeleton, Skeleton.SkeletonPointName.RIGHT_HIP, Skeleton.SkeletonPointName.RIGHT_KNEE, Skeleton.SkeletonPointName.RIGHT_ANKLE),

                angle(skeleton, Skeleton.SkeletonPointName.LEFT_WRIST, Skeleton.SkeletonPointName.LEFT_ELBOW, Skeleton.SkeletonPointName.LEFT_SHOULDER),
                angle(skeleton, Skeleton.SkeletonPointName.LEFT_ELBOW, Skeleton.SkeletonPointName.LEFT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_SHOULDER),
                angle(skeleton, Skeleton.SkeletonPointName.LEFT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_ELBOW),
                angle(skeleton, Skeleton.SkeletonPointName.RIGHT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_ELBOW, Skeleton.SkeletonPointName.RIGHT_WRIST),

                angle(skeleton, Skeleton.SkeletonPointName.LEFT_KNEE, Skeleton.SkeletonPointName.LEFT_HIP, Skeleton.SkeletonPointName.LEFT_SHOULDER),
                angle(skeleton, Skeleton.SkeletonPointName.RIGHT_KNEE, Skeleton.SkeletonPointName.RIGHT_HIP, Skeleton.SkeletonPointName.RIGHT_SHOULDER),

                angle(skeleton, Skeleton.SkeletonPointName.LEFT_SHOULDER, Skeleton.SkeletonPointName.NOSE, Skeleton.SkeletonPointName.LEFT_EAR),
                angle(skeleton, Skeleton.SkeletonPointName.RIGHT_SHOULDER, Skeleton.SkeletonPointName.NOSE, Skeleton.SkeletonPointName.RIGHT_EAR)
        };

        float[] weights = new float[]{
                min(skeleton, Skeleton.SkeletonPointName.LEFT_ANKLE, Skeleton.SkeletonPointName.LEFT_KNEE, Skeleton.SkeletonPointName.LEFT_HIP),
                min(skeleton, Skeleton.SkeletonPointName.LEFT_KNEE, Skeleton.SkeletonPointName.LEFT_HIP, Skeleton.SkeletonPointName.RIGHT_HIP),
                min(skeleton, Skeleton.SkeletonPointName.LEFT_HIP, Skeleton.SkeletonPointName.RIGHT_HIP, Skeleton.SkeletonPointName.RIGHT_KNEE),
                min(skeleton, Skeleton.SkeletonPointName.RIGHT_HIP, Skeleton.SkeletonPointName.RIGHT_KNEE, Skeleton.SkeletonPointName.RIGHT_ANKLE),

                min(skeleton, Skeleton.SkeletonPointName.LEFT_WRIST, Skeleton.SkeletonPointName.LEFT_ELBOW, Skeleton.SkeletonPointName.LEFT_SHOULDER),
                min(skeleton, Skeleton.SkeletonPointName.LEFT_ELBOW, Skeleton.SkeletonPointName.LEFT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_SHOULDER),
                min(skeleton, Skeleton.SkeletonPointName.LEFT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_ELBOW),
                min(skeleton, Skeleton.SkeletonPointName.RIGHT_SHOULDER, Skeleton.SkeletonPointName.RIGHT_ELBOW, Skeleton.SkeletonPointName.RIGHT_WRIST),

                min(skeleton, Skeleton.SkeletonPointName.LEFT_KNEE, Skeleton.SkeletonPointName.LEFT_HIP, Skeleton.SkeletonPointName.LEFT_SHOULDER),
                min(skeleton, Skeleton.SkeletonPointName.RIGHT_KNEE, Skeleton.SkeletonPointName.RIGHT_HIP, Skeleton.SkeletonPointName.RIGHT_SHOULDER),

                min(skeleton, Skeleton.SkeletonPointName.LEFT_SHOULDER, Skeleton.SkeletonPointName.NOSE, Skeleton.SkeletonPointName.LEFT_EAR),
                min(skeleton, Skeleton.SkeletonPointName.RIGHT_SHOULDER, Skeleton.SkeletonPointName.NOSE, Skeleton.SkeletonPointName.RIGHT_EAR)
        };

        for (int i = 0; i < weights.length; ++i) {
            weights[i] = weights[i] >= 0.5f ? 1f : 0f;
        }

        return new Pair<>(angles, weights);

    }

    private static Expression expression(float f) {
        return Expression.newBuilder().setLiteral(CottontailGrpc.Literal.newBuilder().setFloatData(f)).build();
    }

    private static Expression expression(float[] f) {
        return Expression.newBuilder().setLiteral(CottontailGrpc.Literal.newBuilder().setVectorData(CottontailGrpc.Vector.newBuilder().setFloatVector(
                CottontailGrpc.FloatVector.newBuilder().addAllVector(new FloatArrayIterable(f))
        ))).build();
    }

    private static Function.Builder functionBuilder(String name) {
        return CottontailGrpc.Function.newBuilder().setName(CottontailGrpc.FunctionName.newBuilder().setName(name));
    }

    private static ColumnName columnName(String name) {
        return ColumnName.newBuilder().setName(name).build();
    }

    private static ProjectionElement projectionElement(String name) {
        return ProjectionElement.newBuilder().setExpression(Expression.newBuilder().setColumn(columnName(name)).build()).build();
    }

    private CottontailGrpc.QueryMessage buildQuery(float[] query, float[] weights, int limit) {

        float queryWeightSum = 0f;

        for (float w : weights) {
            queryWeightSum += w;
        }

        // element-wise difference between stored and provided weights
        // counts how many elements are set in the query but are not set in the feature
        Expression vectorDifference = Expression.newBuilder().setFunction(/* Nested, min() function */
                functionBuilder("vmin").addArguments(
                        Expression.newBuilder().setColumn(columnName(WEIGHT_COL))
                ).addArguments(
                       expression(weights)
                )
        ).build();

        //assigns maximum distance for each element specified in the query but not present in the feature
        Expression correctionTerm = Expression.newBuilder().setFunction(
                functionBuilder("mul")
                .addArguments( //constant
                        expression((float) Math.PI)
                ).addArguments( //sub-expression
                        Expression.newBuilder().setFunction(
                                Function.newBuilder().setName(CottontailGrpc.FunctionName.newBuilder().setName("sub")
                                ).addArguments(
                                        expression(queryWeightSum)
                                ).addArguments(
                                        Expression.newBuilder().setFunction(
                                        functionBuilder("vsum")
                                                .addArguments(vectorDifference)
                                        )
                                )
                        )
                )

        ).build();

        //weighted Manhattan-distance plus correction term for missing elements
        ProjectionElement distanceFunction = ProjectionElement.newBuilder().setExpression(Expression.newBuilder().setFunction(/* Distance function */
                functionBuilder("add").addArguments(
                    Expression.newBuilder().setFunction(functionBuilder("manhattanw")
                    .addArguments(
                            Expression.newBuilder().setColumn(columnName(FEATURE_COL))
                    ).addArguments(
                            expression(query)
                    ).addArguments(
                            vectorDifference
                    ))
                ).addArguments(
                        correctionTerm
                )
        )).setAlias(columnName("distance")).build();


        return QueryMessage.newBuilder().setQuery(
                Query.newBuilder().setFrom(
                        From.newBuilder().setScan(Scan.newBuilder().setEntity(EntityName.newBuilder()
                                .setName(this.tableName).setSchema(SchemaName.newBuilder().setName("cineast"))))
                ).setProjection(
                        Projection.newBuilder()
                                .addElements(projectionElement(GENERIC_ID_COLUMN_QUALIFIER))
                                .addElements(projectionElement(PERSON_ID_COL))
                                .addElements(distanceFunction)
                ).setOrder(
                    Order.newBuilder()
                        .addComponents(Component.newBuilder().setColumn(columnName(DB_DISTANCE_VALUE_QUALIFIER))
                        .setDirection(Direction.ASCENDING)).build()
                ).setLimit(limit)).build();
    }

    private static float angle(Point2D_F32 p1, Point2D_F32 c, Point2D_F32 p2) {
        return (float) (Math.atan2(p2.y - c.y, p2.x - c.x) - Math.atan2(p1.y - c.y, p1.x - c.x));
    }

    private static float angle(Skeleton skeleton, Skeleton.SkeletonPointName p1, Skeleton.SkeletonPointName p2, Skeleton.SkeletonPointName p3) {
        return angle(skeleton.getPoint(p1), skeleton.getPoint(p2), skeleton.getPoint(p3));
    }

    private static float min(float f, float g, float h) {
        return Math.min(f, Math.min(g, h));
    }

    private static float min(Skeleton skeleton, Skeleton.SkeletonPointName p1, Skeleton.SkeletonPointName p2, Skeleton.SkeletonPointName p3) {
        return min(skeleton.getWeight(p1), skeleton.getWeight(p2), skeleton.getWeight(p3));
    }

    // TODO or FIXME: Remove
    public static void main(String[] args) throws IOException {

        File baseDir = new File("../../Downloads/VBS2022/VBS2022");
        File[] folders = baseDir.listFiles(File::isDirectory);

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<SkeletonEntry>> typeRef = new TypeReference<>() {
        };

        DatabaseConfig config = new DatabaseConfig();
        config.setHost("localhost");
        config.setPort(1865);

        final CottontailWrapper ctWrapper = new CottontailWrapper("localhost", 1865);


        SkeletonPose sp = new SkeletonPose();

        int batchSize = 10000;
        boolean insert = true;
        if (insert) {
            sp.initalizePersistentLayer(() -> new CottontailEntityCreator(ctWrapper));
            sp.init(() -> new CottontailWriter(ctWrapper, 100, true));
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

        Skeleton skeleton = mapper.readValue(new File(baseDir, "00001/shot00001_10_RKF.json"), typeRef).get(0).toSkeleton();

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
