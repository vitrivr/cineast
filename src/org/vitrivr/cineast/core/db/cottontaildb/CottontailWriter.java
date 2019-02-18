package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class CottontailWriter extends AbstractPersistencyWriter<Tuple> {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final Tuple.Builder tupleBuilder = Tuple.newBuilder();
  private static final InsertMessage.Builder insertMessageBuilder = InsertMessage.newBuilder();

  private static boolean useGlobalWrapper = true;
  private static final CottontailWrapper GLOBAL_COTTONTAIL_WRAPPER =
      useGlobalWrapper ? new CottontailWrapper() : null;
  private CottontailWrapper cottontail =
      useGlobalWrapper ? GLOBAL_COTTONTAIL_WRAPPER : new CottontailWrapper();

  private Entity entity;


  @Override
  public boolean open(String name) {
    this.entity = CottontailMessageBuilder.entity(name);
    return true;
  }

  @Override
  public boolean close() {
    if (useGlobalWrapper) {
      return false;
    }
    this.cottontail.close();
    return true;
  }


  @Override
  public boolean exists(String key, String value) {



    return false;
  }


  @Override
  public boolean persist(List<PersistentTuple> tuples) {

    InsertMessage im;
    synchronized (insertMessageBuilder){
      im = insertMessageBuilder.setEntity(this.entity).addAllTuple(tuples.stream().map(this::getPersistentRepresentation).collect(
          Collectors.toList())).build();
    }
    InsertStatus status = this.cottontail.insertBlocking(im);

    return status.getSuccess();
  }

  @Override
  public Tuple getPersistentRepresentation(PersistentTuple tuple) {

    synchronized (tupleBuilder){
      tupleBuilder.clear();

      HashMap<String, Data> tmpMap = new HashMap<>();
      int nameIndex = 0;

      for(Object o : tuple.getElements()){
        tmpMap.put(names[nameIndex++], CottontailMessageBuilder.toData(o));
      }

      return tupleBuilder.putAllData(tmpMap).build();

    }

  }
}
