package org.vitrivr.cineast.core.features;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.LongNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat16;
import org.tensorflow.types.TInt64;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.text.ClipTokenizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

public class CLIPText implements Retriever {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int EMBEDDING_SIZE = 512;
    private static final String TABLE_NAME = "features_clip";
    private static final ReadableQueryConfig.Distance DISTANCE = ReadableQueryConfig.Distance.cosine;

    private static final String RESOURCE_PATH = "resources/CLIP/";
    private static final String EMBEDDING_MODEL = "clip-text-vit-32-tf";

    private static final String EMBEDDING_INPUT = "input";
    private static final String EMBEDDING_OUTPUT = "output";

    private static final CorrespondenceFunction CORRESPONDENCE = CorrespondenceFunction.identity();

    private static SavedModelBundle model;

    private DBSelector selector;
    private ClipTokenizer ct = new ClipTokenizer();

    private static void init() {
        if (model == null) {
            model = SavedModelBundle.load(RESOURCE_PATH + EMBEDDING_MODEL);
        }
    }

    public CLIPText() {
        init();
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(TABLE_NAME, true, EMBEDDING_SIZE);
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(TABLE_NAME);
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(TABLE_NAME);
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

        String text = sc.getText();

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return getSimilar(new FloatArrayTypeProvider(embedText(text)), qc);
    }

    private float[] embedText(String text) {

        long[] tokens = ct.clipTokenize(text);

        LongNdArray arr = NdArrays.ofLongs(Shape.of(1, tokens.length));
        for (int i = 0; i < tokens.length; i++) {
            arr.setLong(tokens[i], 0, i);
        }

        try (TInt64 textTensor = TInt64.tensorOf(arr)) {

            HashMap<String, Tensor> inputMap = new HashMap<>();
            inputMap.put(EMBEDDING_INPUT, textTensor);

            Map<String, Tensor> resultMap = model.call(inputMap);

            try (TFloat16 embedding = (TFloat16) resultMap.get(EMBEDDING_OUTPUT)) {

                float[] embeddingArray = new float[EMBEDDING_SIZE];
                FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
                embedding.read(floatBuffer);
                return embeddingArray;

            }
        }
    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        List<PrimitiveTypeProvider> list = this.selector.getFeatureVectorsGeneric(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId), FEATURE_COLUMN_QUALIFIER, qc);
        if (list.isEmpty()) {
            LOGGER.warn("No feature vector for shotId {} found, returning empty result-list", segmentId);
            return Collections.emptyList();
        }
        return getSimilar(list.get(0), qc);
    }

    private List<ScoreElement> getSimilar(PrimitiveTypeProvider queryProvider, ReadableQueryConfig qc) {
        ReadableQueryConfig qcc = QueryConfig.clone(qc).setDistance(DISTANCE);
        List<SegmentDistanceElement> distances = this.selector.getNearestNeighboursGeneric(qc.getResultsPerModule(), queryProvider, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, qcc);
        CorrespondenceFunction function = qcc.getCorrespondenceFunction().orElse(CORRESPONDENCE);
        return DistanceElement.toScore(distances, function);
    }

    @Override
    public void finish() {
        if (this.selector != null) {
            this.selector.close();
            this.selector = null;
        }
    }
}
