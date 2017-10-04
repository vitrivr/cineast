package org.vitrivr.cineast.core.db.protobuf;

import java.util.HashMap;

import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.TupleInsertMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.TupleInsertMessage.Builder;
import org.vitrivr.adampro.grpc.AdamGrpc.IntVectorMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.VectorMessage;
import org.vitrivr.cineast.core.data.FloatArrayIterable;
import org.vitrivr.cineast.core.data.IntArrayIterable;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public abstract class ProtobufTupleGenerator extends AbstractPersistencyWriter<TupleInsertMessage> {

	private static final Builder builder = AdamGrpc.InsertMessage.TupleInsertMessage.newBuilder();
	
	private final DataMessage.Builder insertMessageBuilder = DataMessage.newBuilder();
	
	private DataMessage generateInsertMessage(Object o){
		synchronized(insertMessageBuilder){
			insertMessageBuilder.clear();
			if(o instanceof Long){
				return insertMessageBuilder.setLongData((Long)o).build();
			}
			if(o instanceof Integer){
				return insertMessageBuilder.setIntData((Integer)o).build();
			}
			if(o instanceof Float){
				return insertMessageBuilder.setFloatData((Float)o).build();
			}
			if(o instanceof Double){
				return insertMessageBuilder.setDoubleData((Double)o).build();
			}
			if(o instanceof Boolean){
				return insertMessageBuilder.setBooleanData((Boolean)o).build();
			}
			if(o instanceof String){
				return insertMessageBuilder.setStringData((String)o).build();
			}
			if(o instanceof float[]){
				return insertMessageBuilder.setVectorData(generateVectorMessage(new FloatArrayIterable((float[])o))).build();
			}
			if(o instanceof int[]){
			  return insertMessageBuilder.setVectorData(generateIntVectorMessage(new IntArrayIterable((int[])o))).build();
			}
			if(o instanceof ReadableFloatVector){
				return insertMessageBuilder.setVectorData(generateVectorMessage(ReadableFloatVector.toList((ReadableFloatVector) o))).build();
			}
			if(o == null){
				return insertMessageBuilder.setStringData("null").build();
			}
			return insertMessageBuilder.setStringData(o.toString()).build();
		}
	}
	
	private final VectorMessage.Builder VectorMessageBuilder = VectorMessage.newBuilder();
	private final DenseVectorMessage.Builder denseVectorMessageBuilder = DenseVectorMessage.newBuilder();
	private final IntVectorMessage.Builder intVectorMessageBuilder = IntVectorMessage.newBuilder();
	
	private VectorMessage generateVectorMessage(Iterable<Float> vector){
		synchronized (VectorMessageBuilder) {
			VectorMessageBuilder.clear();
			DenseVectorMessage msg;
			synchronized (denseVectorMessageBuilder) {
				denseVectorMessageBuilder.clear();
				msg = denseVectorMessageBuilder.addAllVector(vector).build();
			}
			return VectorMessageBuilder.setDenseVector(msg).build();
		}
	}
	
	private VectorMessage generateIntVectorMessage(Iterable<Integer> vector){
		synchronized (VectorMessageBuilder) {
			VectorMessageBuilder.clear();
			IntVectorMessage msg;
			synchronized (intVectorMessageBuilder) {
				intVectorMessageBuilder.clear();
				msg = intVectorMessageBuilder.addAllVector(vector).build();
			}
			return VectorMessageBuilder.setIntVector(msg).build();
		}
	}
	
	protected ProtobufTupleGenerator(String...names){
		super(names);
	}
	
	protected ProtobufTupleGenerator(){
		super();
	}

	@Override
  public TupleInsertMessage getPersistentRepresentation(PersistentTuple tuple) {
    synchronized (builder) {
      builder.clear();      
      HashMap<String, DataMessage> tmpMap = new HashMap<>();
      int nameIndex = 0;
      
      for(Object o : tuple.getElements()){
        
        tmpMap.put(names[nameIndex++], generateInsertMessage(o));
        
      }
      return builder.putAllData(tmpMap).build();
    }
  }

}
