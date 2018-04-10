package org.vitrivr.cineast.core.metadata;

import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.util.MetadataUtil;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class EXIFMetadataExtractor implements MetadataExtractor {

  /**
   * Named key's that will be extracted from the EXIF metadata (if available).
   */
  private static final HashMap<String, Integer> FIELDS = new HashMap<>();


  static {
    FIELDS.put("Copyright", ExifDirectoryBase.TAG_COPYRIGHT);
    FIELDS.put("Datetime original", ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
    FIELDS.put("Datetime digitized", ExifDirectoryBase.TAG_DATETIME_DIGITIZED);
    FIELDS.put("Height", ExifDirectoryBase.TAG_IMAGE_HEIGHT);
    FIELDS.put("Width", ExifDirectoryBase.TAG_IMAGE_WIDTH);
    FIELDS.put("Author", ExifDirectoryBase.TAG_WIN_AUTHOR);
    FIELDS.put("Keywords", ExifDirectoryBase.TAG_WIN_KEYWORDS);
    FIELDS.put("Subject", ExifDirectoryBase.TAG_WIN_SUBJECT);
    FIELDS.put("Title", ExifDirectoryBase.TAG_WIN_TITLE);
    FIELDS.put("Comment", ExifDirectoryBase.TAG_USER_COMMENT);
  }


  /**
   * Extracts the metadata from the specified path and returns a List of
   * MultimediaMetadataDescriptor objects (one for each metadata entry).
   *
   * @param objectId ID of the multimedia object for which metadata will be generated.
   * @param path Path to the file for which metadata should be extracted.
   * @return List of MultimediaMetadataDescriptors. The list may be empty but must always be
   * returned!
   */
  @Override
  public List<MultimediaMetadataDescriptor> extract(String objectId, Path path) {
    ExifSubIFDDirectory md = MetadataUtil
        .getMetadataDirectoryOfType(path, ExifSubIFDDirectory.class);
    if (md == null) {
      return Collections.emptyList();
    }
    Set<Entry<String, Object>> set = Maps.transformValues(FIELDS, md::getObject).entrySet();
    return set.stream().filter(e -> e.getValue() != null).map(
        e -> MultimediaMetadataDescriptor.of(objectId, this.domain(), e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }


  @Override
  public String domain() {
    return "EXIF";
  }
}
