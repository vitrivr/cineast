package org.vitrivr.cineast.core.db;

import com.google.common.util.concurrent.ListenableFuture;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.adam.grpc.AdamGrpc.*;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.setup.EntityCreator;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class VideoLookup{
	
	private ADAMproWrapper adampro = new ADAMproWrapper();

	public List<String> lookUpVideoIds(){
		DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();
		selector.open(EntityCreator.CINEAST_MULTIMEDIAOBJECT);
		List<PrimitiveTypeProvider> ids = selector.getAll("id");
		Set<String> uniqueIds = new HashSet();
		for(PrimitiveTypeProvider l: ids){
			uniqueIds.add(l.getString());
		}
		selector.close();

		List<String> multimediaobjectIds = new ArrayList();
		for(String id: uniqueIds){
			multimediaobjectIds.add(id);
		}

		return multimediaobjectIds;
	}
	
	public VideoDescriptor lookUpVideo(String videoId){
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setAttribute("id").setValue(videoId).build();
		//TODO check type as well
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(AdamGrpc.FromMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT).build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).setUseFallback(true).build();
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<QueryResultTupleMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return null;
		}

		QueryResultTupleMessage result = results.get(0);
		
		return new VideoDescriptor(result.getData());
		
	}
	
	public Map<String, VideoDescriptor> lookUpVideos(String... videoIds){
		if(videoIds == null || videoIds.length == 0){
			return new HashMap<>();
		}
		
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		
		StringBuilder builder = new StringBuilder("IN(");
		for(int i = 0; i < videoIds.length - 1; ++i){
			builder.append("'");
			builder.append(videoIds[i]);
			builder.append("', ");
		}
		builder.append("'");
		builder.append(videoIds[videoIds.length - 1]);
		builder.append("')");
		
		WhereMessage where = WhereMessage.newBuilder().setAttribute("id").setValue(builder.toString()).build();
		//TODO check type as well
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(AdamGrpc.FromMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT).build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).setUseFallback(true).build();
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; //TODO
		}
		
		List<QueryResultTupleMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return null; //TODO
		}
		
		HashMap<String, VideoDescriptor> _return = new HashMap<>();
		
		for(QueryResultTupleMessage result : results){
			Map<String, AdamGrpc.DataMessage> meta = result.getData();
			_return.put(meta.get("id").getStringData(), new VideoDescriptor(meta));
		}
		
		return _return;
	}

	public void close() {
		this.adampro.close();
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

public static class VideoDescriptor{
		
		private final String videoId; 
		private final int width, height, framecount;
		private final float seconds, fps;
		private final String name, path;
		
		VideoDescriptor(Map<String, AdamGrpc.DataMessage> map){
			this.videoId = map.get("id").getStringData();
			this.name = map.get("name").getStringData();
			this.path = map.get("path").getStringData();
			this.width = map.get("width").getIntData();
			this.height = map.get("height").getIntData();
			this.framecount = map.get("framecount").getIntData();
			this.seconds = map.get("duration").getFloatData();
			this.fps = this.framecount / this.seconds;
		}

		public String getVideoId() {
			return videoId;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getFramecount() {
			return framecount;
		}
		
		public float getSeconds() {
			return seconds;
		}

		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}
		
		public float getFPS(){
			return fps;
		}

		@Override
		public String toString() {
			return "VideoDescriptor(" + videoId + ")";
		}
	}
}
