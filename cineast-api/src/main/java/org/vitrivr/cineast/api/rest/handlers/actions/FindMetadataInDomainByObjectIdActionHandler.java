package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataByDomainGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataByDomainPostHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * Finds metadata of a given object id list (REST) / object id (Web) and returns only items in a certain domain.
 *
 * <p>
 * <h3>GET</h3>
 * The action should contain an id and a domain, e.g. {@code /metadata/in/:domain/by/id/:id}. The response is JSON
 * encoded and basically identical to a response from {@link FindMetadataByObjectIdActionHandler}: A list of {@link
 * MediaObjectMetadataDescriptor}s with only entries of the specified domain.
 * </p>
 * <p>
 * <h3>POST</h3>
 * The action should contain a domain, e.g. {@code /metadata/in/:domain}. The post body is an {@link IdList} and the
 * response contains metadata for each id in that list, belonging to the specified domain. The response is JSON encoded
 * and basically identical to a response from {@link FindMetadataByObjectIdActionHandler}:
 * A list of {@link
 * MediaObjectMetadataDescriptor}s with only entries of the specified domain.
 * </p>
 *
 * @author loris.sauter
 * @deprecated See {@link FindObjectMetadataByDomainGetHandler} and {@link FindObjectMetadataByDomainPostHandler}
 */
@Deprecated
public class FindMetadataInDomainByObjectIdActionHandler extends ParsingActionHandler<IdList, MediaObjectMetadataQueryResult> {
  
  private static final String ATTRIBUTE_ID = "id";
  private static final String DOMAIN_NAME = "domain";
  
  @Override
  public List<RestHttpMethod> supportedMethods() {
    return Arrays.asList(RestHttpMethod.GET, RestHttpMethod.POST);
  }
  
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
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("",
        service.findByDomain(objectId, domain));
  }
  
  /**
   * Processes a HTTP POST request.
   *
   * @param context    Object that is handed to the invocation, usually parsed from the request body. May be NULL!
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MediaObjectMetadataQueryResult}
   */
  @Override
  public MediaObjectMetadataQueryResult doPost(IdList context, Map<String, String> parameters) {
    if (context == null || context.getIds().length == 0) {
      return new MediaObjectMetadataQueryResult("", new ArrayList<>(0));
    }
    final String domain = parameters.get(DOMAIN_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("",
        service.findByDomain(context.getIdList(), domain));
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
  
  @Override
  public String getRoute() {
    return String.format("find/metadata/in/:%s/by/id/:%s", DOMAIN_NAME, ATTRIBUTE_ID);
  }
  
  @Override
  public String routeForPost() {
    return String.format("find/metadata/in/:%s", DOMAIN_NAME);
  }
  
  @Override
  public String getDescription(RestHttpMethod method) {
    return "Find meta data in domain by object id";
  }
  
  @Override
  public Class<MediaObjectMetadataQueryResult> outClass() {
    return MediaObjectMetadataQueryResult.class;
  }
}
