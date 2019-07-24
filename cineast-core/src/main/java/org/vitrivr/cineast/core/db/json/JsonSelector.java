package org.vitrivr.cineast.core.db.json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.ImporterSelector;
import org.vitrivr.cineast.standalone.importer.JsonObjectImporter;

import java.io.File;
import java.io.IOException;

public class JsonSelector extends ImporterSelector<JsonObjectImporter> {

  private static final Logger LOGGER = LogManager.getLogger();
  
  @Override
  protected JsonObjectImporter newImporter(File f) {
    try {
      return new JsonObjectImporter(f);
    } catch (IOException e) {
      LOGGER.error("cannot access file '{}'", f.getAbsolutePath());
    }
    return null;
  }

  @Override
  protected String getFileExtension() {
    return ".json";
  }

}
