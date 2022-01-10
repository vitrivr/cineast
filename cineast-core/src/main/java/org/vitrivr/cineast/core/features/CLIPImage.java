package org.vitrivr.cineast.core.features;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
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
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import java.awt.image.BufferedImage;
import java.io.IOException;
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

    private SavedModelBundle model;

    public CLIPImage() {
        super(TABLE_NAME, 1f, EMBEDDING_SIZE);
        model = SavedModelBundle.load(RESOURCE_PATH + EMBEDDING_MODEL);
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

        try (TFloat16 imageTensor = TFloat16.tensorOf(Shape.of(1, 3, 224, 224), DataBuffers.of(rgb))) {
            HashMap<String, Tensor> inputMap = new HashMap<>();
            inputMap.put(EMBEDDING_INPUT, imageTensor);

            System.out.println(imageTensor.shape());

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

        float[] rgb = new float[IMAGE_SIZE * IMAGE_SIZE * 3];

        if (img == null) {
            return rgb;
        }

        try {
            BufferedImage tmp;

            if (img.getWidth() > img.getHeight()) {
                tmp = Thumbnails.of(img).height(IMAGE_SIZE).asBufferedImage();
            } else {
                tmp = Thumbnails.of(img).width(IMAGE_SIZE).asBufferedImage();
            }

            tmp = Thumbnails.of(tmp).crop(Positions.CENTER).size(IMAGE_SIZE, IMAGE_SIZE).asBufferedImage();

            int[] colors = tmp.getRGB(0, 0, IMAGE_SIZE, IMAGE_SIZE, null, 0, IMAGE_SIZE);

            int gOffset = IMAGE_SIZE * IMAGE_SIZE;
            int bOffset = 2 * gOffset;

            for (int i = 0; i < colors.length; i++) {
                //std and mean used by clip during training
                rgb[i] = ((((colors[i] >> 16) & 0xFF) / 255f) - 0.48145466f) / 0.26862954f; // r)
                rgb[i + gOffset] = ((((colors[i] >> 8) & 0xFF) / 255f) - 0.4578275f) / 0.26130258f; // g
                rgb[i + bOffset] = (((colors[i] & 0xFF) / 255f) - 0.40821073f) / 0.27577711f; // b
            }

        } catch (IOException e) {
            LOGGER.error("Error while preparing image {}", e);
        }

        return rgb;

    }
}
