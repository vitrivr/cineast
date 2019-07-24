package org.vitrivr.cineast.core.db.adampro;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.*;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.TupleInsertMessage;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.util.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ADAMproWriter extends ProtobufTupleGenerator {

  /**
   * flag to choose if every selector should have its own connection to ADAMpro or if they should share one
   */
  private static boolean useGlobalWrapper = true;

  private static final ADAMproWrapper GLOBAL_ADAMPRO_WRAPPER = useGlobalWrapper
      ? new ADAMproWrapper() : null;

  private ADAMproWrapper adampro = useGlobalWrapper ? GLOBAL_ADAMPRO_WRAPPER : new ADAMproWrapper();

  private static final Logger LOGGER = LogManager.getLogger();

  private String entityName;
  private final InsertMessage.Builder imBuilder = InsertMessage.newBuilder();
  private QueryMessage.Builder qmBuilder;
  private final WhereMessage.Builder wmBuilder = WhereMessage.newBuilder();
  private FromMessage from;

  @Override
  public boolean open(String name) {
    this.entityName = name;
    this.from = AdamGrpc.FromMessage.newBuilder().setEntity(this.entityName).build();
    this.qmBuilder = QueryMessage.newBuilder().setFrom(this.from);
    return true;
  }

  @Override
  public boolean close() {
    if (useGlobalWrapper) {
      return false;
    }
    this.adampro.close();
    return true;
  }

  @Override
  public boolean exists(String key, String value) {
    WhereMessage where;
    synchronized (this.wmBuilder) {
      where = this.wmBuilder.clear().setAttribute(key)
          .addValues(AdamGrpc.DataMessage.newBuilder().setStringData(value)).build();
    }

    ArrayList<WhereMessage> tmp = new ArrayList<>(1);
    tmp.add(where);
    QueryMessage qbqm;
    synchronized (this.qmBuilder) {
      qbqm = this.qmBuilder.clear().setFrom(this.from).setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp))
          .build();
    }
    ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
    QueryResultInfoMessage responce;
    try {
      QueryResultsMessage qRMessage = f.get();
      if (!qRMessage.hasAck()) {
        LOGGER.error("error in {}.exists, no acc in QueryResultsMessage", entityName);
        return false;
      }
      AckMessage ack = qRMessage.getAck();
      if (ack.getCode() != AckMessage.Code.OK) {
        LOGGER.error("error in {}.exists: {}", entityName, ack.getMessage());
        return false;
      }
      if (qRMessage.getResponsesCount() == 0) {
        LOGGER.error("error in {}.exists, no QueryResultInfoMessage in QueryResultsMessage", entityName);
        return false;
      }
      responce = qRMessage.getResponses(0);
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in {}.exists: {}", entityName, LogHelper.getStackTrace(e));
      return false;
    }

    return responce.getResultsCount() > 0;

  }

  @Override
  public boolean persist(List<PersistentTuple> tuples) {
    InsertMessage im;
    synchronized (this.imBuilder) {
      this.imBuilder.clear();
      this.imBuilder.setEntity(this.entityName);
      ArrayList<TupleInsertMessage> tmp = new ArrayList<>(tuples.size());
      for (PersistentTuple tuple : tuples) {
        TupleInsertMessage tim = getPersistentRepresentation(tuple);
        tmp.add(tim);
      }
      this.imBuilder.addAllTuples(tmp);
      im = this.imBuilder.build();
    }
    LOGGER.debug("Inserting {} elements into {} with serialized size {}", tuples.size(), this.entityName, im.getSerializedSize());
    ListenableFuture<AckMessage> future = this.adampro.insert(im);
    AckMessage ack;
    try {
      ack = future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in {}.persist: {}", entityName, LogHelper.getStackTrace(e));
      return false;
    }
    if (ack.getCode() != Code.OK) {
      LOGGER.warn("Error: {} during persist in entity {}", ack.getMessage(), entityName);
      return false;
    }
    return true;
  }

  @Override
  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

}
