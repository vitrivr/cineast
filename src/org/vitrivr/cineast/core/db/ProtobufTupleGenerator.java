package org.vitrivr.cineast.core.db;

import java.util.HashMap;

import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.adam.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adam.grpc.AdamGrpc.FeatureVectorMessage;
import org.vitrivr.adam.grpc.AdamGrpc.InsertMessage.TupleInsertMessage;
import org.vitrivr.adam.grpc.AdamGrpc.InsertMessage.TupleInsertMessage.Builder;
import org.vitrivr.adam.grpc.AdamGrpc.IntVectorMessage;
import org.vitrivr.cineast.core.data.FloatArrayIterable;
import org.vitrivr.cineast.core.data.ReadableFloatVector;

public abstract class ProtobufTupleGenerator implements PersistencyWriter<TupleInsertMessage> {

	protected String[] names; 
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
				return insertMessageBuilder.setFeatureData(generateFeatureVectorMessage(new FloatArrayIterable((float[])o))).build();
			}
			if(o instanceof ReadableFloatVector){
				return insertMessageBuilder.setFeatureData(generateFeatureVectorMessage(((ReadableFloatVector)o).toList(null))).build();
			}
			if(o == null){
				return insertMessageBuilder.setStringData("null").build();
			}
			return insertMessageBuilder.setStringData(o.toString()).build();
		}
	}
	
	private final FeatureVectorMessage.Builder featureVectorMessageBuilder = FeatureVectorMessage.newBuilder();
	private final DenseVectorMessage.Builder denseVectorMessageBuilder = DenseVectorMessage.newBuilder();
	private final IntVectorMessage.Builder intVectorMessageBuilder = IntVectorMessage.newBuilder();
	
	private FeatureVectorMessage generateFeatureVectorMessage(Iterable<Float> vector){
		synchronized (featureVectorMessageBuilder) {
			featureVectorMessageBuilder.clear();
			DenseVectorMessage msg;
			synchronized (denseVectorMessageBuilder) {
				denseVectorMessageBuilder.clear();
				msg = denseVectorMessageBuilder.addAllVector(vector).build();
			}
			return featureVectorMessageBuilder.setDenseVector(msg).build();
		}
	}
	
	private FeatureVectorMessage generateIntFeatureVectorMessage(Iterable<Integer> vector){
		synchronized (featureVectorMessageBuilder) {
			featureVectorMessageBuilder.clear();
			IntVectorMessage msg;
			synchronized (intVectorMessageBuilder) {
				intVectorMessageBuilder.clear();
				msg = intVectorMessageBuilder.addAllVector(vector).build();
			}
			return featureVectorMessageBuilder.setIntVector(msg).build();
		}
	}
	
	protected ProtobufTupleGenerator(String...names){
		this.names = names;
	}
	
	protected ProtobufTupleGenerator(){
		this("id", "feature");
	}

	@Override
	public PersistentTuple<TupleInsertMessage> generateTuple(Object... objects) {
		return new ProtobufTuple(objects);
	}

	public void setFieldNames(String...names){
		if(names != null && names.length > 0){
			this.names = names;
		}
	}
	
	class ProtobufTuple extends PersistentTuple<TupleInsertMessage>{

		ProtobufTuple(Object...objects){
			super(objects);
		}
		
		@Override
		public TupleInsertMessage getPersistentRepresentation() {
			synchronized (builder) {
				builder.clear();			
				HashMap<String, DataMessage> tmpMap = new HashMap<>();
				int nameIndex = 0;
				
				for(Object o : this.elements){
					
					tmpMap.put(names[nameIndex++], generateInsertMessage(o));
					
				}
				return builder.putAllData(tmpMap).build();
			}
		}
		
		
		
	}
	
}
