package org.vitrivr.cineast.core.features;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat16;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.images.ImagePreprocessingHelper;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CLIPImage extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int EMBEDDING_SIZE = 512;
    private static final String TABLE_NAME = "features_clip";
    private static final ReadableQueryConfig.Distance DISTANCE = ReadableQueryConfig.Distance.cosine;

    private static final int IMAGE_SIZE = 224;

    private static final String RESOURCE_PATH = "resources/CLIP/";
    private static final String EMBEDDING_MODEL = "clip-image-vit-32-tf";

    private static final String EMBEDDING_INPUT = "input";
    private static final String EMBEDDING_OUTPUT = "output";

    private static final float[] MEAN = new float[]{0.48145466f, 0.4578275f, 0.40821073f};
    private static final float[] STD = new float[]{0.26862954f, 0.26130258f, 0.27577711f};

    private SavedModelBundle model;

    public CLIPImage() {
        super(TABLE_NAME, 1f, EMBEDDING_SIZE);
        model = SavedModelBundle.load(RESOURCE_PATH + EMBEDDING_MODEL);
        this.correspondence = CorrespondenceFunction.identity();
    }

    @Override
    public void processSegment(SegmentContainer shot) {

        if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
            return;
        }

        float[] embeddingArray = embedImage(shot.getMostRepresentativeFrame().getImage().getBufferedImage());
        this.persist(shot.getId(), new FloatVectorImpl(embeddingArray));

    }

    @Override
    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
        return QueryConfig.clone(qc).setDistance(DISTANCE);
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

        if (sc.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
            return Collections.emptyList();
        }

        QueryConfig queryConfig = QueryConfig.clone(qc);
        queryConfig.setDistance(DISTANCE);

        float[] embeddingArray = embedImage(sc.getMostRepresentativeFrame().getImage().getBufferedImage());

        return getSimilar(embeddingArray, queryConfig);
    }

    private float[] embedImage(BufferedImage img) {

        float[] rgb = prepareImage(img);

        try (TFloat16 imageTensor = TFloat16.tensorOf(Shape.of(1, 3, IMAGE_SIZE, IMAGE_SIZE), DataBuffers.of(rgb))) {
            HashMap<String, Tensor> inputMap = new HashMap<>();
            inputMap.put(EMBEDDING_INPUT, imageTensor);

            Map<String, Tensor> resultMap = model.call(inputMap);

            try (TFloat16 encoding = (TFloat16) resultMap.get(EMBEDDING_OUTPUT)) {

                float[] embeddingArray = new float[EMBEDDING_SIZE];
                FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
                encoding.read(floatBuffer);

                return embeddingArray;

            }
        }
    }

    private static float[] prepareImage(BufferedImage img) {
        return ImagePreprocessingHelper.imageToCHWArray(
                ImagePreprocessingHelper.squaredScaleCenterCrop(img, IMAGE_SIZE),
                MEAN, STD);
    }
}
