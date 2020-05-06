package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.rest.services.MetadataRetrievalService;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class FindMetadataByKeyByObjectIdActionHandler extends ParsingActionHandler<IdList, MediaObjectMetadataQueryResult> {
  private static final String ATTRIBUTE_ID = "id";
  private static final String KEY_NAME = "key";

  /**
   * Processes a HTTP GET request.
   *
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MediaObjectMetadataQueryResult}
   */
  @Override
  public MediaObjectMetadataQueryResult doGet(Map<String, String> parameters) {
    final String objectId = parameters.get(ATTRIBUTE_ID);
    final String key = parameters.get(KEY_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("", service.findByKey(objectId, key));
  }
  
  /**
   * Processes a HTTP POST request.
   *
   * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
   * @param parameters Map containing named parameters in the URL.
   * @return {@link MediaObjectMetadataQueryResult}
   */
  @Override
  public MediaObjectMetadataQueryResult doPost(IdList context, Map<String, String> parameters) {
    if(context == null || context.getIds().length == 0 ){
      return new MediaObjectMetadataQueryResult("", new ArrayList<>(0) );
    }
    final String key = parameters.get(KEY_NAME);
    final MetadataRetrievalService service = new MetadataRetrievalService();
    return new MediaObjectMetadataQueryResult("",service.findByKey(context.getIdList(), key));
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
  public String routeForPost() {
    return String.format("find/metadata/with/:%s", KEY_NAME);
  }
  
  @Override
  public String getRoute() {
    return String.format("find/metadata/with/:%s/by/id/:%s", KEY_NAME, ATTRIBUTE_ID);
  }
  
  @Override
  public String getDescription(RestHttpMethod method) {
    return "Find meta data for a given object id with specified key";
  }

  @Override
  public Class<MediaObjectMetadataQueryResult> outClass() {
    return MediaObjectMetadataQueryResult.class;
  }

  @Override
  public List<RestHttpMethod> supportedMethods() {
    return Arrays.asList(RestHttpMethod.GET, RestHttpMethod.POST);
  }
}
