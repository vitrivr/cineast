package org.vitrivr.cineast.core.importer.vbs2019;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;

public class GoogleVisionTuple {

  public final GoogleVisionCategory category;
  public final Optional<GoogleVisionWebTuple> web;
  public final Optional<GoogleVisionLabelTuple> label;
  public final Optional<GoogleVisionOCRTuple> ocr;

  public GoogleVisionTuple(GoogleVisionCategory category,
      Optional<GoogleVisionWebTuple> web,
      Optional<GoogleVisionLabelTuple> label,
      Optional<GoogleVisionOCRTuple> ocr) {
    this.category = category;
    this.web = web;
    this.label = label;
    this.ocr = ocr;
  }

  public static GoogleVisionTuple of(GoogleVisionCategory category, JsonNode jsonNode) {
    switch (category) {
      case PARTIALLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case WEB:
        return new GoogleVisionTuple(category, Optional.of(new GoogleVisionWebTuple(jsonNode)), Optional.absent(), Optional.absent());
      case PAGES_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case FULLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case LABELS:
        return new GoogleVisionTuple(category, Optional.absent(), Optional.of(new GoogleVisionLabelTuple(jsonNode)), Optional.absent());
      case OCR:
        return new GoogleVisionTuple(category, Optional.absent(), Optional.absent(), Optional.of(new GoogleVisionOCRTuple(jsonNode)));
      default:
        throw new UnsupportedOperationException();
    }
  }
}
