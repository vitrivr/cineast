package org.vitrivr.cineast.api.messages.components;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract descriptor of metadata filtering.
 *
 * This class serves as the superclass for metadata filters.
 *
 * Subclasses must implement {@link Predicate#test(Object)}, in which the filtering takes place.
 *
 * The ultimate goal is to list keywords, on which a list of {@link MediaObjectMetadataDescriptor}s is filtered.
 * Subclasses define on how to apply these keywords.
 *
 * @author loris.sauter
 */
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @Type(value = MetadataDomainFilter.class, name = "domain"),
    @Type(value = MetadataKeyFilter.class, name = "key")
})
public abstract class AbstractMetadataFilterDescriptor implements
    Predicate<MediaObjectMetadataDescriptor> {

  public static final String KEYWORDS_NAME = "keywords";

  protected List<String> keywords;

  public AbstractMetadataFilterDescriptor() {
  }

  public AbstractMetadataFilterDescriptor(String[] keywords) {
    this.keywords = Arrays.asList(keywords);
  }

  @JsonCreator
  public AbstractMetadataFilterDescriptor(@JsonProperty(KEYWORDS_NAME) List<String> keywords) {
    this.keywords = keywords;
  }

  @JsonProperty(value = KEYWORDS_NAME)
  public List<String> getKeywords() {
    return keywords;
  }

  @JsonProperty(value = KEYWORDS_NAME)
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  @JsonIgnore
  public String[] getKeywordsAsArray() {
    return keywords.toArray(new String[0]);
  }

  @JsonIgnore
  public void setKeywords(String[] keywords){
    this.keywords = Arrays.asList(keywords);
  }

  @JsonIgnore
  public List<String> getKeywordsAsListLowercase() {
    return getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList());
  }
}
