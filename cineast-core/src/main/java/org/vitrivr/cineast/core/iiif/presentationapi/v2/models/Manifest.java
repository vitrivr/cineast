package org.vitrivr.cineast.core.iiif.presentationapi.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The overall description of the structure and properties of the digital representation of an object. It carries information needed for the viewer to present the digitized content to the user, such as a title and other descriptive information about the object or the intellectual work that it conveys. Each manifest describes how to present a single object such as a book, a photograph, or a statue.
 *
 * @author singaltanmay
 * @version 1.0
 * @created 23.06.21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Manifest {

  @JsonProperty("@context")
  private String atContext;
  @JsonProperty("@id")
  private String atId;
  @JsonProperty("@type")
  private String atType;
  @JsonProperty
  private String label;
  @JsonProperty
  private List<Metadata> metadata;
  @JsonProperty
  private String description;
  @JsonProperty
  private Thumbnail thumbnail;
  @JsonProperty
  private String viewingDirection;
  @JsonProperty
  private String viewingHint;
  @JsonProperty
  private String navDate;
  @JsonProperty
  private String license;
  @JsonProperty
  private String attribution;
  @JsonProperty
  private Logo logo;
  @JsonProperty
  private Related related;
  @JsonProperty
  private Service service;
  @JsonProperty
  private SeeAlso seeAlso;
  @JsonProperty
  private Rendering rendering;
  @JsonProperty
  private String within;
  @JsonProperty
  private List<Structure> structures;
  @JsonProperty
  private List<Sequence> sequences;

  public Manifest() {
  }

  public String getAtContext() {
    return atContext;
  }

  public void setAtContext(String atContext) {
    this.atContext = atContext;
  }

  public String getAtId() {
    return atId;
  }

  public void setAtId(String atId) {
    this.atId = atId;
  }

  public String getAtType() {
    return atType;
  }

  public void setAtType(String atType) {
    this.atType = atType;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<Metadata> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<Metadata> metadata) {
    this.metadata = metadata;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Thumbnail getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Thumbnail thumbnail) {
    this.thumbnail = thumbnail;
  }

  public String getViewingDirection() {
    return viewingDirection;
  }

  public void setViewingDirection(String viewingDirection) {
    this.viewingDirection = viewingDirection;
  }

  public String getViewingHint() {
    return viewingHint;
  }

  public void setViewingHint(String viewingHint) {
    this.viewingHint = viewingHint;
  }

  public String getNavDate() {
    return navDate;
  }

  public void setNavDate(String navDate) {
    this.navDate = navDate;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getAttribution() {
    return attribution;
  }

  public void setAttribution(String attribution) {
    this.attribution = attribution;
  }

  public Logo getLogo() {
    return logo;
  }

  public void setLogo(Logo logo) {
    this.logo = logo;
  }

  public Related getRelated() {
    return related;
  }

  public void setRelated(Related related) {
    this.related = related;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public SeeAlso getSeeAlso() {
    return seeAlso;
  }

  public void setSeeAlso(SeeAlso seeAlso) {
    this.seeAlso = seeAlso;
  }

  public Rendering getRendering() {
    return rendering;
  }

  public void setRendering(Rendering rendering) {
    this.rendering = rendering;
  }

  public String getWithin() {
    return within;
  }

  public void setWithin(String within) {
    this.within = within;
  }

  public List<Structure> getStructures() {
    return structures;
  }

  public void setStructures(List<Structure> structures) {
    this.structures = structures;
  }

  public List<Sequence> getSequences() {
    return sequences;
  }

  public void setSequences(List<Sequence> sequences) {
    this.sequences = sequences;
  }

  @Override
  public String toString() {
    return "Manifest{" +
        "atContext='" + atContext + '\'' +
        ", atId='" + atId + '\'' +
        ", atType='" + atType + '\'' +
        ", label='" + label + '\'' +
        ", metadata=" + metadata +
        ", description='" + description + '\'' +
        ", thumbnail=" + thumbnail +
        ", viewingDirection='" + viewingDirection + '\'' +
        ", viewingHint='" + viewingHint + '\'' +
        ", navDate='" + navDate + '\'' +
        ", license='" + license + '\'' +
        ", attribution='" + attribution + '\'' +
        ", logo=" + logo +
        ", related=" + related +
        ", service=" + service +
        ", seeAlso=" + seeAlso +
        ", rendering=" + rendering +
        ", within='" + within + '\'' +
        ", structures=" + structures +
        ", sequences=" + sequences +
        '}';
  }
}