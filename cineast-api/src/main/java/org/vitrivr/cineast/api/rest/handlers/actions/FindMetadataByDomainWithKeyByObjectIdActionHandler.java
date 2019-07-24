package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;

/**
 * This class handles GET requests with an object id, domain and key and returns all matching
 * metadata descriptors.
 * <p>
 * <h3>GET</h3>
 * This action's resource should have the following structure: {@code
 * find/metadata/of/:id/in/:domain/with/:key}. It returns then all metadata of the object with this
 * id, belonging to that domain with the specified key.
 * </p>
 *
 * @author loris.sauter
 */
public class FindMetadataByDomainWithKeyByObjectIdActionHandler extends
        ParsingActionHandler<AnyMessage> {

  public static final String OBJECT_ID_NAME = ":id";
  public static final String DOMAIN_NAME = ":domain";
  public static final String KEY_NAME = ":key";

  private static Predicate<MediaObjectMetadataDescriptor> createDomainAndKeyFilter(String domain,
                                                                                   String key) {
    return (m) -> m.getKey().toLowerCase().equals(key.toLowerCase()) && m.getDomain().toLowerCase()
        .equals(domain.toLowerCase());
  }

  @Override
  public Object doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(OBJECT_ID_NAME);
    final String domain = parameters.get(DOMAIN_NAME);
    final String key = parameters.get(KEY_NAME);
    final MediaObjectMetadataReader reader = new MediaObjectMetadataReader();
    final List<MediaObjectMetadataDescriptor> descriptors = reader
        .lookupMultimediaMetadata(objectId);
    reader.close();
    return new MediaObjectMetadataQueryResult("",
        descriptors.stream().filter(createDomainAndKeyFilter(domain, key))
            .collect(Collectors.toList()));
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }
}
