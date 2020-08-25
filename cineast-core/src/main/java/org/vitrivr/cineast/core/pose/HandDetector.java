package org.vitrivr.cineast.core.pose;

import gnu.trove.list.array.TFloatArrayList;
import java.nio.file.Paths;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

import static java.nio.file.Files.readAllBytes;
import static org.bytedeco.opencv.global.opencv_dnn.NMSBoxes;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLab;


public class HandDetector implements AutoCloseable {

    private Session session;
    private Graph graph;
    final static float DEFAULT_SCORE_THRESHOLD = 0.4f;
    final static float DEFAULT_IOU_THRESHOLD = 0.8f;
    private final float scoreThreshold;
    private final float iouThreshold;

    public HandDetector(float scoreThreshold, float iouThreshold) {
        this.scoreThreshold = scoreThreshold;
        this.iouThreshold = iouThreshold;
        this.initModel();
    }

    public HandDetector() {
        this.scoreThreshold = DEFAULT_SCORE_THRESHOLD;
        this.iouThreshold = DEFAULT_IOU_THRESHOLD;
        this.initModel();
    }

    private void initModel() {
        byte[] modelBytes;
        try {
            modelBytes = readAllBytes(Paths.get("resources/handtracking/ssdlitemobilenetv2.pb"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.graph = new Graph();
        graph.importGraphDef(modelBytes);
        this.session = new Session(graph);
    }

    @Override
    public void close() {
        this.session.close();
        this.graph.close();
    }

    private float[][] postprocess(int width, int height, float[][] boxes, float[] scores) {
        TFloatArrayList scoresFiltered = new TFloatArrayList();
        RectVector boxesVec = new RectVector();

        for (int i = 0; i < boxes.length; ++i) {
            float[] box = boxes[i];
            float score = scores[i];

            if (score >= this.scoreThreshold) {
                scoresFiltered.add(score);
                boxesVec.push_back(new Rect(
                    (int)(width * box[0]),
                    (int)(height * box[1]),
                    (int)(width * (box[2] - box[0])),
                    (int)(height * (box[3] - box[1]))
                ));
            }
        }

        // Perform non maximum suppression to eliminate redundant overlapping boxes with
        // lower confidences
        int[] indices = new int[scoresFiltered.size()];

        NMSBoxes(boxesVec, scoresFiltered.toArray(), this.scoreThreshold, this.iouThreshold, indices);

        float[][] keptBoxes = new float[indices.length][4];
        int keptBoxIdx = 0;
        for (int idx : indices) {
            float[] box = boxes[idx];
            // Swap x/y
            float tmp = box[0];
            box[0] = box[1];
            box[1] = tmp;
            tmp = box[2];
            box[2] = box[3];
            box[3] = tmp;
            keptBoxes[keptBoxIdx] = box;
            keptBoxIdx++;
        }
        return keptBoxes;
    }

    public float[][] getHandBboxs(MultiImage img) {
        Tensor<UInt8> inputTensor = DeepLab.prepareImage(img.getBufferedImage());

        List<Tensor<?>> results = session.runner().feed("image_tensor", inputTensor)
            .fetch("detection_boxes").fetch("detection_scores").run();
        Tensor<Float> boxes = results.get(0).expect(Float.class);
        Tensor<Float> scores = results.get(1).expect(Float.class);
        float[][][] boxesArr = new float[1][100][4];
        boxes.copyTo(boxesArr);
        float[][] scoresArr = new float[1][100];
        scores.copyTo(scoresArr);
        return postprocess(img.getWidth(), img.getHeight(), boxesArr[0], scoresArr[0]);
    }
}
