package org.vitrivr.cineast.core.db.adampro;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import com.google.common.util.concurrent.ListenableFuture;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.*;
import org.vitrivr.cineast.core.data.DefaultValueHashMap;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DataMessageConverter;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class AbstractADAMproSelector implements DBSelector {

  /**
   * MessageBuilder instance used to create the query messages.
   */
  protected final ADAMproMessageBuilder mb = new ADAMproMessageBuilder();

  /**
   * Name of the entity the current instance of ADAMproSelector uses.
   */
  protected String entityName;

  /**
   * FromMessaged used by the instance of ADAMproSelector.
   */
  protected FromMessage fromMessage;

  final ADAMproWrapper adampro;

  public AbstractADAMproSelector(ADAMproWrapper wrapper){
    this.adampro = wrapper;
  }

  @Override
  public boolean open(String name) {
    this.entityName = name;
    this.fromMessage = this.mb.buildFromMessage(name);
    return true;
  }

  @Override
  public boolean close() {

    //this.adampro.close();
    return true;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
    return getRows(fieldName, RelationalOperator.EQ, values);
  }

  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    return preview(Integer.MAX_VALUE);
  }

  @Override
  public boolean existsEntity(String eName) {
    ListenableFuture<ExistsMessage> future = this.adampro.existsEntity(eName);
    try {
      return future.get().getExists();
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
  }

  public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
    PreviewMessage msg = PreviewMessage.newBuilder().setEntity(this.entityName).setN(k)
        .build();
    ListenableFuture<QueryResultsMessage> f = this.adampro.previewEntity(msg);
    QueryResultsMessage result;
    try {
      result = f.get();
    } catch (InterruptedException | ExecutionException e) {
      return new ArrayList<>(0);
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important

    List<QueryResultTupleMessage> resultList = response.getResultsList();
    return resultsToMap(resultList);
  }

  protected <T extends DistanceElement> List<T> handleNearestNeighbourResponse(QueryResultInfoMessage response, int k, Class<? extends T> distanceElementClass) {
    List<T> result = new ArrayList<>(k);
    for (QueryResultTupleMessage msg : response.getResultsList()) {
      String id = msg.getDataMap().get(GENERIC_ID_COLUMN_QUALIFIER).getStringData();
      if (id == null) {
        continue;
      }
      double distance = msg.getDataMap().get("ap_distance").getDoubleData();
      T e = DistanceElement.create(distanceElementClass, id, distance);
      result.add(e);
    }

    return result;
  }

  /**
   * SELECT label FROM ... Be careful with the size of the resulting List :)
   */
  @Override
  public List<PrimitiveTypeProvider> getAll(String label) {
    List<Map<String, PrimitiveTypeProvider>> resultList = getAll();
    return resultList.stream().map(row -> row.get(label)).collect(Collectors.toList());
  }

  /**
   * @param resultList can be empty
   * @return an ArrayList of length one if the resultList is empty, else the transformed QueryResultTupleMessage
   */
  protected List<Map<String, PrimitiveTypeProvider>> resultsToMap(
      List<QueryResultTupleMessage> resultList) {
    if (resultList.isEmpty()) {
      return new ArrayList<>(0);
    }

    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(resultList.size());

    for (QueryResultTupleMessage resultMessage : resultList) {
      Map<String, DataMessage> data = resultMessage.getDataMap();
      Set<String> keys = data.keySet();
      DefaultValueHashMap<String, PrimitiveTypeProvider> map = new DefaultValueHashMap<>(
          NothingProvider.INSTANCE);
      for (String key : keys) {
        map.put(key, DataMessageConverter.convert(data.get(key)));
      }
      _return.add(map);
    }

    return _return;
  }

  @Override
  public boolean ping() {
    return adampro.pingBlocking().getCode() == Code.OK;
  }
}
