package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.cs.dbis.cineast.core.data.Shot;
import ch.unibas.cs.dbis.cineast.core.setup.EntityCreator;
import ch.unibas.dmi.dbis.adam.http.Adam;
import ch.unibas.dmi.dbis.adam.http.Adam.BooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.BooleanQueryMessage.WhereMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.FromMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryResultInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryResultTupleMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryResultsMessage;

public class ShotLookup {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	public void close(){
		this.adampro.close();
	}
	
	public ShotDescriptor lookUpShot(String shotId){
		LOGGER.entry();
		long start = System.currentTimeMillis();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("id").setValue(shotId).build();
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
			return new ShotDescriptor("", "", 0, 0);
		}

		List<QueryResultTupleMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return new ShotDescriptor("", "", 0, 0);
		}

		QueryResultTupleMessage result = results.get(0);
		
		Map<String, Adam.DataMessage> map = result.getData();
		
		ShotDescriptor _return = new ShotDescriptor(map.get("multimediaobject").getStringData(), map.get("id").getStringData(), map.get("segmentstart").getIntData(), map.get("segmentend").getIntData());
		
		LOGGER.debug("lookUpShot done in {}ms", System.currentTimeMillis() - start);
		return LOGGER.exit(_return);
		
	}
	
	public Map<String, ShotDescriptor> lookUpShots(String...ids){
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
		WhereMessage where = WhereMessage.newBuilder().setField("id").setValue(builder.toString()).build();
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
		
		HashMap<String, ShotDescriptor> _return = new HashMap<>();
		for(QueryResultTupleMessage result : results){
			Map<String, Adam.DataMessage> map = result.getData();
			_return.put(map.get("id").getStringData(), new ShotDescriptor(map.get("multimediaobject").getStringData(), map.get("id").getStringData(), map.get("segmentstart").getIntData(), map.get("segmentend").getIntData()));
		}
		LOGGER.debug("lookUpShot done in {}ms", System.currentTimeMillis() - start);
		return LOGGER.exit(_return);
	}
	
	
	public String lookUpVideoid(String name){ //TODO move to VideoLookup
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("name").setValue(name).build();
		//TODO check type as well
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.getDefaultInstance().newBuilder().setFrom(FromMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT).build())
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
	
	public List<ShotDescriptor> lookUpVideo(String videoId){
		LinkedList<ShotDescriptor> _return = new LinkedList<ShotLookup.ShotDescriptor>();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("multimediaobject").setValue(videoId).build();
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
			Map<String, Adam.DataMessage> metadata = result.getData();
			_return.add(new ShotDescriptor(
					videoId,
					metadata.get("id").getStringData(),
					metadata.get("segmentstart").getIntData(),
					metadata.get("segmentend").getIntData()));
		}
		

		
		return _return;
	}
	
//	@Override
//	protected void finalize() throws Throwable {
//		
//		super.finalize();
//	}

	public static class ShotDescriptor{
		
		private final String shotId, videoId;
		private final int startFrame, endFrame;
		
		
		public ShotDescriptor(String videoId, int shotNumber, int startFrame, int endFrame) {
			this(videoId, Shot.generateShotID(videoId, shotNumber), startFrame, endFrame);
		}
		
		ShotDescriptor(String videoId, String shotId,  int startFrame, int endFrame){
			this.videoId = videoId;
			this.shotId = shotId;
			this.startFrame = startFrame;
			this.endFrame = endFrame;
		}

		public String getShotId() {
			return shotId;
		}

		public String getVideoId() {
			return videoId;
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
			return "ShotDescriptor(" + shotId + ")";
		}

	}
	
}
