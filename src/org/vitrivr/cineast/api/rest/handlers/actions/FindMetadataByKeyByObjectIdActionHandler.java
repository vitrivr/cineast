package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.components.MetadataKeyFilter;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.MetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class FindMetadataByKeyByObjectIdActionHandler extends ParsingActionHandler<IdList> {
  private static final String ATTRIBUTE_ID = ":id";
  private static final String KEY_NAME = ":key";
  
  /**
   * Processes a HTTP GET request.
   *
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MetadataQueryResult}
   */
  @Override
  public MetadataQueryResult doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(ATTRIBUTE_ID);
    final String key = parameters.get(KEY_NAME);
    final MultimediaMetadataReader reader = new MultimediaMetadataReader();
    final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(objectId);
    reader.close();
    final MetadataKeyFilter predicate = MetadataKeyFilter.createForKeywords(key);
    return new MetadataQueryResult("", descriptors.stream().filter(predicate).collect(Collectors.toList()));
  }
  
  /**
   * Processes a HTTP POST request.
   *
   * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MetadataQueryResult}
   */
  @Override
  public MetadataQueryResult doPost(IdList context, Map<String, String> parameters) {
    if(context == null || context.getIds().length == 0 ){
      return new MetadataQueryResult("", new ArrayList<>(0) );
    }
    final String key = parameters.get(KEY_NAME);
    final MultimediaMetadataReader reader = new MultimediaMetadataReader();
    final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(context.getIdList());
    reader.close();
    final MetadataKeyFilter prediate = MetadataKeyFilter.createForKeywords(key);
    return new MetadataQueryResult("",descriptors.stream().filter(prediate).collect(Collectors.toList()));
  }
  
  /**
   * Class of the message this {@link ParsingActionHandler} can process.
   *
   * @return Class<AnyMessage>
   */
  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }
}
