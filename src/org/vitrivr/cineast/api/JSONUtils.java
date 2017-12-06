package org.vitrivr.cineast.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.*;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.query.containers.ImageQueryContainer;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.web.ImageParser;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import georegression.struct.point.Point2D_F32;
import gnu.trove.map.hash.TObjectDoubleHashMap;
@Deprecated
public class JSONUtils {

	private static Logger LOGGER = LogManager.getLogger();
	private JSONUtils(){}
	
	public static Pair<ImageQueryContainer, TObjectDoubleHashMap<String>> readQueryFromJSON(Reader reader){
		LOGGER.traceEntry();
		try {
			JsonObject jobj_in = JsonObject.readFrom(reader);
			ImageQueryContainer qc = queryContainerFromJSON(jobj_in);
			
			String weights = jobj_in.get("weights").toString();
			
			TObjectDoubleHashMap<String> weightMap = getWeightsFromJsonString(weights);
			
			return LOGGER.traceExit(new Pair<ImageQueryContainer, TObjectDoubleHashMap<String>>(qc, weightMap));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return null;
		}
	}
	
	public static ImageQueryContainer queryContainerFromJSON(JsonObject jobj){//TODO improve robustness against wrong data types
		BufferedImage img = jobj.get("img") == null ? null : ImageParser.dataURLtoBufferedImage(jobj.get("img").asString());
		ImageQueryContainer qc = img == null ? new ImageQueryContainer((MultiImage)null) : new ImageQueryContainer(MultiImageFactory.newInMemoryMultiImage(img));
		if(jobj.get("subelements") != null){
			JsonArray subs = jobj.get("subelements").asArray();
			for(JsonValue jv : subs){
				qc.getSubtitleItems().add(new QuerySubTitleItem(jv.asString()));
			}
		}
		if(jobj.get("motion") != null){
			JsonArray motion = jobj.get("motion").asArray();
			for(JsonValue motionPath : motion){
				LinkedList<Point2D_F32> pathList = new LinkedList<Point2D_F32>();
				for(JsonValue point : motionPath.asArray()){
					JsonArray pa = point.asArray();
					pathList.add(new Point2D_F32(pa.get(0).asFloat(), pa.get(1).asFloat()));
				}
				qc.addPath(pathList);
			}
		}
		if(jobj.get("motionbackground") != null){
			JsonArray motion = jobj.get("motionbackground").asArray();
			for(JsonValue motionPath : motion){
				LinkedList<Point2D_F32> pathList = new LinkedList<Point2D_F32>();
				for(JsonValue point : motionPath.asArray()){
					JsonArray pa = point.asArray();
					pathList.add(new Point2D_F32(pa.get(0).asFloat(), pa.get(1).asFloat()));
				}
				qc.addBgPath(pathList);
			}
		}
		
//		if(jobj.get("tags") != null){
//			JsonObject tags = jobj.get("tags").asObject();
//			JsonArray concepts = tags.get("concepts").asArray();
//			for(JsonValue concept : concepts){
//				qc.addTag(concept.asString());
//			}
//		}
		
		
		qc.setRelativeStart(jobj.get("start") == null ? 0 : jobj.get("start").asFloat());
		qc.setRelativeEnd(jobj.get("end") == null ? 0 : jobj.get("end").asFloat());
		
		if(jobj.get("weight") != null){
			qc.setWeight(jobj.get("weight").asFloat());
		}
		
		if(jobj.get("id") != null){
			qc.setId(jobj.get("id").asString());
		}
		
		return qc;
	}
	
	public static String queryContainerToJSON(ImageQueryContainer qc){
		JsonObject jobj = new JsonObject();
		jobj.add("img", ImageParser.BufferedImageToDataURL(qc.getMostRepresentativeFrame().getImage().getBufferedImage(), "PNG"));
		
		JsonArray paths = new JsonArray();
		for(Pair<Integer, LinkedList<Point2D_F32>> pair : qc.getPaths()){
			LinkedList<Point2D_F32> motionPath = pair.second;
			JsonArray arr = new JsonArray();
			for(Point2D_F32 point : motionPath){
				JsonArray jpoint = new JsonArray();
				jpoint.add(point.x);
				jpoint.add(point.y);
				arr.add(jpoint);
			}
			paths.add(arr);
		}
		jobj.add("motion", paths);
		
		JsonArray bgpaths = new JsonArray();
		for(Pair<Integer, LinkedList<Point2D_F32>> pair : qc.getBgPaths()){
			LinkedList<Point2D_F32> motionPath = pair.second;
			JsonArray arr = new JsonArray();
			for(Point2D_F32 point : motionPath){
				JsonArray jpoint = new JsonArray();
				jpoint.add(point.x);
				jpoint.add(point.y);
				arr.add(jpoint);
			}
			paths.add(arr);
		}
		jobj.add("motionbackground", bgpaths);
				
		JsonArray subs = new JsonArray();
		for(SubtitleItem sub : qc.getSubtitleItems()){
			subs.add(sub.getText());
		}
		jobj.add("subelements", subs);
		
		jobj.add("start", qc.getRelativeStart());
		jobj.add("end", qc.getRelativeEnd());
		
		
		return jobj.toString();
	}

	
	public static TObjectDoubleHashMap<String> getWeightsFromJsonString(String s){
		TObjectDoubleHashMap<String> weightMap = new TObjectDoubleHashMap<String>();
		JsonObject weights = JsonObject.readFrom(s);
		weightMap.put("global", weights.get("global").asDouble());
		weightMap.put("local", weights.get("local").asDouble());
		weightMap.put("edge", weights.get("edge").asDouble());
		weightMap.put("text", weights.get("text").asDouble());
		weightMap.put("motion", weights.get("motion").asDouble());
		weightMap.put("motionbackground", weights.get("motionbackground").asDouble());
		weightMap.put("complex", weights.get("complex").asDouble());
		return weightMap;
	}

	
	/**
	 * Sends Results to the client as specified in the JSON-Doc
	 * @param printer Output goes here
	 * @param resultlist Pair of shots and scores
	 * @param category which category has been used to generate the results
	 * @param index index of the query (used for multisketch)
	 */
	public static void printResultsBatched(PrintStream printer, List<StringDoublePair> resultlist, String category, int index) {
		printer.print(JSONEncoder.encodeResultBatched(resultlist, category, index).toString());
		printer.println(',');
	}
	
	public static HashSet<String> printShotsBatched(PrintStream printer, List<StringDoublePair> resultlist, HashSet<String> shotids) {
		ArrayList<SegmentDescriptor> sdList = new ArrayList<>(resultlist.size());
		SegmentLookup sl = new SegmentLookup();
		
		String[] ids = new String[resultlist.size()];
		int i = 0;
		for(StringDoublePair sdp : resultlist){
			ids[i++] = sdp.key;
		}
		
		Map<String, SegmentDescriptor> map = sl.lookUpSegments(Arrays.asList(ids));
		
		for(String id : ids){
			SegmentDescriptor sd = map.get(id);
			if(sd != null){
				sdList.add(sd);
			}
		}
		
//		for(int i = 0; i < resultlist.size(); ++i){
//			
//			String shotid = resultlist.get(i).key;
//			if(shotids.contains(shotid)){
//				continue;
//			}
//			shotids.add(shotid);
//			ShotDescriptor descriptor = sl.lookUpShot(shotid);
//
//			sdList.add(descriptor);
//			
//		}
		printer.print(JSONEncoder.encodeShotBatch(sdList).toString());
		printer.println(',');
		sl.close();
		return shotids;
	}
	
	public static HashSet<String> printVideosBatched(PrintStream printer, List<StringDoublePair> resultlist, HashSet<String> videoids) {
		SegmentLookup sl = new SegmentLookup();
		MultimediaObjectLookup vl = new MultimediaObjectLookup();
		
		String[] ids = new String[resultlist.size()];
		int i = 0;
		for(StringDoublePair sdp : resultlist){
			ids[i++] = sdp.key;
		}
		
		Map<String, SegmentDescriptor> map = sl.lookUpSegments(Arrays.asList(ids));
		
		HashSet<String> videoIds = new HashSet<>();
		for(String id : ids){
		  SegmentDescriptor sd = map.get(id);
		  if(sd == null){
		    continue;
		  }
			videoIds.add(sd.getObjectId());
		}
		
		String[] vids = new String[videoIds.size()];
		i = 0;
		for(String vid : videoIds){
			vids[i++] = vid;
		}
		
		ArrayList<MultimediaObjectDescriptor> vdList = new ArrayList<>(vids.length);
		
		Map<String, MultimediaObjectDescriptor> vmap = vl.lookUpObjects(vids);
		
		for(String vid : vids){
			vdList.add(vmap.get(vid));
		}
		
//		for(int i = 0; i < resultlist.size(); ++i){
//			String shotid = resultlist.get(i).key;
//			ShotDescriptor descriptor = sl.lookUpShot(shotid);
//			
//			if(videoids.contains(descriptor.getObjectId())){
//				continue;
//			}
//			videoids.add(descriptor.getObjectId());
//			
//			vdList.add(vl.lookUpVideo(descriptor.getObjectId()));
//			
//		}
		
		printer.print(JSONEncoder.encodeVideoBatch(vdList).toString());
		printer.println(',');
		sl.close();
		vl.close();
		return videoids;
	}
	
	public static String formatResultName(String name){
		return JSONEncoder.encodeResultName(name).toString();
	}
	
	public static void printResultName(PrintStream printer, String name){
		printer.print(formatResultName(name));
		printer.println(',');
	}
}
