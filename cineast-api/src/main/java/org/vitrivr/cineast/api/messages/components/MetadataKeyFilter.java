package org.vitrivr.cineast.api.messages.components;

import java.util.function.Predicate;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * Filter for metadata, based on the key.
 * <p>
 * This class servers as filter descriptor and as actual filter.
 *
 * @author loris.sauter
 * @created 04.08.18
 */
public class MetadataKeyFilter extends AbstractMetadataFilterDescriptor implements
    Predicate<MediaObjectMetadataDescriptor> {

  /**
   * Test filter to get a keywords list as lowercase to be applied on a {@link
   * MediaObjectMetadataDescriptor}.
   *
   * @return boolean
   */
  @Override
  public boolean test(MediaObjectMetadataDescriptor mediaObjectMetadataDescriptor) {
    return getKeywordsAsListLowercase()
        .contains(mediaObjectMetadataDescriptor.getKey().toLowerCase());
  }

  /**
   * Create a metadata key filter instance for the given keywords.
   *
   * @return {@link MetadataDomainFilter}
   */
  public static MetadataKeyFilter createForKeywords(String... keywords) {
    MetadataKeyFilter filter = new MetadataKeyFilter();
    filter.setKeywords(keywords);
    return filter;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
