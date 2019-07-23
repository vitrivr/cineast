package org.vitrivr.cineast.core.importer.vbs2019.gvision;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class GoogleVisionTuple {

  public final GoogleVisionCategory category;
  public final Optional<GoogleVisionWebTuple> web;
  public final Optional<GoogleVisionLabelTuple> label;
  public final Optional<GoogleVisionOCRTuple> ocr;
  public final String completeID;

  public GoogleVisionTuple(GoogleVisionCategory category,
      Optional<GoogleVisionWebTuple> web,
      Optional<GoogleVisionLabelTuple> label,
      Optional<GoogleVisionOCRTuple> ocr, String completeID) {
    this.category = category;
    this.web = web;
    this.label = label;
    this.ocr = ocr;
    this.completeID = completeID;
  }

  public static GoogleVisionTuple of(GoogleVisionCategory category, JsonNode jsonNode, String completeID) {
    switch (category) {
      case PARTIALLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case WEB:
        return new GoogleVisionTuple(category, Optional.of(new GoogleVisionWebTuple(jsonNode)), Optional.absent(), Optional.absent(), completeID);
      case PAGES_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case FULLY_MATCHING_IMAGES:
        throw new UnsupportedOperationException();
      case LABELS:
        return new GoogleVisionTuple(category, Optional.absent(), Optional.of(new GoogleVisionLabelTuple(jsonNode)), Optional.absent(), completeID);
      case OCR:
        return new GoogleVisionTuple(category, Optional.absent(), Optional.absent(), Optional.of(new GoogleVisionOCRTuple(jsonNode)), completeID);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
