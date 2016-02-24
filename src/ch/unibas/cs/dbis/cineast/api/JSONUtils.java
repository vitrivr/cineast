package ch.unibas.cs.dbis.cineast.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiIdTriplet;
import ch.unibas.cs.dbis.cineast.core.data.MultiImageFactory;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.data.QuerySubTitleItem;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.SubtitleItem;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import georegression.struct.point.Point2D_F32;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

public class JSONUtils {

	private static Logger LOGGER = LogManager.getLogger();
	private JSONUtils(){}
	
	public static Pair<QueryContainer, TObjectDoubleHashMap<String>> readQueryFromJSON(Reader reader){
		LOGGER.entry();
		try {
			JsonObject jobj_in = JsonObject.readFrom(reader);
			QueryContainer qc = queryContainerFromJSON(jobj_in);
			
			String weights = jobj_in.get("weights").toString();
			
			TObjectDoubleHashMap<String> weightMap = getWeightsFromJsonString(weights);
			
			return LOGGER.exit(new Pair<QueryContainer, TObjectDoubleHashMap<String>>(qc, weightMap));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return null;
		}
	}
	
	public static QueryContainer queryContainerFromJSON(JsonObject jobj){
		BufferedImage img = jobj.get("img") == null ? null : WebUtils.dataURLtoBufferedImage(jobj.get("img").asString());
		QueryContainer qc = new QueryContainer(MultiImageFactory.newInMemoryMultiImage(img));
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
				
		qc.setRelativeStart(jobj.get("start") == null ? 0 : jobj.get("start").asFloat());
		qc.setRelativeEnd(jobj.get("end") == null ? 0 : jobj.get("end").asFloat());
		
		return qc;
	}
	
	public static String queryContainerToJSON(QueryContainer qc){
		JsonObject jobj = new JsonObject();
		jobj.add("img", WebUtils.BufferedImageToDataURL(qc.getMostRepresentativeFrame().getImage().getBufferedImage(), "PNG"));
		
		JsonArray paths = new JsonArray();
		for(LinkedList<Point2D_F32> motionPath : qc.getPaths()){
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
		
		JsonArray subs = new JsonArray();
		for(SubtitleItem sub : qc.getSubtitleItems()){
			subs.add(sub.getText());
		}
		jobj.add("subelements", subs);
		
		jobj.add("start", qc.getRelativeStart());
		jobj.add("end", qc.getRelativeEnd());
		
		
		return jobj.toString();
	}
	
	public static JsonArray resultToJSONArray(List<LongDoublePair> result, ShotLookup sl){
		JsonArray jarr = new JsonArray();
		for(LongDoublePair p : result){
			long shotId = p.key;
			ShotLookup.ShotDescriptor descriptor = sl.lookUpShot(shotId);
			JsonObject jobj = new JsonObject();
			jobj.add("score", p.value).add("name", descriptor.getName());
			jobj.add("startframe", descriptor.getStartFrame()).add("endframe", descriptor.getEndFrame());
			jobj.add("shotid", shotId);
			jobj.add("videoid", descriptor.getVideoId());
			jobj.add("framerate", descriptor.getFPS());
			jobj.add("path", descriptor.getPath());
			jarr.add(jobj);
		}
		return jarr;
	}
	
	
	@Deprecated
	public static JsonArray getShotList(HashMap<String, List<LongDoublePair>> result, ShotLookup sl){
		HashSet<Long> ids = new HashSet<Long>();
		Set<String> categories = result.keySet();
		for(String category : categories){
			List<LongDoublePair> list = result.get(category);
			for(LongDoublePair ldp : list){
				ids.add(ldp.key);
			}
		}
		ArrayList<Long> idList = new ArrayList<Long>(ids.size());
		idList.addAll(ids);
		Collections.sort(idList);
		JsonArray jarr = new JsonArray();
		for(long id : idList){
			ShotLookup.ShotDescriptor descriptor = sl.lookUpShot(id);
			JsonObject jobj = new JsonObject();
			jobj.add("name", descriptor.getName());
			jobj.add("startframe", descriptor.getStartFrame()).add("endframe", descriptor.getEndFrame());
			jobj.add("shotid", id);
			jobj.add("videoid", descriptor.getVideoId());
			jobj.add("framerate", descriptor.getFPS());
			jobj.add("path", descriptor.getPath());
			jarr.add(jobj);
		}
		return jarr;
	}
	
	public static JsonArray getShotList(List<LongDoublePair> result, ShotLookup sl){
		ArrayList<Long> idList = new ArrayList<Long>(result.size());
		for(LongDoublePair ldp : result){
			idList.add(ldp.key);
		}
		Collections.sort(idList);
		JsonArray jarr = new JsonArray();
		for(long id : idList){
			ShotLookup.ShotDescriptor descriptor = sl.lookUpShot(id);
			if(descriptor == null){
				continue;
			}
			JsonObject jobj = new JsonObject();
			jobj.add("name", descriptor.getName());
			jobj.add("startframe", descriptor.getStartFrame()).add("endframe", descriptor.getEndFrame());
			jobj.add("shotid", id);
			jobj.add("videoid", descriptor.getVideoId());
			jobj.add("framerate", descriptor.getFPS());
			jobj.add("path", descriptor.getPath());
			jarr.add(jobj);
		}
		return jarr;
	}
	@Deprecated
	public static JsonObject getPairLists(HashMap<String, List<LongDoublePair>> result){
		JsonObject _return = new JsonObject();
		Set<String> categories = result.keySet();
		for(String category : categories){
			List<LongDoublePair> list = result.get(category);
			JsonArray jarr = new JsonArray();
			for(LongDoublePair ldp : list){
				JsonObject pair = new JsonObject();
				pair.add("id", ldp.key);
				pair.add("score", ldp.value);
				jarr.add(pair);
			}
			_return.add(category, jarr);
		}
		return _return;
	}
	
	public static JsonArray getPairLists(List<LongDoublePair> result){
		JsonArray jarr = new JsonArray();
		for(LongDoublePair ldp : result){
			JsonObject pair = new JsonObject();
			pair.add("id", ldp.key);
			pair.add("score", ldp.value);
			jarr.add(pair);
		}
		return jarr;
	}
	
	public static String resultToJSONString(List<LongDoublePair> result, ShotLookup sl){
		return resultToJSONArray(result, sl).toString();
	}
	
	public static TObjectDoubleHashMap<String> getWeightsFromJsonString(String s){
		TObjectDoubleHashMap<String> weightMap = new TObjectDoubleHashMap<String>();
		JsonObject weights = JsonObject.readFrom(s);
		weightMap.put("global", weights.get("global").asDouble());
		weightMap.put("local", weights.get("local").asDouble());
		weightMap.put("edge", weights.get("edge").asDouble());
		weightMap.put("text", weights.get("text").asDouble());
		weightMap.put("motion", weights.get("motion").asDouble());
		weightMap.put("complex", weights.get("complex").asDouble());
		return weightMap;
	}

	/**
	 * 
	 *@return A JSON array with information about all shots contained in segments. Includes moviename, startframe, shotid, videoid, framerate, path
	 */
	public static JsonArray getShotList(ShotLookup sl, List<MultiIdTriplet> segments) {
		TLongHashSet added = new TLongHashSet();
		
		/*
		 * Add all ids to a hashset
		 */
		ArrayList<Long> idList = new ArrayList<Long>(segments.size() * 2);
		for(MultiIdTriplet mit : segments){
			if(mit.firstId > -1 && !added.contains(mit.firstId)){
				idList.add(mit.firstId);
			}
			if(mit.secondId > -1 && !added.contains(mit.secondId)){
				idList.add(mit.secondId);
			}
		}
		
		Collections.sort(idList);
		
		/*
		 * Add info about all shots to an array
		 */
		JsonArray jarr = new JsonArray();
		for(long id : idList){
			ShotLookup.ShotDescriptor descriptor = sl.lookUpShot(id);
			JsonObject jobj = new JsonObject();
			jobj.add("name", descriptor.getName());
			jobj.add("startframe", descriptor.getStartFrame()).add("endframe", descriptor.getEndFrame());
			jobj.add("shotid", id);
			jobj.add("videoid", descriptor.getVideoId());
			jobj.add("framerate", descriptor.getFPS());
			jobj.add("path", descriptor.getPath());
			jarr.add(jobj);
		}
		return jarr;
	}
	
	/**
	 * Transforms the list to a JSON Array
	 */
	public static JsonArray getTripletLists(List<MultiIdTriplet> result){
		JsonArray jarr = new JsonArray();
		for(MultiIdTriplet mit : result){
			JsonObject pair = new JsonObject();
			pair.add("firstid", mit.firstId);
			pair.add("secondid", mit.secondId);
			pair.add("score", mit.score);
			jarr.add(pair);
		}
		return jarr;
	}

	/**
	 * Sends Results to the client as specified in the JSON-Doc
	 * @param printer Output goes here
	 * @param resultlist Pair of shots and scores
	 * @param category which category has been used to generate the results
	 * @param index index of the query (used for multisketch)
	 */
	public static void printResults(PrintStream printer, List<LongDoublePair> resultlist, JsonValue category, int index) {
		JsonObject resultobj = new JsonObject();
		for(int i = 0; i<resultlist.size();i++){
			resultobj = new JsonObject();
			resultobj.add("type", "result");
			resultobj.add("shotid", resultlist.get(i).key);
			resultobj.add("score", resultlist.get(i).value);
			resultobj.add("category", category);
			resultobj.add("containerid", index);
			printer.print(resultobj.toString());
			printer.println(',');
		}
		
	}

	public static TLongHashSet printShots(PrintStream printer, List<LongDoublePair> resultlist, TLongHashSet shotids) {
		JsonObject resultobj = new JsonObject();
		ShotLookup sl = new ShotLookup();
		for(int i = 0; i<resultlist.size();i++){
			
			long shotid = resultlist.get(i).key;
			if(shotids.contains(shotid)){
				continue;
			}
			shotids.add(shotid);
			ShotLookup.ShotDescriptor descriptor = sl.lookUpShot(shotid);
			
			resultobj = new JsonObject();
			resultobj.add("type", "shot");
			resultobj.add("shotid", resultlist.get(i).key);
			resultobj.add("videoid", descriptor.getVideoId());
			resultobj.add("start", descriptor.getStartFrame());
			resultobj.add("end", descriptor.getEndFrame());
			printer.print(resultobj.toString());
			printer.println(',');
			
		}
		sl.close();
		return shotids;
	}

	public static TIntHashSet printVideos(PrintStream printer, List<LongDoublePair> resultlist, TIntHashSet videoids) {
		JsonObject resultobj = new JsonObject();
		ShotLookup sl = new ShotLookup();
		for(int i = 0; i<resultlist.size();i++){
			long shotid = resultlist.get(i).key;
			ShotLookup.ShotDescriptor descriptor = sl.lookUpShot(shotid);
			
			if(videoids.contains(descriptor.getVideoId())){
				continue;
			}
			videoids.add(descriptor.getVideoId());
			
			resultobj = new JsonObject();
			resultobj.add("type", "video");
			resultobj.add("name", descriptor.getName());
			resultobj.add("videoid",descriptor.getVideoId());
			resultobj.add("path", descriptor.getPath());
			resultobj.add("width", descriptor.getWidth());
			resultobj.add("height", descriptor.getHeight());
			resultobj.add("frames", descriptor.getFramecount());
			resultobj.add("seconds", descriptor.getSeconds());
			printer.print(resultobj.toString());
			printer.println(',');
		}
		sl.close();
		return videoids;
	}
	
	/**
	 * Sends Results to the client as specified in the JSON-Doc
	 * @param printer Output goes here
	 * @param resultlist Pair of shots and scores
	 * @param category which category has been used to generate the results
	 * @param index index of the query (used for multisketch)
	 */
	public static void printResults(PrintStream printer, List<LongDoublePair> resultlist, JsonValue category) {
		JsonObject resultobj = new JsonObject();
		for(int i = 0; i<resultlist.size();i++){
			resultobj = new JsonObject();
			resultobj.add("type", "result");
			resultobj.add("shotid", resultlist.get(i).key);
			resultobj.add("score", resultlist.get(i).value);
			resultobj.add("category", category);
			printer.print(resultobj.toString());
			printer.println(',');
		}
	}
	
	public static String formatResultName(String name){
		JsonObject resultobj = new JsonObject();
		resultobj.add("type", "resultname");
		resultobj.add("name", name);
		return resultobj.toString();
	}
	
	public static void printResultName(PrintStream printer, String name){
		printer.print(formatResultName(name));
		printer.println(',');
	}
}
