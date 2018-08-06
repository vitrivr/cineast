package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.components.AbstractMetadataFilterDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.OptionallyFilteredIdList;
import org.vitrivr.cineast.core.data.messages.result.MetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

/**
 * Retrieves all the {@link MultimediaMetadataDescriptor}s for the given ID of a {@link
 * MultimediaObjectDescriptor}
 */
public class FindMetadataByObjectIdActionHandler extends
    ParsingActionHandler<OptionallyFilteredIdList> {

  private static final String ATTRIBUTE_ID = ":id";

  /**
   * Processes a HTTP GET request.
   *
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MetadataQueryResult}
   */
  @Override
  public MetadataQueryResult doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(ATTRIBUTE_ID);
    final MultimediaMetadataReader reader = new MultimediaMetadataReader();
    final List<MultimediaMetadataDescriptor> descriptors = reader
        .lookupMultimediaMetadata(objectId);
    reader.close();
    return new MetadataQueryResult("", descriptors);
  }

  /**
   * Processes a HTTP POST request.
   *
   * @param context Object that is handed to the invocation, usually parsed from the request body.
   * May be NULL!
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MetadataQueryResult}
   */
  @Override
  public MetadataQueryResult doPost(OptionallyFilteredIdList context,
      Map<String, String> parameters) {
    if (context == null || context.getIds().length == 0) {
      return new MetadataQueryResult("", new ArrayList<>(0));
    }

    final MultimediaMetadataReader reader = new MultimediaMetadataReader();
    List<MultimediaMetadataDescriptor> descriptors = reader
        .lookupMultimediaMetadata(context.getIdList());
    reader.close();
    if (context.hasFilters()) {
      final List<AbstractMetadataFilterDescriptor> filters = context.getFilterList();
      for (AbstractMetadataFilterDescriptor filter : filters) {
        descriptors = descriptors.stream().filter(filter).collect(Collectors.toList());
      }
    }
    return new MetadataQueryResult("", descriptors);
  }

  /**
   * Class of the message this {@link ParsingActionHandler} can process.
   *
   * @return Class<AnyMessage>
   */
  @Override
  public Class<OptionallyFilteredIdList> inClass() {
    return OptionallyFilteredIdList.class;
  }
}
