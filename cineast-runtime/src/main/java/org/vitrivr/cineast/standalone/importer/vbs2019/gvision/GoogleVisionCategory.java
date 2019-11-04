package org.vitrivr.cineast.standalone.importer.vbs2019.gvision;

public enum GoogleVisionCategory {

  PARTIALLY_MATCHING_IMAGES("empty"), WEB("features_segmenttags"), PAGES_MATCHING_IMAGES("empty"), FULLY_MATCHING_IMAGES("empty"), LABELS("features_segmenttags"), OCR("features_ocr");

  public final String tableName;

  GoogleVisionCategory(String tableName){
    this.tableName = tableName;
  }
}
