package org.vitrivr.cineast.api.messages.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * Abstract descriptor of metadata filtering.
 * <p>
 * This class serves as the superclass for metadata filters.
 * <p>
 * Subclasses must implement {@link Predicate#test(Object)}, in which the filtering takes place.
 * <p>
 * The ultimate goal is to list keywords, on which a list of {@link MediaObjectMetadataDescriptor}s is filtered. Subclasses define on how to apply these keywords.
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

  /**
   * Keywords name variable necessary for JSON creation.
   */
  public static final String KEYWORDS_NAME = "keywords";

  /**
   * Keywords on which a list of {@link MediaObjectMetadataDescriptor}s is filtered.
   */
  protected List<String> keywords;

  /**
   * This default constructor is required for deserialization by fasterxml/jackson.
   */
  public AbstractMetadataFilterDescriptor() {
  }

  /**
   * Constructor for the AbstractMetadataFilterDescriptor object. Creates the internal list representation from the given array.
   *
   * @param keywords Array of keywords.
   */
  public AbstractMetadataFilterDescriptor(String[] keywords) {
    this.keywords = Arrays.asList(keywords);
  }

  /**
   * Constructor for the AbstractMetadataFilterDescriptor object.
   *
   * @param keywords List of keywords.
   */
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
  public void setKeywords(String[] keywords) {
    this.keywords = Arrays.asList(keywords);
  }

  @JsonIgnore
  public List<String> getKeywordsAsListLowercase() {
    return getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
