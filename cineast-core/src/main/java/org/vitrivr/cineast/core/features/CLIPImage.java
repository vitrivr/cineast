package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
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
import org.vitrivr.cineast.core.util.distance.CosineDistance;
import org.vitrivr.cineast.core.util.images.ImagePreprocessingHelper;

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

  public static void main(String[] args) {
    var images = new HashMap<String, String>();
    images.put("page.png", "a page of text about segmentation");
    images.put("chelsea.png", "a facial photo of a tabby cat");
    images.put("astronaut.png", "a portrait of an astronaut with the American flag");
    images.put("rocket.jpg", "a rocket standing on a launchpad");
    images.put("motorcycle_right.png", "a red motorcycle standing in a garage");
    images.put("camera.png", "a person looking at a camera on a tripod");
    images.put("horse.png", "a black-and-white silhouette of a horse");
    images.put("coffee.png", "a cup of coffee on a saucer");

    var imgCache = new HashMap<String, float[]>();
    var textCache = new HashMap<String, float[]>();

    var clipImg = new CLIPImage();
    var clipTxt = new CLIPText();

    images.forEach((key, text) -> {
      BufferedImage img = null;
      try {
        img = ImageIO.read(new File(key));
        var vec = clipImg.embedImage(img);
        imgCache.put(key, vec);
        var txt = clipTxt.embedText(text);
        textCache.put(text, txt);
        var txt2 = clipTxt.embedText("This is " + text);
        textCache.put("This is " + text, txt);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    var dists = new HashMap<String, Double>();
    images.keySet().forEach(file -> {
      images.values().forEach(prompt -> {
        var alt = "This is "+prompt;
        dists.put(file + " + " + prompt, CosineDistance.cosineDist(imgCache.get(file), textCache.get(prompt)));
        dists.put(file + " + " + alt, CosineDistance.cosineDist(imgCache.get(file), textCache.get(alt)));
      });
    });
    var sorted = dists.entrySet().stream().sorted(Comparator.comparingDouble((Entry<String, Double> o) -> o.getValue()).reversed())
        .map(entry -> entry.getKey() + ": " + entry.getValue())
        .collect(Collectors.toList());
    sorted.forEach(System.out::println);
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
