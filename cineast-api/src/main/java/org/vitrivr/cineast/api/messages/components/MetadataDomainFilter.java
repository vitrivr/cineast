package org.vitrivr.cineast.api.messages.components;

import java.util.function.Predicate;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * Filter for metadata, based on the domain.
 * <p>
 * This class servers as filter descriptor and as actual filter.
 */
public class MetadataDomainFilter extends AbstractMetadataFilterDescriptor implements Predicate<MediaObjectMetadataDescriptor> {

  /**
   * Create a metadata domain filter instance for the given keywords.
   *
   * @return {@link MetadataDomainFilter}
   */
  public static MetadataDomainFilter createForKeywords(String... keywords) {
    MetadataDomainFilter filter = new MetadataDomainFilter();
    filter.setKeywords(keywords);
    return filter;
  }

  /**
   * Test filter to get a keywords list as lowercase to be applied on a {@link MediaObjectMetadataDescriptor}.
   *
   * @return boolean
   */
  @Override
  public boolean test(MediaObjectMetadataDescriptor mediaObjectMetadataDescriptor) {
    return getKeywordsAsListLowercase().contains(mediaObjectMetadataDescriptor.getDomain().toLowerCase());
  }

}
