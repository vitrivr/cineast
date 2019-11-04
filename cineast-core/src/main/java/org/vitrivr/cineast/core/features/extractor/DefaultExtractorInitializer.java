package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;

public class DefaultExtractorInitializer implements ExtractorInitializer {


  private PersistencyWriterSupplier persistencyWriterSupplier;

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
  
  @Override
  public void initialize(MetadataFeatureModule<?> e) {
    e.init(this.persistencyWriterSupplier);
  }

}
