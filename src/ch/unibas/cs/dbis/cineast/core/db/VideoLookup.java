package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.cs.dbis.cineast.core.setup.EntityCreator;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage.WhereMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage;

public class VideoLookup{
	
	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	public VideoDescriptor lookUpVideo(String videoId){
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("id").setValue(videoId).build();
		//TODO check type as well
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResponseInfoMessage responce;
		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<QueryResultMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return null;
		}
		
		QueryResultMessage result = results.get(0);
		
		return new VideoDescriptor(result.getMetadata());
		
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
		
		WhereMessage where = WhereMessage.newBuilder().setField("id").setValue(builder.toString()).build();
		//TODO check type as well
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResponseInfoMessage responce;
		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; //TODO
		}
		
		List<QueryResultMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return null; //TODO
		}
		
		HashMap<String, VideoDescriptor> _return = new HashMap<>();
		
		for(QueryResultMessage result : results){
			Map<String, String> meta = result.getMetadata();
			_return.put(meta.get("id"), new VideoDescriptor(meta));
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
		
		VideoDescriptor(Map<String, String> map){
			this.videoId = map.get("id");
			this.name = map.get("name");
			this.path = map.get("path");
			this.width = Integer.parseInt(map.get("width"));
			this.height = Integer.parseInt(map.get("height"));
			this.framecount = Integer.parseInt(map.get("framecount"));
			this.seconds = Float.parseFloat(map.get("duration"));
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
