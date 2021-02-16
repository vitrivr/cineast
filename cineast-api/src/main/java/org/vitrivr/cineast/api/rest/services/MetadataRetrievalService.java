package org.vitrivr.cineast.api.rest.services;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.vitrivr.cineast.api.messages.components.MetadataDomainFilter;
import org.vitrivr.cineast.api.messages.components.MetadataKeyFilter;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * This is a utility class to retrieve metadata in various forms.
 * <p>
 * TODO: Name and location to be discussed
 */
public class MetadataRetrievalService {

  private final MediaObjectMetadataReader objectMetadataReader;
  private final MediaSegmentMetadataReader segmentMetadataReader;

  private final boolean autoclose;

  public MetadataRetrievalService() {
    this(true);
  }

  public MetadataRetrievalService(final boolean autoclose) {
    this.autoclose = autoclose;
    this.objectMetadataReader = new MediaObjectMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    this.segmentMetadataReader = new MediaSegmentMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  }

  public List<MediaObjectMetadataDescriptor> find(final String objectId, final String domain, final String key) {
    return lookupMultimediaMetadata(objectId).stream().filter(createDomainAndKeyFilter(domain, key)).collect(Collectors.toList());
  }

  public List<MediaObjectMetadataDescriptor> findByKey(final String objectId, final String key) {
    final MetadataKeyFilter predicate = MetadataKeyFilter.createForKeywords(key);
    return lookupMultimediaMetadata(objectId).stream().filter(predicate).collect(Collectors.toList());
  }

  public List<MediaObjectMetadataDescriptor> findByKey(final List<String> objectIds, final String key) {
    final MetadataKeyFilter filter = MetadataKeyFilter.createForKeywords(key);
    return lookupMultimediaMetadata(objectIds).stream().filter(filter).collect(Collectors.toList());
  }

  public List<MediaObjectMetadataDescriptor> findByDomain(final String objectId, final String domain) {
    final MetadataDomainFilter predicate = MetadataDomainFilter.createForKeywords(domain);
    return lookupMultimediaMetadata(objectId).stream().filter(predicate).collect(Collectors.toList());
  }

  public List<MediaObjectMetadataDescriptor> findByDomain(final List<String> objectIds, final String domain) {
    final MetadataDomainFilter predicate = MetadataDomainFilter.createForKeywords(domain);
    return lookupMultimediaMetadata(objectIds).stream().filter(predicate).collect(Collectors.toList());
  }


  public List<MediaObjectMetadataDescriptor> lookupMultimediaMetadata(String objectId) {
    final List<MediaObjectMetadataDescriptor> descriptors = objectMetadataReader.lookupMultimediaMetadata(objectId);
    if (autoclose) {
      objectMetadataReader.close();
    }
    return descriptors;
  }

  public List<MediaObjectMetadataDescriptor> lookupMultimediaMetadata(List<String> ids) {
    final List<MediaObjectMetadataDescriptor> descriptors = objectMetadataReader.lookupMultimediaMetadata(ids);
    if (autoclose) {
      objectMetadataReader.close();
    }
    return descriptors;
  }


  private static Predicate<MediaObjectMetadataDescriptor> createDomainAndKeyFilter(final String domain, final String key) {
    return (m) -> m.getKey().equalsIgnoreCase(key) && m.getDomain().equalsIgnoreCase(domain);
  }

  public List<MediaSegmentMetadataDescriptor> lookupSegmentMetadata(String segmentId) {
    List<MediaSegmentMetadataDescriptor> descriptors = segmentMetadataReader.lookupMultimediaMetadata(segmentId);
    if (autoclose) {
      segmentMetadataReader.close();
    }
    return descriptors;
  }
}
