package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.config.Config;

public class DefaultExtractorInitializer implements ExtractorInitializer {

  @Override
  public void initialize(Extractor e) {
    e.init(Config.getDatabaseConfig().getWriterSupplier());
  }

}
