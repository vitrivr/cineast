package org.vitrivr.cineast.core.extraction.metadata;

/**
 * Similar to {@link JsonMetaDataExtractor} except that it reads JSON files with the extension ".iiif" and stores in the database with the "IIIF" domain
 */
public class IIIFMetaDataExtractor extends JsonMetaDataExtractor {

  @Override
  public String domain() {
    return "IIIF";
  }

  @Override
  protected String extension() {
    return ".iiif";
  }
}
