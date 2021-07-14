package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
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
    TesseractRecognizer tess4j = new TesseractRecognizer();
    String text;
    text = tess4j.recognizer(image);
    this.writer.write(new SimpleFulltextFeatureDescriptor(shot.getId(), text));

    AttentionOCRrecognizer aocr = new AttentionOCRrecognizer();
    try {
      text = aocr.recognizeText(image);
      this.writer.write(new SimpleFulltextFeatureDescriptor(shot.getId(), text));

    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Testing...");
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
    tesseract.setPageSegMode(1);
    tesseract.setOcrEngineMode(1);
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