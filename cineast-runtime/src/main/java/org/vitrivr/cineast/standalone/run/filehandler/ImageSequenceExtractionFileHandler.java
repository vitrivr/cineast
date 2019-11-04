package org.vitrivr.cineast.standalone.run.filehandler;

import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.decode.image.ImageSequenceDecoder;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.extraction.segmenter.image.ImageSequenceSegmenter;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Deprecated
public class ImageSequenceExtractionFileHandler extends AbstractExtractionFileHandler<BufferedImage> {

  /**
   * Default constructor used to initialize the class.
   *
   * @param context ExtractionContextProvider that holds extraction specific configurations.
   */
  public ImageSequenceExtractionFileHandler(
      ExtractionContainerProvider itemProvider,
      ExtractionContextProvider context) throws IOException {
    super(itemProvider, context);
  }

  @Override
  Decoder<BufferedImage> newDecoder() {
    return new ImageSequenceDecoder();
  }

  @Override
  Segmenter<BufferedImage> newSegmenter() {
    return new ImageSequenceSegmenter(context);
  }
}
