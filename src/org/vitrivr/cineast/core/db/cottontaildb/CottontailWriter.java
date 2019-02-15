package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BoolVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DoubleVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.FloatVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.IntVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.LongVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Vector;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class CottontailWriter extends AbstractPersistencyWriter<Tuple> {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Data.Builder dataBuilder = Data.newBuilder();
  private static final Tuple.Builder tupleBuilder = Tuple.newBuilder();
  private static final InsertMessage.Builder insertMessageBuilder = InsertMessage.newBuilder();
  private static final Vector.Builder vectorBuilder = Vector.newBuilder();
  private static final FloatVector.Builder floatVectorBuilder = FloatVector.newBuilder();
  private static final DoubleVector.Builder doubleVectorBuilder = DoubleVector.newBuilder();
  private static final IntVector.Builder intVectorBuilder = IntVector.newBuilder();
  private static final LongVector.Builder longVectorBuilder = LongVector.newBuilder();
  private static final BoolVector.Builder boolVectorBuilder = BoolVector.newBuilder();
  private static boolean useGlobalWrapper = true;
  private static final CottontailWrapper GLOBAL_COTTONTAIL_WRAPPER =
      useGlobalWrapper ? new CottontailWrapper() : null;
  private CottontailWrapper cottontail =
      useGlobalWrapper ? GLOBAL_COTTONTAIL_WRAPPER : new CottontailWrapper();

  private Entity entity;

  private static Data toData(Object o) {

    synchronized (dataBuilder) {
      dataBuilder.clear();

      if (o == null) {
        return dataBuilder.setStringData("null").build();
      }

      if (o instanceof Boolean) {
        return dataBuilder.setBooleanData((boolean) o).build();
      }

      if (o instanceof Integer) {
        return dataBuilder.setIntData((int) o).build();
      }

      if (o instanceof Float) {
        return dataBuilder.setFloatData((float) o).build();
      }

      if (o instanceof Double) {
        return dataBuilder.setDoubleData((double) o).build();
      }

      if (o instanceof String) {
        return dataBuilder.setStringData((String) o).build();
      }

      if (o instanceof float[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (floatVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setFloatVector(
                        floatVectorBuilder.addAllVector(Floats.asList((float[]) o))))
                .build();
          }
        }
      }

      if (o instanceof double[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (doubleVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setDoubleVector(
                        doubleVectorBuilder.addAllVector(Doubles.asList((double[]) o))))
                .build();
          }
        }
      }

      if (o instanceof int[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (intVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setIntVector(
                        intVectorBuilder.addAllVector(Ints.asList((int[]) o))))
                .build();
          }
        }
      }

      if (o instanceof long[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (longVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setLongVector(
                        longVectorBuilder.addAllVector(Longs.asList((long[]) o))))
                .build();
          }
        }
      }

      if (o instanceof boolean[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (boolVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setBooleanVector(
                        boolVectorBuilder.addAllVector(Booleans.asList((boolean[]) o))))
                .build();
          }
        }
      }

      return dataBuilder.setStringData(o.toString()).build();
    }
  }

  @Override
  public boolean open(String name) {
    this.entity = CottontailMessageBuilder.entityFromName(CottontailMessageBuilder.CINEAST_SCHEMA, name);
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
        tmpMap.put(names[nameIndex++], toData(o));
      }

      return tupleBuilder.putAllData(tmpMap).build();

    }

  }
}
