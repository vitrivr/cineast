package org.vitrivr.cineast.core.features;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import com.google.common.cache.CacheLoader;
import georegression.struct.shapes.Quadrilateral_F64;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;
import org.vitrivr.cineast.core.util.HungarianAlgorithm;
import org.vitrivr.cineast.core.util.ThreadLocalObjectCache;
import org.vitrivr.cineast.core.util.ocr.MultiTracker;
import org.vitrivr.cineast.core.util.ocr.NeedlemanWunschMerge;
import org.vitrivr.cineast.core.util.text.TextDetector_EAST;
import org.vitrivr.cineast.core.util.text.TextRecognizer_CTC;
import org.vitrivr.cineast.core.util.text.TextStream;

/**
 * OCR is handled by adding fuzziness / levenshtein-distance support to the query if there are no quotes present (as quotes indicate precision) This makes sense here since we expect small errors from OCR sources
 */
public class OCRSearch extends AbstractTextRetriever {

  public static final String OCR_TABLE_NAME = "features_ocr";

  /**
   * Configurations rate: Rate refers to the rate at which detections are done. E.g.: Rate = 3 --> Detections are done at every third frame Increase rate for better inference times
   * <p>
   * threshold_CIoU: CIoU stands for Combined Intersection over Union When the CIoU is below 0.4, they are regarded as separate streams Should not be changed
   * <p>
   * threshold_postproc: This is the threshold for the postprocessing stream association step Strongly urge not to change
   * <p>
   * tracker_type: Refers to the tracker which is used threshold_stream_length: Refers to the amount of consecutive frames a text should minimally appear in If a text appears in less consecutive frames than the threshold, the text is discarded
   */
  private static final int rate = 3;
  private static final int batchSize = 16;
  private static final double threshold_CIoU = 0.4;
  private static final double threshold_postproc = 8;
  private static final MultiTracker.TRACKER_TYPE tracker_type = MultiTracker.TRACKER_TYPE.CIRCULANT;
  private static final int threshold_stream_length = 9;

  private static final ThreadLocalObjectCache<TextDetector_EAST> detectorCache = new ThreadLocalObjectCache<>(new CacheLoader<Thread, TextDetector_EAST>() {
    @Override
    public TextDetector_EAST load(Thread key) {
      return new TextDetector_EAST().initialize();
    }
  });

  private static final ThreadLocalObjectCache<TextRecognizer_CTC> recognizerCache = new ThreadLocalObjectCache<>(new CacheLoader<Thread, TextRecognizer_CTC>() {
    @Override
    public TextRecognizer_CTC load(Thread key) {
      return new TextRecognizer_CTC().initialize();
    }
  });

  public OCRSearch() {
    super(OCR_TABLE_NAME);
  }

  /**
   * @param coordinate The coordinate to be processed
   * @return The minimum and maximum (in that order) of the X-coordinates
   */
  private Pair<Double, Double> minimumMaximumX(Quadrilateral_F64 coordinate) {
    double x_max = Double.NEGATIVE_INFINITY;
    double x_min = Double.POSITIVE_INFINITY;

    if (coordinate.getA().x > x_max) {
      x_max = coordinate.getA().x;
    }
    if (coordinate.getA().x < x_min) {
      x_min = coordinate.getA().x;
    }
    if (coordinate.getB().x > x_max) {
      x_max = coordinate.getB().x;
    }
    if (coordinate.getB().x < x_min) {
      x_min = coordinate.getB().x;
    }
    if (coordinate.getC().x > x_max) {
      x_max = coordinate.getC().x;
    }
    if (coordinate.getC().x < x_min) {
      x_min = coordinate.getC().x;
    }
    if (coordinate.getD().x > x_max) {
      x_max = coordinate.getD().x;
    }
    if (coordinate.getD().x < x_min) {
      x_min = coordinate.getD().x;
    }

    return new Pair<>(x_min, x_max);
  }

  /**
   * @param coordinate The coordinate to be processed
   * @return The minimum and maximum (in that order) of the Y-coordinates
   */
  private Pair<Double, Double> minimumMaximumY(Quadrilateral_F64 coordinate) {
    double y_max = Double.NEGATIVE_INFINITY;
    double y_min = Double.POSITIVE_INFINITY;

    if (coordinate.getA().y > y_max) {
      y_max = coordinate.getA().y;
    }
    if (coordinate.getA().y < y_min) {
      y_min = coordinate.getA().y;
    }
    if (coordinate.getB().y > y_max) {
      y_max = coordinate.getB().y;
    }
    if (coordinate.getB().y < y_min) {
      y_min = coordinate.getB().y;
    }
    if (coordinate.getC().y > y_max) {
      y_max = coordinate.getC().y;
    }
    if (coordinate.getC().y < y_min) {
      y_min = coordinate.getC().y;
    }
    if (coordinate.getD().y > y_max) {
      y_max = coordinate.getD().y;
    }
    if (coordinate.getD().y < y_min) {
      y_min = coordinate.getD().y;
    }

    return new Pair<>(y_min, y_max);
  }

  /**
   * img2Mat converts the buffered image to an RGB (OpenCV) Mat image
   *
   * @param original The buffered image to be converted
   * @return The buffered image as an RGB (OpenCV) Mat image
   */
  private Mat img2Mat(BufferedImage original) {
    BufferedImage in = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphic = in.createGraphics();
    try {
      graphic.setComposite(AlphaComposite.Src);
      graphic.drawImage(original, 0, 0, null);
    } finally {
      graphic.dispose();
    }

    Mat out;
    byte[] data;

    out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
    data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
    int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
    for (int i = 0; i < dataBuff.length; i++) {
      data[i * 3] = (byte) ((dataBuff[i]) & 0xFF);
      data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
      data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
    }

    out.put(0, 0, data);
    return out;
  }

  private GrayU8 bufferedImage2GrayU8(SegmentContainer shot, int frame_index) {
    return ConvertBufferedImage.convertFromSingle(
        shot.getVideoFrames().get(frame_index).getImage().getBufferedImage(), null, GrayU8.class);
  }

  /**
   * @param streamPast   Stream which appears before streamFuture
   * @param streamFuture Stream which appears after streamPast
   * @return The similarity of the streams by taking into account the spatial distance, the overlap, the text similarity, and the frame offset
   */
  private double getSimilarity(TextStream streamPast, TextStream streamFuture) {
    Quadrilateral_F64 lastBox = streamPast.findLastBox();
    Quadrilateral_F64 firstBox = streamFuture.findFirstBox();

    double spatial_x = (Math.abs(lastBox.getA().x - firstBox.getA().x) + Math.abs(lastBox.getB().x - firstBox.getB().x) +
        Math.abs(lastBox.getC().x - firstBox.getC().x) + Math.abs(lastBox.getD().x - firstBox.getD().x)) / 4;
    double spatial_y = (Math.abs(lastBox.getA().y - firstBox.getA().y) + Math.abs(lastBox.getB().y - firstBox.getB().y) +
        Math.abs(lastBox.getC().y - firstBox.getC().y) + Math.abs(lastBox.getD().y - firstBox.getD().y)) / 4;

    double spatial_dis = (spatial_x / Math.max(Math.abs(lastBox.getB().x - lastBox.getA().x), Math.abs(firstBox.getB().x - firstBox.getA().x)))
        + (spatial_y / Math.max(Math.abs(lastBox.getD().y - lastBox.getA().y), Math.abs(firstBox.getD().y - firstBox.getA().y)));

    double inv_IoU = 1 - getIntersectionOverUnion(lastBox, firstBox);

    double edit_dis = 1 - new JaroWinklerSimilarity().apply(streamPast.getText(), streamFuture.getText());

    return spatial_dis + inv_IoU + 10 * edit_dis + 0.2 * Math.abs(streamFuture.getFirst() - streamPast.getLast());
  }

  /**
   * transformString transforms the string by replacing certain characters with others. This is helpful since certain characters are visually (almost) indistinguishable to the recognition module
   *
   * @param str The string to be transformed
   * @return The transformed string
   */
  private String transformString(String str) {
    return str.replace("i", "l").replace("l", "1").replace("a", "o");
  }

  /**
   * saveText transforms and saves the scene text in the database
   *
   * @param id          id of the shot (the {@link SegmentContainer}) to be processed
   * @param recognition The scene text that should be safed
   */
  private void saveText(String id, String recognition) {
    if (recognition.equals("")) {
      return;
    }
    this.writer.write(new SimpleFulltextFeatureDescriptor(id, transformString(recognition)));
  }

  /**
   * Computes and returns the IoU of the two rectangular boxes. The method will not work if they are not rectangular
   *
   * @param coordA Coordinate of a rectangular box
   * @param coordB Coordinate of a rectangular box
   * @return the intersection over union of the two rectangular coordinates
   */
  private double getIntersectionOverUnion(Quadrilateral_F64 coordA, Quadrilateral_F64 coordB) {
    double[] boxA = new double[]{coordA.getA().x, coordA.getA().y, coordA.getC().x, coordA.getC().y};
    double[] boxB = new double[]{coordB.getA().x, coordB.getA().y, coordB.getC().x, coordB.getC().y};

    double xA = Math.max(boxA[0], boxB[0]);
    double yA = Math.max(boxA[1], boxB[1]);
    double xB = Math.min(boxA[2], boxB[2]);
    double yB = Math.min(boxA[3], boxB[3]);

    double interArea = Math.max(0, xB - xA + 1) * Math.max(0, yB - yA + 1);

    double boxAArea = (boxA[2] - boxA[0] + 1) * (boxA[3] - boxA[1] + 1);
    double boxBArea = (boxB[2] - boxB[0] + 1) * (boxB[3] - boxB[1] + 1);

    return interArea / (boxAArea + boxBArea - interArea);
  }

  /**
   * getAverageIntersectionOverUnion takes two lists of coordinates and returns the average IoU The coordinates provided have to describe a rectangular box
   *
   * @param coordinates1 Sequentially ordered coordinates (from frame i to frame i+n)
   * @param coordinates2 Sequentially ordered coordinates (from frame i to frame i+n)
   * @return The average intersection over union
   */
  public double getAverageIntersectionOverUnion(List<Quadrilateral_F64> coordinates1, List<Quadrilateral_F64> coordinates2) {
    double total_IoU = 0;
    int count = 0;

    for (int i = 0; i < coordinates1.size(); i++) {
      if (coordinates1.get(i) == null || coordinates2.get(i) == null) {
        continue;
      }
      count++;
      total_IoU = total_IoU + getIntersectionOverUnion(coordinates1.get(i), coordinates2.get(i));
    }

    return count == 0 ? 0 : total_IoU / count;
  }

  /**
   * Extracts the scene text and ingests it using the {@link SimpleFulltextFeatureDescriptor}.
   *
   * @param shot The {@link SegmentContainer} that should be processed.
   */
  @Override
  public void processSegment(SegmentContainer shot) {
    TextDetector_EAST detector = detectorCache.get();
    TextRecognizer_CTC recognizer = recognizerCache.get();

    int lenVideo = shot.getVideoFrames().size();
    // Scene text extraction for image
    if (lenVideo == 1) {
      Mat frame = img2Mat(shot.getVideoFrames().get(0).getImage().getBufferedImage());
      Point[][] coordinates = detector.detect(frame);
      StringBuilder sb = new StringBuilder();
      for (Point[] coordinate : coordinates) {
        sb.append(recognizer.recognize(coordinate, frame, true)).append('\n');
      }
      saveText(shot.getId(), sb.toString());
      return;
    }

    // Scene text extraction for video
    List<TextStream> streams = new ArrayList<>();

    List<Mat> matFrames = new ArrayList<>();
    for (int i = 0; i < lenVideo; i = i + rate) {
      matFrames.add(img2Mat(shot.getVideoFrames().get(i).getImage().getBufferedImage()));
    }

    List<Point[][]> detections = detector.detect(matFrames, batchSize);
    List<GrayU8> frames_grayU8 = new ArrayList<>();

    for (int i = 0; i < lenVideo; i++) {
      frames_grayU8.add(bufferedImage2GrayU8(shot, i));
    }

    for (int i = 0; i + rate < lenVideo && i < lenVideo; i = i + rate) {
      List<List<Quadrilateral_F64>> tracking_forward = new ArrayList<>();
      List<List<Quadrilateral_F64>> tracking_backward = new ArrayList<>();

      // Forward Tracking (from frame i to frame i+rate)
      List<Quadrilateral_F64> coordinates_tracking = new ArrayList<>();
      Point[][] initialCoordinates = detections.get(i / rate);
      if (initialCoordinates.length == 0) {
        continue;
      }
      int count = 0;
      for (Point[] coordinate : initialCoordinates) {
        Quadrilateral_F64 coordinate_tracking = new Quadrilateral_F64(coordinate[1].x, coordinate[1].y, coordinate[0].x, coordinate[0].y, coordinate[3].x, coordinate[3].y, coordinate[2].x, coordinate[2].y);
        Pair<Double, Double> minMaxX = minimumMaximumX(coordinate_tracking);
        Pair<Double, Double> minMaxY = minimumMaximumY(coordinate_tracking);
        coordinate_tracking = new Quadrilateral_F64(minMaxX.first, minMaxY.first, minMaxX.second, minMaxY.first, minMaxX.second, minMaxY.second, minMaxX.first, minMaxY.second);

        coordinates_tracking.add(coordinate_tracking);
        tracking_forward.add(new ArrayList<>());
        tracking_forward.get(count).add(coordinate_tracking);
        count++;
      }

      MultiTracker tracker_forward = new MultiTracker(bufferedImage2GrayU8(shot, i), coordinates_tracking, tracker_type);

      for (int j = i + 1; j < lenVideo && j <= i + rate; j++) {
        List<Pair<Boolean, Quadrilateral_F64>> new_coordinates = tracker_forward.update(frames_grayU8.get(j));
        for (int k = 0; k < new_coordinates.size(); k++) {
          if (new_coordinates.get(k).first) {
            tracking_forward.get(k).add(new_coordinates.get(k).second);
          } else {
            tracking_forward.get(k).add(null);
          }
        }
      }

      // Backward Tracking (from frame i+rate to frame i)
      coordinates_tracking.clear();
      initialCoordinates = detections.get((i + rate) / rate);
      if (initialCoordinates.length == 0) {
        continue;
      }
      count = 0;
      for (Point[] coordinate : initialCoordinates) {
        Quadrilateral_F64 coordinate_tracking = new Quadrilateral_F64(coordinate[1].x, coordinate[1].y, coordinate[2].x, coordinate[2].y, coordinate[3].x, coordinate[3].y, coordinate[0].x, coordinate[0].y);
        coordinates_tracking.add(coordinate_tracking);
        tracking_backward.add(new ArrayList<>());
        tracking_backward.get(count).add(coordinate_tracking);
        count++;
      }

      MultiTracker tracker_backward = new MultiTracker(bufferedImage2GrayU8(shot, i + rate), coordinates_tracking, tracker_type);
      for (int j = i + rate - 1; j >= 0 && j >= i; j--) {
        List<Pair<Boolean, Quadrilateral_F64>> new_coordinates = tracker_backward.update(frames_grayU8.get(j));
        for (int k = 0; k < new_coordinates.size(); k++) {
          if (new_coordinates.get(k).first) {
            tracking_backward.get(k).add(0, new_coordinates.get(k).second);
          } else {
            tracking_backward.get(k).add(0, null);
          }
        }
      }

      // Find best matches between the forward tracking stream and the backward tracking one and compare it to threshold_CiOU
      double[][] cost = new double[tracking_forward.size()][tracking_backward.size()];
      for (int j = 0; j < tracking_forward.size(); j++) {
        for (int k = 0; k < tracking_backward.size(); k++) {
          cost[j][k] = 1 - getAverageIntersectionOverUnion(tracking_forward.get(j), tracking_backward.get(k));
        }
      }
      HungarianAlgorithm optimization = new HungarianAlgorithm(cost);

      int[] pairs = optimization.execute();

      for (int j = 0; j < tracking_forward.size(); j++) {
        if (pairs[j] != -1 && cost[j][pairs[j]] <= 1 - threshold_CIoU) {
          TextStream stream = null;
          for (TextStream textStream : streams) {
            if (textStream.getLast() == i && textStream.getCoordinate_id() == j) {
              stream = textStream;
              stream.add(i, i + rate, pairs[j], tracking_forward.get(j), tracking_backward.get(pairs[j]));
            }
          }
          if (stream == null) {
            stream = new TextStream(i, i + rate, pairs[j], tracking_forward.get(j), tracking_backward.get(pairs[j]));
            streams.add(stream);
          }
        }
      }
    }

    List<TextStream> shouldRemove = new ArrayList<>();

    // Recognize text within each stream
    for (TextStream stream : streams) {
      HashMap<Integer, Quadrilateral_F64> filtered = stream.getFilteredCoordinates();
      Iterator<Integer> frameIterator = filtered.keySet().iterator();
      HashMap<String, Integer> counts = new HashMap<>();

      // Recognize text from the filtered coordinates and count the amount of times they occur
      while (frameIterator.hasNext()) {
        int key = frameIterator.next();

        Quadrilateral_F64 coord_before = filtered.get(key);
        Point[] coordinates = new Point[]{new Point(coord_before.getD().x, coord_before.getD().y), new Point(coord_before.getA().x, coord_before.getA().y), new Point(coord_before.getB().x, coord_before.getB().y), new Point(coord_before.getC().x, coord_before.getC().y)};
        Mat frame = img2Mat(shot.getVideoFrames().get(key).getImage().getBufferedImage());
        String recognition = recognizer.recognize(coordinates, frame, false);
        Integer count = counts.get(recognition);
        counts.put(recognition, count != null ? count + 1 : 1);
      }

      // Select the recognitions which appear most often (majority voting)
      int max_count = 0;
      List<String> prunedRecognitions = new ArrayList<>();
      for (HashMap.Entry<String, Integer> val : counts.entrySet()) {
        if (max_count < val.getValue()) {
          prunedRecognitions.clear();
          prunedRecognitions.add(val.getKey());
          max_count = val.getValue();
        } else if (max_count == val.getValue()) {
          prunedRecognitions.add(val.getKey());
        }
      }

      // If there are two recognitions which appear most often, apply NeedlemanWunschMerge. If there are more than two, then delete the stream
      if (prunedRecognitions.size() == 1) {
        stream.setText(prunedRecognitions.get(0));
      } else if (prunedRecognitions.size() == 2) {
        stream.setText(new NeedlemanWunschMerge(prunedRecognitions.get(0), prunedRecognitions.get(1)).execute());
      } else {
        shouldRemove.add(stream); // Streams who cannot agree on at most two recognitions are considered unstable and removed
      }
    }

    for (TextStream stream : shouldRemove) {
      streams.remove(stream);
    }

    HashMap<Integer, List<TextStream>> firsts = new HashMap<>();
    HashMap<Integer, List<TextStream>> lasts = new HashMap<>();
    for (TextStream stream : streams) {
      int first = stream.getFirst();
      int last = stream.getLast();
      if (firsts.containsKey(first)) {
        List<TextStream> s = firsts.get(first);
        s.add(stream);
      } else {
        List<TextStream> s = new ArrayList<>();
        s.add(stream);
        firsts.put(first, s);
      }
      if (lasts.containsKey(last)) {
        List<TextStream> s = lasts.get(last);
        s.add(stream);
      } else {
        List<TextStream> s = new ArrayList<>();
        s.add(stream);
        lasts.put(last, s);
      }
    }

    // Combine streams which are considered to be highly similar
    // This is done to overcome scenarios in which the object was occluded during the detection, or couldn't be detected for other reasons, which resulted in two or more streams for the same text instance
    int distance = rate;
    while (distance < threshold_postproc / 0.2) {
      for (int i = rate; i + distance < lenVideo; i = i + rate) {
        List<TextStream> streams_last = lasts.get(i);
        List<TextStream> streams_first = firsts.get(i + distance);
        if (streams_last == null || streams_last.size() == 0 || streams_first == null || streams_first.size() == 0) {
          continue;
        }

        double[][] cost = new double[streams_last.size()][streams_first.size()];
        for (int j = 0; j < streams_last.size(); j++) {
          for (int k = 0; k < streams_first.size(); k++) {
            cost[j][k] = getSimilarity(streams_last.get(j), streams_first.get(k));
          }
        }
        HungarianAlgorithm optimization = new HungarianAlgorithm(cost);
        int[] pairs = optimization.execute();

        List<Pair<TextStream, TextStream>> matches = new ArrayList<>();
        for (int j = 0; j < streams_last.size(); j++) {
          if (pairs[j] < 0) {
            continue;
          }
          if (cost[j][pairs[j]] < threshold_postproc) {
            matches.add(new Pair<>(streams_last.get(j), streams_first.get(pairs[j])));
          }
        }
        for (Pair<TextStream, TextStream> match : matches) {
          TextStream stream_last = match.first;
          TextStream stream_first = match.second;
          if (!stream_last.getText().equals(stream_first.getText())) {
            if ((stream_last.getLast() - stream_last.getFirst()) > (stream_first.getLast() - stream_first.getFirst())) {
              stream_first.setText(stream_last.getText());
            } else if ((stream_last.getLast() - stream_last.getFirst()) == (stream_first.getLast() - stream_first.getFirst())) {
              stream_first.setText(new NeedlemanWunschMerge(stream_last.getText(), stream_first.getText()).execute());
            }
          }

          firsts.get(stream_last.getFirst()).remove(stream_last);
          lasts.get(stream_last.getLast()).remove(stream_last);
          streams.remove(stream_last);
          firsts.get(stream_first.getFirst()).remove(stream_first);
          firsts.get(stream_last.getFirst()).add(stream_first);

          stream_first.add(stream_last);
        }
      }
      distance = distance + rate;
    }

    shouldRemove.clear();
    // Remove streams which appear in less frames than specified in threshold_stream_length
    // This is done to prune unreliable streams and text instances which the user could not possibly have memorized
    for (TextStream stream : streams) {
      if ((stream.getLast() - stream.getFirst()) < threshold_stream_length) {
        shouldRemove.add(stream);
      }
    }
    for (TextStream stream : shouldRemove) {
      streams.remove(stream);
    }
    StringBuilder sb = new StringBuilder();
    streams.forEach(s -> sb.append(s.getText()).append('\n'));
    saveText(shot.getId(), sb.toString());
  }

  @Override
  protected String enrichQueryTerm(String queryTerm) {
    // The EAST text detector views text instances with a slash inbetween as one text instance
    // The recognizer cannot recognize a slash. Subsequently we also remove it in the query term
    queryTerm = transformString(queryTerm.replaceAll("-", "").toLowerCase(Locale.ROOT));
    if (queryTerm.contains("\"")) {
      return queryTerm;
    }
    return queryTerm + "~1";
  }
}
