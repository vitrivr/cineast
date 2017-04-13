package org.vitrivr.cineast.core.db;

import java.io.File;
import java.io.FileNotFoundException;

import org.vitrivr.cineast.core.importer.TupleInsertMessageImporter;

public class ProtoSelector extends ImporterSelector<TupleInsertMessageImporter>{

  @Override
  protected TupleInsertMessageImporter newImporter(File f) {
    try {
      return new TupleInsertMessageImporter(f);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected String getFileExtension() {
    return ".bin";
  }

}
