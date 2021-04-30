package org.vitrivr.cineast.core.features;

import org.apache.commons.lang3.NotImplementedException;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A visual-text co-embedding mapping images and text descriptions to the same embedding space.
 */
public class VisualTextCoEmbedding extends AbstractFeatureModule {

  private static final int EMBEDDING_SIZE = 256;

  /**
   * Embedding network from text to intermediary embedding.
   */
  private final SavedModelBundle textEmbedding;
  /**
   * Embedding network from text intermediary embedding to visual-text co-embedding.
   */
  private final SavedModelBundle textCoEmbedding;

  protected VisualTextCoEmbedding() {
    super("features_visualtextcoembedding", 2f, EMBEDDING_SIZE);
    // If the separation of extract from visual, query by text is strict, the models can be loaded in the respective
    // init methods.
    textEmbedding = SavedModelBundle.load("resources/VisualTextCoEmbedding/universal-sentence-encoder_4");
    textCoEmbedding = SavedModelBundle.load("resources/VisualTextCoEmbedding/text-co-embedding");
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    throw new NotImplementedException();
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    String text = sc.getText();

    float[] embeddingArray = embedText(text);

    return getSimilar(embeddingArray, qc);
  }

  private float[] embedText(String text) {
    TString textTensor = TString.tensorOf(NdArrays.vectorOfObjects(text));

    HashMap<String, Tensor> inputMap = new HashMap<>();
    inputMap.put("inputs", textTensor);

    Map<String, Tensor> resultMap = textEmbedding.call(inputMap);

    TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get("outputs");

    inputMap.clear();
    inputMap.put("textual_features", intermediaryEmbedding);

    resultMap = textCoEmbedding.call(inputMap);
    TFloat32 embedding = (TFloat32) resultMap.get("l2_norm");

    float[] embeddingArray = new float[EMBEDDING_SIZE];
    FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
    // Beware TensorFlow allows tensor writing to buffers through the function read rather than write
    embedding.read(floatBuffer);
    // Close tensors manually (although these eager tensors should be closed automatically)
    textTensor.close();
    intermediaryEmbedding.close();
    embedding.close();

    return embeddingArray;
  }
}
