package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.standalone.config.Config;

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
        ParsingActionHandler<AnyMessage, MediaObjectMetadataQueryResult> {

  public static final String OBJECT_ID_NAME = ":id";
  public static final String DOMAIN_NAME = ":domain";
  public static final String KEY_NAME = ":key";

  private static Predicate<MediaObjectMetadataDescriptor> createDomainAndKeyFilter(String domain,
                                                                                   String key) {
    return (m) -> m.getKey().toLowerCase().equals(key.toLowerCase()) && m.getDomain().toLowerCase()
        .equals(domain.toLowerCase());
  }

  @Override
  public MediaObjectMetadataQueryResult doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(OBJECT_ID_NAME);
    final String domain = parameters.get(DOMAIN_NAME);
    final String key = parameters.get(KEY_NAME);
    final MediaObjectMetadataReader reader = new MediaObjectMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
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

  @Override
  public String getRoute() {
    return String.format("find/metadata/of/%s/in/%s/with/%s", OBJECT_ID_NAME, DOMAIN_NAME, KEY_NAME);
  }

  @Override
  public String getDescription(RestHttpMethod method) {
    return "Find meta data for specific object id in given domain with given key";
  }

  @Override
  public Class<MediaObjectMetadataQueryResult> outClass() {
    return MediaObjectMetadataQueryResult.class;
  }
}
