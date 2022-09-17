package org.vitrivr.cineast.core.iiif.presentationapi.v3.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Related;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.SeeAlso;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Sequence;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Service;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Structure;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Thumbnail;

/**
 * IIIF Presentation API 3.0 manifest class.
 * <p>
 * WARNING: Does not currently parse all properties provided by Presentation API 3.0 manifests.
 */
public class Manifest_v3 {

  @JsonProperty("@context")
  public String atContext;
  @JsonProperty
  public String id;
  @JsonProperty
  public String type;
  @JsonProperty
  public LanguageValues label;
  @JsonProperty
  public Object items; // TODO: Parse
  @JsonProperty
  public List<Metadata_v3> metadata;
  @JsonProperty
  public String summary;
  @JsonProperty
  public Thumbnail thumbnail;
  @JsonProperty
  public String viewingDirection;
  @JsonProperty
  public List<String> behavior;
  @JsonProperty
  public Object logo; // TODO: Parse
  @JsonProperty
  public String navDate;
  @JsonProperty
  public String rights;
  @JsonProperty
  public Metadata_v3 requiredStatement;
  @JsonProperty
  public String provider;
  @JsonProperty
  public Related homepage;
  @JsonProperty
  public Service service;
  @JsonProperty
  public SeeAlso seeAlso;
  @JsonProperty
  public Object rendering;
  @JsonProperty
  public List<partOfItem> partOf;
  @JsonProperty
  public List<Structure> structures;
  @JsonProperty
  public List<Sequence> sequences;
}
