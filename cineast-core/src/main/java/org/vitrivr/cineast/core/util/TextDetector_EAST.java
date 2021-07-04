package org.vitrivr.cineast.core.util;

import java.util.concurrent.atomic.AtomicReference;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.vitrivr.cineast.core.data.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * TextDetector_EAST is able to detect scene text contained within an image
 */
public class TextDetector_EAST {

  static {
    nu.pattern.OpenCV.loadLocally();
  }

  private final float confThreshold = 0.5f;
  private final float nmsThreshold = 0.4f;
  private Net model;
  private List<String> outNames;

  /**
   * Sets default confidence and non-maximum suppression threshold values Confidence threshold = 0.5f Non-maximum suppression threshold = 0.4f
   */
  public TextDetector_EAST() {
    outNames = new ArrayList<>(2);
    outNames.add("feature_fusion/Conv_7/Sigmoid");
    outNames.add("feature_fusion/concat_3");
  }

  /**
   * initialize takes a path to the weights file and reads it
   *
   * @param modelPath of the weights file
   * @return returns its own instance
   */
  public TextDetector_EAST initialize(String modelPath) {
    this.model = Dnn.readNetFromTensorflow(modelPath);
    return this;
  }

  /**
   * initialize reads the default weights file
   *
   * @return returns its own instance
   */
  public TextDetector_EAST initialize() {
    return initialize("resources/SceneTextExtractor/frozen_east_text_detection.pb");
  }

  private Pair<List<Float>, List<RotatedRect>> decode(Mat scores, Mat geometry) {
    List<Float> confidences = new ArrayList<>();
    List<RotatedRect> boxes = new ArrayList<>();
    int W = geometry.cols();
    int H = geometry.rows() / 5;

    for (int y = 0; y < H; ++y) {
      Mat scoresData = scores.row(y);
      Mat x0Data = geometry.submat(0, H, 0, W).row(y);
      Mat x1Data = geometry.submat(H, 2 * H, 0, W).row(y);
      Mat x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y);
      Mat x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y);
      Mat anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y);

      for (int x = 0; x < W; ++x) {
        double score = scoresData.get(0, x)[0];
        if (score >= confThreshold) {
          double offsetX = x * 4.0;
          double offsetY = y * 4.0;
          double angle = anglesData.get(0, x)[0];
          double cosA = Math.cos(angle);
          double sinA = Math.sin(angle);
          double x0 = x0Data.get(0, x)[0];
          double x1 = x1Data.get(0, x)[0];
          double x2 = x2Data.get(0, x)[0];
          double x3 = x3Data.get(0, x)[0];
          double h = x0 + x2;
          double w = x1 + x3;
          Point offset = new Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2);
          Point p1 = new Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y);
          Point p3 = new Point(-1 * cosA * w + offset.x, sinA * w + offset.y);
          RotatedRect r = new RotatedRect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h), -1 * angle * 180 / Math.PI);
          boxes.add(r);
          confidences.add((float) score);
        }
      }
    }
    return new Pair<>(confidences, boxes);
  }

  private Point[][] detect(Mat scores, Mat geometry, Size size, int rows, int cols) {
    Pair<List<Float>, List<RotatedRect>> decoded = decode(scores, geometry);
    if (decoded.first.size() == 0 || decoded.second.size() == 0) {
      return new Point[0][0];
    }
    MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(decoded.first));
    RotatedRect[] boxesArray = decoded.second.toArray(new RotatedRect[0]);
    MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
    MatOfInt indices = new MatOfInt();
    Dnn.NMSBoxesRotated(boxes, confidences, confThreshold, nmsThreshold, indices);

    Point ratio = new Point((float) cols / size.width, (float) rows / size.height);
    int[] indexes = indices.toArray();
    Point[][] allPoints = new Point[indexes.length][4];

    for (int i = 0; i < indexes.length; ++i) {
      RotatedRect rot = boxesArray[indexes[i]];
      Point[] vertices = new Point[4];
      rot.points(vertices);
      for (int j = 0; j < 4; ++j) {
        if (vertices[j].x < 0) {
          vertices[j].x = 0;
        }
        if (vertices[j].y < 0) {
          vertices[j].y = 0;
        }
        vertices[j].x *= ratio.x;
        vertices[j].y *= ratio.y;
        if (vertices[j].x > cols) {
          vertices[j].x = cols;
        }
        if (vertices[j].y > rows) {
          vertices[j].y = rows;
        }
      }
      allPoints[i] = vertices;
    }
    return allPoints;

  }

  public List<Point[][]> detect (List<Mat> frames, int batchSize) {
    assert model != null : "Model has not been initialized!";
    List<Point[][]> allPoints = new ArrayList<>();
    if (frames.size() == 0) { return allPoints; }
    frames.forEach(frame -> Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB));
    int rows = frames.get(0).rows();
    int cols = frames.get(0).cols();
    Size size = new Size(frames.get(0).width() - (frames.get(0).width() % 32), frames.get(0).height() - (frames.get(0).height() % 32));
    int H = (int) (size.height / 4);
    for (int i=0; i<frames.size(); i=i+batchSize) {
      List<Mat> subFrames = frames.subList(i, Math.min(i + batchSize, frames.size()));
      Mat blob = Dnn.blobFromImages(subFrames, 1.0, size, new Scalar(123.68, 116.78, 103.94), true, false);
      List<Mat> outs = new ArrayList<>(2);
      model.setInput(blob);
      model.forward(outs, outNames);

      for (int j=0; j<subFrames.size(); j++) {
        Mat scores = outs.get(0).row(j).reshape(1, H);
        Mat geometry = outs.get(1).row(j).reshape(1, 5 * H);
        allPoints.add(detect(scores, geometry, size, rows, cols));
      }
    }
    return allPoints;
  }

  /**
   * detect takes an image and returns the detect text as rotated rectangles
   *
   * @param frame The image on which the detectin should be done
   * @return The coordinates of the detected text instances (as rotated rectangles)
   */
  public Point[][] detect(Mat frame) {
    assert model != null : "Model has not been initialized!";
    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

    Size size = new Size(frame.width() - (frame.width() % 32), frame.height() - (frame.height() % 32));
    int H = (int) (size.height / 4);
    Mat blob = Dnn.blobFromImage(frame, 1.0, size, new Scalar(123.68, 116.78, 103.94), true, false);
    List<Mat> outs = new ArrayList<>(2);
    model.setInput(blob);
    model.forward(outs, outNames);

    Mat scores = outs.get(0).reshape(1, H);
    Mat geometry = outs.get(1).reshape(1, 5 * H);

    return detect(scores, geometry, size, frame.rows(), frame.cols());
  }
}