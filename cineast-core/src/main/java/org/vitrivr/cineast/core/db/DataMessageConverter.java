package org.vitrivr.cineast.core.db;

import org.vitrivr.adampro.grpc.AdamGrpc.*;
import org.vitrivr.cineast.core.data.FloatArrayIterable;
import org.vitrivr.cineast.core.data.IntArrayIterable;
import org.vitrivr.cineast.core.data.providers.primitive.*;


import java.util.List;

public final class DataMessageConverter {

	private DataMessageConverter(){}
	
	public static final PrimitiveTypeProvider convert(DataMessage message){
		switch(message.getDatatypeCase()){
		case BOOLEANDATA:
			return new BooleanTypeProvider(message.getBooleanData());
		case DOUBLEDATA:
			return new DoubleTypeProvider(message.getDoubleData());
		case VECTORDATA:
			VectorMessage VectorMessage = message.getVectorData();
			switch(VectorMessage.getVectorCase()){
			case DENSEVECTOR:
				return new FloatVectorProvider(convert(VectorMessage.getDenseVector()));
			case INTVECTOR:
				return new IntVectorProvider(convert(VectorMessage.getIntVector()));
			case SPARSEVECTOR:
				return new FloatVectorProvider(convert(VectorMessage.getSparseVector()));
			case VECTOR_NOT_SET:
			default:
				return new NothingProvider();
			}
		case FLOATDATA:
			return new FloatTypeProvider(message.getFloatData());
		case INTDATA:
			return new IntTypeProvider(message.getIntData());
		case LONGDATA:
			return new LongTypeProvider(message.getLongData());
		case STRINGDATA:
			return new StringTypeProvider(message.getStringData());
		case DATATYPE_NOT_SET:
		default:
			return new NothingProvider();
		
		}
	}
	
	private static final DataMessage.Builder builder = DataMessage.newBuilder();
	private static final VectorMessage.Builder vectorBuilder = VectorMessage.newBuilder();
	private static final DenseVectorMessage.Builder denseVectorBuilder = DenseVectorMessage.newBuilder();
	private static final IntVectorMessage.Builder intVectorBuilder = IntVectorMessage.newBuilder();
	
	public static DataMessage convert(PrimitiveTypeProvider provider){
		switch(provider.getType()){
		case BOOLEAN:
			return convert(provider.getBoolean());
		case BYTE:
			return convert(provider.getByte());
		case DOUBLE:
			return convert(provider.getDouble());
		case FLOAT:
			return convert(provider.getFloat());
		case FLOAT_ARRAY:
			return convert(provider.getFloatArray());
		case INT:
			return convert(provider.getInt());
		case INT_ARRAY:
			return convert(provider.getIntArray());
		case LONG:
			return convert(provider.getLong());
		case SHORT:
			return convert(provider.getShort());
		case STRING:
			return convert(provider.getString());
		case UNKNOWN:
		default:
			throw new IllegalArgumentException("Cannot convert ProviderDataType " + provider.getType() + " to DataMessage");
		
		}
	}
	
	

	public static final DataMessage convert(boolean bool){
		synchronized (builder) {
			builder.clear();
			return builder.setBooleanData(bool).build();
		}
	}
	
	public static final DataMessage convert(int i){
		synchronized (builder) {
			builder.clear();
			return builder.setIntData(i).build();
		}
	}
	
	public static DataMessage convert(double d) {
		synchronized (builder) {
			builder.clear();
			return builder.setDoubleData(d).build();
		}
	}
	
	public static DataMessage convert(float f){
		synchronized (builder) {
			builder.clear();
			return builder.setFloatData(f).build();
		}
	}
	
	public static DataMessage convert(long l){
		synchronized (builder) {
			builder.clear();
			return builder.setLongData(l).build();
		}
	}
	
	public static DataMessage convert(String s){
		synchronized (builder){
			builder.clear();
			return builder.setStringData(s).build();
		}
	}
	
	public static VectorMessage convertVectorMessage(float[] vector){
		if(vector == null){
			vector = new float[0];
		}
		DenseVectorMessage dvmg;
		synchronized (denseVectorBuilder) {
			dvmg = denseVectorBuilder.clear().addAllVector(new FloatArrayIterable(vector)).build();
		}
		synchronized (vectorBuilder) {
			vectorBuilder.clear();
			return vectorBuilder.setDenseVector(dvmg).build();
		}
	}
	
	public static DataMessage convert(float[] vector){
		synchronized (builder) {
			builder.clear();
			return builder.setVectorData(convertVectorMessage(vector)).build();
		}
	}
	
	public static VectorMessage convertVectorMessage(int[] vector){
		if(vector == null){
			vector = new int[0];
		}
		IntVectorMessage ivmg;
		synchronized (intVectorBuilder) {
			ivmg = intVectorBuilder.clear().addAllVector(new IntArrayIterable(vector)).build();
		}
		synchronized (vectorBuilder) {
			vectorBuilder.clear();
			return vectorBuilder.setIntVector(ivmg).build();
		}
	}
	
	public static DataMessage convert(int[] vector){
		synchronized (builder) {
			builder.clear();
			return builder.setVectorData(convertVectorMessage(vector)).build();
		}
	}
	
	private static float[] convert(DenseVectorMessage message){
		List<Float> floatlist = message.getVectorList();
		if(floatlist == null || floatlist.isEmpty()){
			return FloatArrayProvider.DEFAULT_FLOAT_ARRAY;
		}
		float[] _return = new float[floatlist.size()];
		int i = 0;
		for(float f : floatlist){
			_return[i++] = f;
		}
		return _return;
	}
	
	private static float[] convert(SparseVectorMessage message) {
		List<Integer> indexList = message.getIndexList();
		if(indexList == null || indexList.isEmpty()){
			return FloatArrayProvider.DEFAULT_FLOAT_ARRAY;
		}
		
		int maxIndex = 0;
		for(int i : indexList){
			maxIndex = maxIndex < i ? i : maxIndex;
		}
		
		float[] _return = new float[maxIndex + 1];
		
		List<Float> valueList = message.getDataList();
		
		for(int i = 0; i < indexList.size(); ++i){
			_return[indexList.get(i)] = valueList.get(i);
		}
		
		return _return;
	}
	
	private static int[] convert(IntVectorMessage message) {
		List<Integer> intList = message.getVectorList();
		if(intList == null || intList.isEmpty()){
			return IntArrayProvider.DEFAULT_INT_ARRAY;
		}
		int[] _return = new int[intList.size()];
		int i = 0;
		for(int j : intList){
			_return[i++] = j;
		}
		return _return;
	}
	
}

