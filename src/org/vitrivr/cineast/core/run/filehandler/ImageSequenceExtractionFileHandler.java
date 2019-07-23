package org.vitrivr.cineast.core.run.filehandler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.decode.image.ImageSequenceDecoder;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.segmenter.image.ImageSequenceSegmenter;

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
