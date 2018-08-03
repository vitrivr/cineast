package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.MetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Finds metadata of a given object id list (REST) / object id (Web) and returns only items in a certain domain.
 *
 * <p>
 *   <h3>GET</h3>
 *   The action should contain an id and a domain, e.g. {@code /metadata/in/:domain/by/id/:id}.
 *   The response is JSON encoded and basically identical to a response from {@link FindMetadataByObjectIdActionHandler}:
 *   A list of {@link MultimediaMetadataDescriptor}s with only entries of the specified domain.
 * </p>
 * <p>
 *   <h3>POST</h3>
 *   The action should contain a domain, e.g. {@code /metadata/in/:domain}.
 *   The post body is an {@link IdList} and the response contains metadata for each id in that list, belonging to the specified domain.
 *   The response is JSON encoded and basically identical to a response from {@link FindMetadataByObjectIdActionHandler}:
 *  *   A list of {@link MultimediaMetadataDescriptor}s with only entries of the specified domain.
 * </p>
 *
 * @author loris.sauter
 */
public class FindMetadataInDomainByObjectIdActionHandler extends ParsingActionHandler<IdList> {
  private static final String ATTRIBUTE_ID = ":id";
  private static final String DOMAIN_NAME = ":domain";
  
  /**
   * Processes a HTTP GET request.
   *
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MetadataQueryResult}
   */
  @Override
  public MetadataQueryResult doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(ATTRIBUTE_ID);
    final String domain = parameters.get(DOMAIN_NAME);
    final MultimediaMetadataReader reader = new MultimediaMetadataReader();
    final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(objectId);
    reader.close();
    return new MetadataQueryResult("", filterDescriptors(domain, descriptors));
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
    final String domain = parameters.get(DOMAIN_NAME);
    final MultimediaMetadataReader reader = new MultimediaMetadataReader();
    final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(context.getIdList());
    reader.close();
    return new MetadataQueryResult("",filterDescriptors(domain, descriptors));
  }
  
  private List<MultimediaMetadataDescriptor> filterDescriptors(String domain, List<MultimediaMetadataDescriptor> list){
    return list.stream().filter(mmmdd -> mmmdd.getDomain().toLowerCase().equals(domain.toLowerCase())).collect(Collectors.toList());
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
