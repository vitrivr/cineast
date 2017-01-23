package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;

public class DefaultExtractorInitializer implements ExtractorInitializer {


  private PersistencyWriterSupplier persistencyWriterSupplier;

  /**
   * Default constructor; uses the application configuration.
   */
  public DefaultExtractorInitializer() {
    this(Config.sharedConfig().getDatabase().getWriterSupplier());
  }

  /**
   * Constructor that allows to provide a PersistencyWriterSupplier.
   *
   * @param supplier PersistencyWriterSupplier that should be used to initialize an Extractor.
   */
  public DefaultExtractorInitializer(PersistencyWriterSupplier supplier) {
    this.persistencyWriterSupplier = supplier;
  }

  @Override
  public void initialize(Extractor e) {
    e.init(this.persistencyWriterSupplier);
  }

}
