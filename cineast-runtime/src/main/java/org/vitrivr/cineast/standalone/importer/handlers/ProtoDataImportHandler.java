package org.vitrivr.cineast.standalone.importer.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.standalone.importer.TupleInsertMessageImporter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoDataImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   *
   * @param threads
   * @param batchsize
   */
  public ProtoDataImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }


  /**
   * Starts data import process using PROTO files. The method can either be used with a single file or a folder containing PROTO files.
   *
   * @param path Path to the PROTO file or a folder containing PROTO files.
   */
  @Override
  public void doImport(Path path) {
    try {
      LOGGER.info("Starting data import with PROTO files in: {}", path.toString());
      Files.walk(path, 2).filter(p -> p.toString().toLowerCase().endsWith(".bin")).forEach(p -> {
        final String filename = p.getFileName().toString();
        final String suffix = filename.substring(filename.lastIndexOf("."));
        try {
          this.futures.add(this.service.submit(new DataImportRunner(new TupleInsertMessageImporter(p.toFile()), filename.replace(suffix, ""), "proto_" + filename.replace(suffix, ""))));
        } catch (FileNotFoundException e) {
          LOGGER.error("Could not start data import for file '{}'. Skipping...?", filename);
        }
      });
      this.waitForCompletion();
      LOGGER.info("Completed data import with PROTO files in: {}", path.toString());
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException. Aborting...", path.toString());
    }
  }
}
