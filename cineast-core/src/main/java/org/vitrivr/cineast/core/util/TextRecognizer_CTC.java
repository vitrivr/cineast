package org.vitrivr.cineast.core.util;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TextRecognizer is able to take a frame with coordinates, and return the scene text within it It applies generic CTC decoding and can thus be used for any CTC-based text recognizer (e.g. CRNN) The latin alphabet is used. Changes in the alphabet should only be done if the recognition module has been trained with it accordingly
 */
public class TextRecognizer_CTC {

  static {
    nu.pattern.OpenCV.loadLocally();
  }

  private Net model;
  private final int width = 100;
  private final int height = 32;
  private final String[] alphabet = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
      "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
      "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

  public TextRecognizer_CTC initialize(String modelPath) {
    this.model = Dnn.readNetFromONNX(modelPath);
    return this;
  }

  public TextRecognizer_CTC initialize() {
    return initialize("resources/TextSpotter/CRNN_VGG_BiLSTM_CTC.onnx");
  }

  /**
   * quadrilateralCrop crops the image by warping the quadrilateral position to a rectangular one
   *
   * @param coordinate The quadrilateral coordinate of the text within the frame
   * @param frame      The frame containing the text
   * @return The quadrilaterally cropped image containing the text
   */
  private Mat quadrilateralCrop(Point[] coordinate, Mat frame) {
    MatOfPoint2f src = new MatOfPoint2f(coordinate[0], coordinate[1], coordinate[2], coordinate[3]);
    MatOfPoint2f dst = new MatOfPoint2f(
        new Point(0, height - 1),
        new Point(0, 0),
        new Point(width - 1, 0),
        new Point(width - 1, height - 1)
    );
    Mat warp = Imgproc.getPerspectiveTransform(src, dst);
    Mat cropped = new Mat();
    Imgproc.warpPerspective(frame, cropped, warp, new Size(this.width, this.height));
    return cropped;
  }

  /**
   * rectangularCrop crops the image according to the coordinate supplied
   *
   * @param coordinate The quadrilateral coordinate of the text within the frame
   * @param frame      The frame containing the text
   * @return The rectangularly cropped image containing the text
   */
  private Mat rectangularCrop(Point[] coordinate, Mat frame) {
    Rect rectangle = Imgproc.boundingRect(Converters.vector_Point_to_Mat(Arrays.asList(coordinate)));
    if (rectangle.x + rectangle.width > frame.width()) {
      rectangle.width = frame.width() - rectangle.x;
    }
    if (rectangle.y + rectangle.height > frame.height()) {
      rectangle.height = frame.height() - rectangle.y;
    }
    Mat cropped = new Mat(frame, rectangle);
    Mat resized = new Mat();
    Imgproc.resize(cropped, resized, new Size(this.width, this.height));
    return resized;
  }

  /**
   * decode takes an output layer and extracts the string using CTC-based decoding
   *
   * @param predictions The output layer of the model
   * @return The extracted string from the layer
   */
  private String decode(Mat predictions) {
    List<String> text = new ArrayList<>();

    for (int i = 0; i < predictions.rows(); i++) {
      int maxIndex = 0;
      for (int j = 1; j < predictions.cols(); j++) {
        if (predictions.get(i, j)[0] > predictions.get(i, maxIndex)[0]) {
          maxIndex = j;
        }
      }
      if (maxIndex > 0 && maxIndex <= this.alphabet.length) {
        text.add(this.alphabet[maxIndex - 1]);
      } else {
        text.add("-");
      }
    }
    StringBuffer char_list = new StringBuffer();
    for (int i = 0; i < text.size(); i++) {
      if (!text.get(i).equals("-") && !(i > 0 && text.get(i).equals(text.get(i - 1)))) {
        char_list.append(text.get(i));
      }
    }
    return char_list.toString();
  }

  /**
   * @param coordinate    The coordinate of the text
   * @param frame         The frame cotnaining the text
   * @param quadrilateral If true, the region of the coordinates is regarded as quadrilateral, and quadrilateral cropping is applied
   * @return The extracted text within the coordinate.
   */
  public String recognize(Point[] coordinate, Mat frame, boolean quadrilateral) {
    assert model != null : "Model has not been initialized!";
    Mat cropped = quadrilateral ? quadrilateralCrop(coordinate, frame) : rectangularCrop(coordinate, frame);
    Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_RGB2GRAY);
    Mat blob = Dnn.blobFromImage(cropped, 1 / 127.5, new Size(100, 32), new Scalar(127.5));
    model.setInput(blob);
    List<Mat> outs = new ArrayList<>();
    model.forward(outs);
    Mat result = outs.get(0);
    return decode(result.reshape(1, (int) result.size().height));
  }
}

