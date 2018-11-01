package org.vitrivr.cineast.core.data.messages.components;

import java.util.function.Predicate;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * Filter for metadata, based on the domain.
 *
 * This class servers as filter descriptor and as actual filter.
 *
 * @author loris.sauter
 */
public class MetadataDomainFilter extends AbstractMetadataFilterDescriptor implements
    Predicate<MediaObjectMetadataDescriptor> {


  @Override
  public boolean test(MediaObjectMetadataDescriptor mediaObjectMetadataDescriptor) {
    return getKeywordsAsListLowercase().contains(mediaObjectMetadataDescriptor.getDomain().toLowerCase());
  }

  public static MetadataDomainFilter createForKeywords(String...keywords){
    MetadataDomainFilter filter = new MetadataDomainFilter();
    filter.setKeywords(keywords);
    return filter;
  }
}
