package org.vitrivr.cineast.core.util;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.vitrivr.cineast.core.data.Pair;

import java.util.ArrayList;
import java.util.List;

public class TextDetector_EAST {
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    float confThreshold;
    float nmsThreshold;
    Net model;
    List<String> outNames;

    /**
     *
     * @param confidenceThreshold threshold that determines which detections are returned. Set low threshold if precision is not important
     * @param nonMaximumSuppressionThreshold threshold that determines the amount of overlap at which detections are fused
     */
    public TextDetector_EAST(float confidenceThreshold, float nonMaximumSuppressionThreshold) {
        this.confThreshold = confidenceThreshold;
        this.nmsThreshold = nonMaximumSuppressionThreshold;
        this.outNames = new ArrayList<>(2);
        outNames.add("feature_fusion/Conv_7/Sigmoid");
        outNames.add("feature_fusion/concat_3");

    }

    /**
     * Sets default confidence and non-maximum suppression threshold values
     * Confidence threshold = 0.5f
     * Non-maximum suppression threshold = 0.4f
     */
    public TextDetector_EAST() {
        this(0.5f, 0.4f);
    }

    /**
     * initialize takes a path to the weights file and reads it
     * @param modelPath of the weights file
     * @return returns its own instance
     */
    public TextDetector_EAST initialize(String modelPath) {
       this.model = Dnn.readNetFromTensorflow(modelPath);
       return this;
    }

    /**
     * initialize reads the default weights file
     * @return returns its own instance
     */
    public TextDetector_EAST initialize() {
        return initialize("resources/TextSpotter/frozen_east_text_detection.pb");
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
                if (score >= this.confThreshold) {
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
                    Point p3 = new Point(-1 * cosA * w + offset.x,      sinA * w + offset.y); // original trouble here !
                    RotatedRect r = new RotatedRect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h), -1 * angle * 180 / Math.PI);
                    boxes.add(r);
                    confidences.add((float) score);
                }
            }
        }
        return new Pair<>(confidences, boxes);
    }

    /**
     * detect takes an image and returns the detect text as rotated rectangles
     * @param frame The image on which the detectin should be done
     * @return The coordinates of the detected text instances (as rotated rectangles)
     */
    public Point[][] detect(Mat frame) {
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        Size size = new Size(frame.width() - (frame.width() % 32), frame.height() - (frame.height() % 32));
        int H = (int)(size.height / 4);
        Mat blob = Dnn.blobFromImage(frame, 1.0,size, new Scalar(123.68, 116.78, 103.94), true, false);
        this.model.setInput(blob);
        List<Mat> outs = new ArrayList<>(2);

        this.model.forward(outs, this.outNames);

        Mat scores = outs.get(0).reshape(1, H);
        Mat geometry = outs.get(1).reshape(1, 5 * H);
        Pair<List<Float>, List<RotatedRect>> decoded = decode(scores, geometry);
        if (decoded.first.size() == 0 || decoded.second.size() == 0) {
            return new Point[0][0];
        }
        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(decoded.first));
        RotatedRect[] boxesArray = decoded.second.toArray(new RotatedRect[0]);
        MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxesRotated(boxes, confidences, this.confThreshold, this.nmsThreshold, indices);

        Point ratio = new Point((float) frame.cols() / size.width, (float) frame.rows() / size.height);
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
                if (vertices[j].x > frame.width()) {
                    vertices[j].x = frame.width();
                }
                if (vertices[j].y > frame.height()) {
                    vertices[j].y = frame.height();
                }
            }
            allPoints[i] = vertices;
        }
        return allPoints;
    }
}
