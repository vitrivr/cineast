package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint2f;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

/** Tesseract Imports */
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/** Tensorflow Imports */
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import java.nio.file.Paths;
import java.io.*;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.charset.Charset;

/** OpenCV Imports */
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

/**
 *  OCR is handled by adding fuzziness / levenshtein-distance support to the query if there are no quotes present (as quotes indicate precision)
 *  This makes sense here since we expect small errors from OCR sources
 */

public class OCRSearch extends AbstractTextRetriever {

  public static final String OCR_TABLE_NAME = "features_ocr";

  /**
   * Default constructor for {@link OCRSearch}.
   */
  public OCRSearch() {
    super(OCR_TABLE_NAME);
  }

  public void processSegment(SegmentContainer shot) {
    BufferedImage image;
    image = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
    String text;

    SceneTextOCR det = new SceneTextOCR();
    text = det.TextDetector(image);
    this.writer.write(new SimpleFulltextFeatureDescriptor(shot.getId(), text));

    System.out.println(text);
  }

  @Override
  protected String enrichQueryTerm(String queryTerm) {
    if (queryTerm.contains("\"")) {
      return queryTerm;
    }
    return queryTerm + "~1";
  }
}

class TesseractRecognizer {

  public String recognizer(BufferedImage image){
    Tesseract tesseract = new Tesseract();
    tesseract.setDatapath("resources/OCRSearch/TesseractData/tessdata/");
    tesseract.setLanguage("eng");
    tesseract.setTessVariable("tessedit_char_whitelist", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    tesseract.setPageSegMode(6);
    tesseract.setOcrEngineMode(2);
    tesseract.setTessVariable("user_defined_dpi", "300");
    try {
      String result = tesseract.doOCR(image);
      return result;
    } catch (TesseractException e) {
      e.printStackTrace();
      return null;
    }
  }
}

class AttentionOCRrecognizer{
  private static final String modelpath = "resources/OCRSearch/AttentionOCRGraph/aocr_frozen_graph/";
  protected static byte[] graphDef;
  protected static byte[] imageBytes;

  private static byte[] readAllBytesOrExit(Path path)  {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      System.err.println("Failed to read [" + path + "]: " + e.getMessage());
      System.exit(1);
    }
    return null;
  }

  private static String executeAOCRGraph(byte[] graphDef, Tensor image) {
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      try (Session s = new Session(g);
          Tensor result = s.runner().feed("input_image_as_bytes", image).fetch("prediction").run().get(0)) {

        String rstring = new String(result.bytesValue(), Charset.forName("UTF-8"));

        return rstring;
      }
    }
  }

  public String recognizeText(BufferedImage img) throws Exception {

    graphDef = readAllBytesOrExit(Paths.get(modelpath, "frozen_model_temp.pb"));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", baos);
    imageBytes = baos.toByteArray();

    try (Tensor image = Tensor.create(imageBytes)) {
      String result = executeAOCRGraph(graphDef, image);
      return result;
    }
    catch (Exception e){
      e.printStackTrace();
      return null;
    }
  }
}

class SceneTextOCR{
  public String TextDetector(BufferedImage image){
    nu.pattern.OpenCV.loadLocally();

    float scoreThresh = 0.5f;
    float nmsThresh = 0.1f;

    Mat frame = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
    byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    frame.put(0,0, data);

    Net net = Dnn.readNetFromTensorflow("resources/OCRSearch/EASTModel/frozen_east_text_detection.pb");
    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

    Size siz = new Size(320, 320);
    int W = (int)(siz.width / 4); // width of the output geometry  / score maps
    int H = (int)(siz.height / 4); // height of those. the geometry has 4, vertically stacked maps, the score one 1
    Mat blob = Dnn.blobFromImage(frame, 1.0,siz, new Scalar(123.68, 116.78, 103.94), true, false);
    net.setInput(blob);
    List<Mat> outs = new ArrayList<>(2);
    List<String> outNames = new ArrayList<String>();
    outNames.add("feature_fusion/Conv_7/Sigmoid");
    outNames.add("feature_fusion/concat_3");
    net.forward(outs, outNames);
    String text = "";

  // Decode predicted bounding boxes.
    Mat scores = outs.get(0).reshape(1, H);
    Mat geometry = outs.get(1).reshape(1, 5 * H);
    List<Float> confidencesList = new ArrayList<>();
    List<RotatedRect> boxesList = decode(scores, geometry, confidencesList, scoreThresh);
    MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confidencesList));
    RotatedRect[] boxesArray = boxesList.toArray(new RotatedRect[0]);
    MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
    MatOfInt indices = new MatOfInt();
    Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices);

    // Calculating vertices
    Point ratio = new Point((float)frame.cols()/siz.width, (float)frame.rows()/siz.height);
    int[] indexes = indices.toArray();
    for(int i = 0; i<indexes.length;++i) {
      RotatedRect rot = boxesArray[indexes[i]];
      Point[] vertices = new Point[4];
      rot.points(vertices);
      for (int j = 0; j < 4; ++j) {
        vertices[j].x *= ratio.x;
        vertices[j].y *= ratio.y;
      }

      double min_x = vertices[0].x, min_y = vertices[0].y;
      double max_x = 0, max_y = 0;
      for (int j = 0; j < 4; j++) {
        if(vertices[j].x < min_x){
          min_x = vertices[j].x;
        }
        if(vertices[j].y < min_y){
          min_y = vertices[j].y;
        }
        if(vertices[j].x > max_x){
          max_x = vertices[j].x;
        }
        if(vertices[j].y > max_y){
          max_y = vertices[j].y;
        }
      }

      // Warping perspective
      Point tl = new Point(vertices[1].x - 5, vertices[1].y - 5);
      Point tr = new Point(vertices[2].x + 5, vertices[2].y - 5);
      Point br = new Point(vertices[3].x + 5, vertices[3].y + 5);
      Point bl = new Point(vertices[0].x - 5, vertices[0].y + 5);

      Mat processedImg = new Mat((int)(max_y - min_y), (int)(max_x - min_x)  , frame.type());
      Mat src = new MatOfPoint2f(tl, tr, br, bl);
      Mat dst = new MatOfPoint2f(new Point(0, 0), new Point(processedImg.width() - 1, 0), new Point(processedImg.width() - 1, processedImg.height() - 1), new Point(0, processedImg.height() - 1));
      Mat transform = Imgproc.getPerspectiveTransform(src, dst);
      Imgproc.warpPerspective(frame, processedImg, transform, processedImg.size());

      //Preprocessing cropped image
      Imgproc.cvtColor(processedImg, processedImg, Imgproc.COLOR_BGR2GRAY);
      Imgproc.GaussianBlur(processedImg, processedImg, new Size(3, 3),0);
//      Imgproc.adaptiveThreshold(processedImg, processedImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C ,Imgproc.THRESH_BINARY_INV, 99, 4);

      Imgproc.threshold(processedImg, processedImg, 62, 255, Imgproc.THRESH_BINARY);
      if(processedImg.get(0,0)[0] == 0){
        Core.bitwise_not(processedImg, processedImg);
      }
      Imgproc.GaussianBlur(processedImg, processedImg, new Size(3, 3),0);

      Size scaleSize = new Size(processedImg.width() * 2, processedImg.height() * 2);
      Imgproc.resize(processedImg, processedImg, scaleSize, 0, 0, Imgproc.INTER_CUBIC);
      Imgproc.GaussianBlur(processedImg, processedImg, new Size(3, 3),0);

      MatOfByte matOfByte = new MatOfByte();
      Imgcodecs.imencode(".jpg", processedImg, matOfByte);
      byte[] byteArray = matOfByte.toArray();
      InputStream in = new ByteArrayInputStream(byteArray);

      // Run through recognizer
      TesseractRecognizer tess4j = new TesseractRecognizer();
      try {
        BufferedImage bufImage = ImageIO.read(in);
        text = text + " " + tess4j.recognizer(bufImage);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return text;
  }

  private static List<RotatedRect> decode(Mat scores, Mat geometry, List<Float> confidences, float scoreThresh) {
    int W = geometry.cols();
    int H = geometry.rows() / 5;

    List<RotatedRect> detections = new ArrayList<>();
    for (int y = 0; y < H; ++y) {
      Mat scoresData = scores.row(y);
      Mat x0Data = geometry.submat(0, H, 0, W).row(y);
      Mat x1Data = geometry.submat(H, 2 * H, 0, W).row(y);
      Mat x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y);
      Mat x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y);
      Mat anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y);

      for (int x = 0; x < W; ++x) {
        double score = scoresData.get(0, x)[0];
        if (score >= scoreThresh) {
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
          Point p3 = new Point(-1 * cosA * w + offset.x,      sinA * w + offset.y);
          RotatedRect r = new RotatedRect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h), -1 * angle * 180 / Math.PI);
          detections.add(r);
          confidences.add((float) score);
        }
      }
    }
    return detections;
  }
}