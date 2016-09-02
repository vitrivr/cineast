package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adam.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultInfoMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultTupleMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Shot;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.setup.EntityCreator;

import com.google.common.util.concurrent.ListenableFuture;

public class ShotLookup {//TODO rename to SegmentLookup

	private static final Logger LOGGER = LogManager.getLogger();
	@Deprecated
	private ADAMproWrapper adampro = new ADAMproWrapper(); //FIXME use abstaction layer!
	private final DBSelector selector;
	
	public ShotLookup(){
		this.selector = Config.getDatabaseConfig().getSelectorSupplier().get();
		this.selector.open(EntityCreator.CINEAST_SEGMENT);
	}
	
	public void close(){
		this.adampro.close();
		this.selector.close();
	}
	
	public SegmentDescriptor lookUpShot(String segmentId){
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows("id", segmentId);
		
		if(results.isEmpty()){
			return new SegmentDescriptor();
		}
		
		Map<String, PrimitiveTypeProvider> map = results.get(0);
		
		PrimitiveTypeProvider idProvider = map.get("id");
		PrimitiveTypeProvider mmobjidProvider = map.get("multimediaobject");
		PrimitiveTypeProvider startProvider = map.get("segmentstart");
		PrimitiveTypeProvider endProvider = map.get("segmentend");
		
		if(idProvider == null){
			LOGGER.error("no id in segment");
			return new SegmentDescriptor();
		}
		
		if(idProvider.getType() != ProviderDataType.STRING){
			LOGGER.error("invalid data type for field id in segment, expected string, got {}", idProvider.getType());
		}
		
		if(mmobjidProvider == null){
			LOGGER.error("no multimediaobject in segment");
			return new SegmentDescriptor();
		}
		
		if(mmobjidProvider.getType() != ProviderDataType.STRING){
			LOGGER.error("invalid data type for field multimediaobject in segment, expected string, got {}", mmobjidProvider.getType());
		}
		
		if(startProvider == null){
			LOGGER.error("no segmentstart in segment");
			return new SegmentDescriptor();
		}
		
		if(startProvider.getType() != ProviderDataType.INT){
			LOGGER.error("invalid data type for field segmentstart in segment, expected int, got {}", startProvider.getType());
		}
		
		if(endProvider == null){
			LOGGER.error("no segmentend in segment");
			return new SegmentDescriptor();
		}
		
		if(endProvider.getType() != ProviderDataType.INT){
			LOGGER.error("invalid data type for field segmentend in segment, expected int, got {}", endProvider.getType());
		}
		
		return new SegmentDescriptor(mmobjidProvider.getString(), idProvider.getString(), startProvider.getInt(), endProvider.getInt());
		
	}
	
	public Map<String, SegmentDescriptor> lookUpShots(String...ids){
		LOGGER.entry();
		
		if(ids == null || ids.length == 0){
			return new HashMap<>();
		}
		
		long start = System.currentTimeMillis();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		StringBuilder builder = new StringBuilder("IN(");
		for(int i = 0; i < ids.length - 1; ++i){
			builder.append("'");
			builder.append(ids[i]);
			builder.append("', ");
		}
		builder.append("'");
		builder.append(ids[ids.length - 1]);
		builder.append("')");
		WhereMessage where = WhereMessage.newBuilder().setAttribute("id").setValue(builder.toString()).build();
		//TODO check type as well
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(FromMessage.newBuilder().setEntity(EntityCreator.CINEAST_SEGMENT).build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResultsMessage> f = adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<>();
		}

		List<QueryResultTupleMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return new HashMap<>();
		}
		
		HashMap<String, SegmentDescriptor> _return = new HashMap<>();
		for(QueryResultTupleMessage result : results){
			Map<String, AdamGrpc.DataMessage> map = result.getData();
			_return.put(map.get("id").getStringData(), new SegmentDescriptor(map.get("multimediaobject").getStringData(), map.get("id").getStringData(), map.get("segmentstart").getIntData(), map.get("segmentend").getIntData()));
		}
		LOGGER.debug("lookUpShot done in {}ms", System.currentTimeMillis() - start);
		return LOGGER.exit(_return);
	}
	
	
	public String lookUpVideoid(String name){ //TODO move to VideoLookup
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setAttribute("name").setValue(name).build();
		//TODO check type as well
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(FromMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT).build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResultsMessage> f = adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

		List<QueryResultTupleMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return "";
		}

		QueryResultTupleMessage result = results.get(0);
		
		String id = result.getData().get("id").getStringData();
		
		return id;

	}
	
	public List<SegmentDescriptor> lookUpVideo(String videoId){
		LinkedList<SegmentDescriptor> _return = new LinkedList<ShotLookup.SegmentDescriptor>();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setAttribute("multimediaobject").setValue(videoId).build();
		//TODO check type as well
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(FromMessage.newBuilder().setEntity(EntityCreator.CINEAST_SEGMENT).build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResultsMessage> f = adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return _return;
		}
		
		List<QueryResultTupleMessage> results = responce.getResultsList();
		
		for(QueryResultTupleMessage result : results){
			Map<String, AdamGrpc.DataMessage> metadata = result.getData();
			_return.add(new SegmentDescriptor(
					videoId,
					metadata.get("id").getStringData(),
					metadata.get("segmentstart").getIntData(),
					metadata.get("segmentend").getIntData()));
		}
		

		
		return _return;
	}
	

	public static class SegmentDescriptor{
		
		private final String segmentId, mmobjId;
		private final int startFrame, endFrame;
		
		
		public SegmentDescriptor(String videoId, int segmentNumber, int startFrame, int endFrame) {
			this(videoId, Shot.generateShotID(videoId, segmentNumber), startFrame, endFrame);
		}
		
		SegmentDescriptor(String multimediaObjectId, String segmentId,  int startFrame, int endFrame){
			this.mmobjId = multimediaObjectId;
			this.segmentId = segmentId;
			this.startFrame = startFrame;
			this.endFrame = endFrame;
		}

		public SegmentDescriptor() {
			this("", "", 0, 0);
		}

		public String getShotId() {
			return segmentId;
		}

		public String getVideoId() {
			return mmobjId;
		}



		public int getFramecount() {
			return endFrame - startFrame + 1;
		}

		public int getStartFrame() {
			return startFrame;
		}

		public int getEndFrame() {
			return endFrame;
		}

		@Override
		public String toString() {
			return "ShotDescriptor(" + segmentId + ")";
		}

	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
}
