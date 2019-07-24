package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.components.MetadataDomainFilter;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Finds metadata of a given object id list (REST) / object id (Web) and returns only items in a
 * certain domain.
 *
 * <p>
 * <h3>GET</h3>
 * The action should contain an id and a domain, e.g. {@code /metadata/in/:domain/by/id/:id}. The
 * response is JSON encoded and basically identical to a response from {@link
 * FindMetadataByObjectIdActionHandler}: A list of {@link MediaObjectMetadataDescriptor}s with only
 * entries of the specified domain.
 * </p>
 * <p>
 * <h3>POST</h3>
 * The action should contain a domain, e.g. {@code /metadata/in/:domain}. The post body is an {@link
 * IdList} and the response contains metadata for each id in that list, belonging to the specified
 * domain. The response is JSON encoded and basically identical to a response from {@link
 * FindMetadataByObjectIdActionHandler}: *   A list of {@link MediaObjectMetadataDescriptor}s with
 * only entries of the specified domain.
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
   * @return {@link MediaObjectMetadataQueryResult}
   */
  @Override
  public MediaObjectMetadataQueryResult doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(ATTRIBUTE_ID);
    final String domain = parameters.get(DOMAIN_NAME);
    final MediaObjectMetadataReader reader = new MediaObjectMetadataReader();
    final List<MediaObjectMetadataDescriptor> descriptors = reader
        .lookupMultimediaMetadata(objectId);
    reader.close();
    final MetadataDomainFilter predicate = MetadataDomainFilter.createForKeywords(domain);
    return new MediaObjectMetadataQueryResult("",
        descriptors.stream().filter(predicate).collect(Collectors.toList()));
  }

  /**
   * Processes a HTTP POST request.
   *
   * @param context Object that is handed to the invocation, usually parsed from the request body.
   * May be NULL!
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MediaObjectMetadataQueryResult}
   */
  @Override
  public MediaObjectMetadataQueryResult doPost(IdList context, Map<String, String> parameters) {
    if (context == null || context.getIds().length == 0) {
      return new MediaObjectMetadataQueryResult("", new ArrayList<>(0));
    }
    final String domain = parameters.get(DOMAIN_NAME);
    final MediaObjectMetadataReader reader = new MediaObjectMetadataReader();
    final List<MediaObjectMetadataDescriptor> descriptors = reader
        .lookupMultimediaMetadata(context.getIdList());
    reader.close();
    final MetadataDomainFilter predicate = MetadataDomainFilter.createForKeywords(domain);
    return new MediaObjectMetadataQueryResult("",
        descriptors.stream().filter(predicate).collect(Collectors.toList()));
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
