package ch.unibas.cs.dbis.cineast.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.ADAMproSelector;
import ch.unibas.cs.dbis.cineast.core.db.DBResultCache;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup.ShotDescriptor;
import ch.unibas.cs.dbis.cineast.core.db.VideoLookup;
import ch.unibas.cs.dbis.cineast.core.util.ContinousRetrievalLogic;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * Handles connection to and from the Client As the name of the class suggests,
 * communication is done via JSON-Objects
 */
public class JSONAPIThread extends Thread {

	private Socket socket = null;
	private Reader reader;
	private PrintStream printer;

	private static Logger LOGGER = LogManager.getLogger();

	public JSONAPIThread(Reader reader, PrintStream printer) {
		this.printer = printer;
		this.reader = reader;
	}

	public JSONAPIThread(Socket socket) throws IOException {
		this(new InputStreamReader(socket.getInputStream()), new PrintStream(socket.getOutputStream()));
		this.socket = socket;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis(); 
		/*
		 * _return will get sent to the client at the end with a toString() call
		 */
		JsonObject _return = new JsonObject();
		try {
			JsonObject clientJSON = JsonObject.readFrom(reader);

			switch (clientJSON.get("queryType").asString()) {
				/*
				 * Input: id: ID of a video
				 * 
				 * Output: Information about the video - path, name etc. List of
				 * all shots belonging to the video and their startframe and
				 * endframe
				 */
			case "video": {
				JsonObject queryObject = clientJSON.get("query").asObject();
				// String category = queryObject.get("category").asString();
				String shotId = queryObject.get("shotid").asString();
				
				ShotLookup sl = new ShotLookup();
				ShotDescriptor shot = sl.lookUpShot(shotId);
				//List<ShotDescriptor> allShots = sl.lookUpVideo(shot.getVideoId());
				
				//Send metadata
				VideoLookup vl = new VideoLookup();
				VideoLookup.VideoDescriptor descriptor = vl.lookUpVideo(shot.getVideoId());
			
				JsonObject resultobj = JSONEncoder.encodeVideo(descriptor);
				
//				vl.close();
				this.printer.print(resultobj.toString());
				this.printer.print(',');
				
				String id = descriptor.getVideoId();
				
				//send shots
				DBSelector selector = new ADAMproSelector();
				ResultSet rset;
//				rset = selector.select("SELECT id, startframe, endframe FROM cineast.shots WHERE video = " + id);
				int i = 0;
//				while (rset.next()) {
//					ShotLookup.ShotDescriptor desc= sl.lookUpShot(rset.getInt(1));
//					
//					resultobj = JSONEncoder.encodeShot(rset.getInt(1), desc.getVideoId(), desc.getStartFrame(), desc.getEndFrame());
//					
//					this.printer.print(resultobj.toString());
//					this.printer.print(',');
//					printer.flush();
//					if(i % 20 == 0){
//						Thread.sleep(100);
//					}
//				}

				sl.close();
				vl.close();
				selector.close();
				
				break;
			}

			case "relevanceFeedback": {
				JsonObject queryObject = clientJSON.get("query").asObject();
				JsonArray categories = queryObject.get("categories").asArray();
				JsonArray parr = queryObject.get("positive").asArray();
				JsonArray narr = queryObject.get("negative").asArray();
				HashSet<String> shotids = new HashSet<>();
				HashSet<String> videoids = new HashSet<>();
				List<StringDoublePair> result;
				TObjectDoubleHashMap<String> map;

				//String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString(); 
				QueryConfig qconf = null;
				
				
				for (JsonValue category : categories) {
					map = new TObjectDoubleHashMap<>();

					for (JsonValue _el : parr) {
						String _shotid = _el.asString();
						result = ContinousRetrievalLogic.retrieve(_shotid, category.asString(), qconf);
						for (StringDoublePair pair : result) {
							if (Double.isInfinite(pair.value) || Double.isNaN(pair.value)) {
								continue;
							}
							if (map.contains(pair.key)) {
								map.put(pair.key, map.get(pair.key) + pair.value);
								continue;
							}
							map.put(pair.key, pair.value);
						}
					}
					for (JsonValue _el : narr) {
						String _shotid = _el.asString();
						result = ContinousRetrievalLogic.retrieve(_shotid, category.asString(), qconf);
						for (StringDoublePair pair : result) {
							if (Double.isInfinite(pair.value) || Double.isNaN(pair.value)) {
								continue;
							}
							if (map.contains(pair.key)) {
								map.put(pair.key, map.get(pair.key) - pair.value);
								continue;
							}
							map.put(pair.key, -pair.value);
						}
					}

					// Take positive score values & put together the definite
					// list
					List<StringDoublePair> list = new ArrayList<>(map.size());
					String[] keys = (String[]) map.keys();
					for (String key : keys) {
						double val = map.get(key);
						if (val > 0) {
							list.add(new StringDoublePair(key, val));
						}
					}

					Collections.sort(list, StringDoublePair.COMPARATOR);

					int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();

					if (list.size() > MAX_RESULTS) {
						list = list.subList(0, MAX_RESULTS);
					}
					videoids = JSONUtils.printVideosBatched(printer, list, videoids);
					shotids = JSONUtils.printShotsBatched(printer, list, shotids);
					JSONUtils.printResultsBatched(printer, list, category.asString(), 1);

				}
				
				String resultName = DBResultCache.newCachedResult(shotids);
				JSONUtils.printResultName(printer, resultName);
				break;
			}

				/*
				 * Input: Multiple QueryContainers A QueryContainer can contain
				 * an id
				 * 
				 * Output: A sorted list of movie sequences
				 */
			case "multiSketch": {
				JsonArray queryArray = clientJSON.get("query").asArray();
				HashSet<String> shotids = new HashSet<>();
				HashSet<String> videoids = new HashSet<>();

				String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString();
				if(resultCacheName != null && resultCacheName.equalsIgnoreCase("null")){
					resultCacheName = null;
				}
				
				QueryConfig qconf = null; //TODO
				
				DBResultCache.createIfNecessary(resultCacheName);
				
				int index = 1;
				for (Iterator<JsonValue> it = queryArray.iterator(); it.hasNext(); ++index) {

					JsonObject query = it.next().asObject();
					for (JsonValue category : query.get("categories").asArray()) {

						List<StringDoublePair> result;
						if (query.get("id") != null) {
							String id = query.get("id").asString();
							result = ContinousRetrievalLogic.retrieve(id, category.asString(), qconf);
						} else {
							QueryContainer qc = JSONUtils.queryContainerFromJSON(query);
							result = ContinousRetrievalLogic.retrieve(qc, category.asString(), qconf);
						}
						
						videoids = JSONUtils.printVideosBatched(printer, result, videoids);
						shotids = JSONUtils.printShotsBatched(printer, result, shotids);
						JSONUtils.printResultsBatched(printer, result, category.asString(), index);

					}
				}

				String resultName = DBResultCache.newCachedResult(shotids);
				JSONUtils.printResultName(printer, resultName);

				break;
			}
			
			case "query":{
				
				JsonArray queryArray = clientJSON.get("query").asArray();
				HashSet<String> shotids = new HashSet<>();
				HashSet<String> videoids = new HashSet<>();

				String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString();
				if(resultCacheName != null && resultCacheName.equalsIgnoreCase("null")){
					resultCacheName = null;
				}
				
				QueryConfig qconf = null; //TODO
				
				DBResultCache.createIfNecessary(resultCacheName);
				
				HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
				
				for(JsonValue jval : queryArray){
					JsonObject jobj = jval.asObject();
					QueryContainer qc = JSONUtils.queryContainerFromJSON(jobj);
					if(qc.getWeight() == 0f || jobj.get("categories") == null){
						continue;
					}
					for(JsonValue c : jobj.get("categories").asArray()){
						String category = c.asString();
						if(!categoryMap.containsKey(category)){
							categoryMap.put(category, new ArrayList<>());
						}
						categoryMap.get(category).add(qc);
					}
				}
				
				Set<String> categories = categoryMap.keySet();
				
				
				List<StringDoublePair> result;
				for(String category : categories){
					TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
					for(QueryContainer qc : categoryMap.get(category)){
						
						float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation 
						
						if(qc.hasId()){
							result = ContinousRetrievalLogic.retrieve(qc.getId(), category, qconf);
						}else{
							result = ContinousRetrievalLogic.retrieve(qc, category, qconf);
						}
						
						for (StringDoublePair pair : result) {
							if (Double.isInfinite(pair.value) || Double.isNaN(pair.value)) {
								continue;
							}
							if (map.contains(pair.key)) {
								map.put(pair.key, map.get(pair.key) + pair.value * weight);
								continue;
							}
							map.put(pair.key, pair.value * weight);
						}
						
						List<StringDoublePair> list = new ArrayList<>(map.size());
						String[] keys = (String[]) map.keys();
						for (String key : keys) {
							double val = map.get(key);
							if (val > 0) {
								list.add(new StringDoublePair(key, val));
							}
						}

						Collections.sort(list, StringDoublePair.COMPARATOR);

						int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();

						if (list.size() > MAX_RESULTS) {
							list = list.subList(0, MAX_RESULTS);
						}
						videoids = JSONUtils.printVideosBatched(printer, list, videoids);
						shotids = JSONUtils.printShotsBatched(printer, list, shotids);
						JSONUtils.printResultsBatched(printer, list, category, 1);
						
					}
					
					
				}
				
				break;
			}
			
			/*
			 * Input: List of shotids
			 * Output: Information about neighboring shots
			 */
			case "context":{
				LOGGER.debug("Context API call starting");
				JsonObject query = clientJSON.get("query").asObject();
				JsonArray shotidlist = query.get("shotidlist").asArray();
				int limit = query.get("limit") == null ? 1 : query.get("limit").asInt();
				DBSelector selector = new ADAMproSelector();
				ShotLookup sl = new ShotLookup();
				ShotLookup.ShotDescriptor descriptor;
				this.printer.print('[');
				
//				PreparedStatement select = selector.createPreparedStatement("(select id, startframe, endframe from cineast.shots WHERE video=? AND startframe<? ORDER BY startframe desc LIMIT ?)UNION(select id, startframe, endframe from cineast.shots WHERE video=? AND endframe>? ORDER BY startframe asc LIMIT ?)");
				
				JsonObject batch = new JsonObject();
				batch.add("type", "batch");
				batch.add("inner", "shot");
				JsonArray array = new JsonArray();
				
				for(int i = 0; i < shotidlist.size(); ++i){
//					JsonValue val = shotidlist.get(i);
//					String shotid = val.asString();
//					descriptor = sl.lookUpShot(shotid);
					
//					select.setInt(1, descriptor.getVideoId());
//					select.setInt(2, descriptor.getStartFrame());
//					select.setInt(3, limit);
//					select.setInt(4, descriptor.getVideoId());
//					select.setInt(5, descriptor.getEndFrame());
//					select.setInt(6, limit);
					
					
//					ResultSet rset = select.executeQuery();
//					while(rset != null && rset.next()){
//						array.add(JSONEncoder.encodeShot(rset.getLong(1), descriptor.getVideoId(), rset.getLong(2), rset.getLong(3), false));						
//					}					
				}
				batch.add("array", array);
				printer.println(batch.toString());
				this.printer.print(']');
				this.printer.flush();
				this.printer.close();
				selector.close();
				sl.close();
				
				LOGGER.debug("Context API call ending");
				break;
			}

			default: {
				LOGGER.warn("queryType {} is unknown", clientJSON.get("queryType").asString());
			}
			}

		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		} catch (Exception e) {
			LOGGER.error(e.getMessage() + " | " + e.toString() + "\n");
			e.printStackTrace();
		} finally {
			try {
				LOGGER.debug("Finished API request in {} ms", (System.currentTimeMillis() - startTime));
				this.printer.print(_return.toString());

				/*
				 * Cleanup
				 */
				this.printer.flush();
				this.printer.close();
				this.reader.close();
				if (this.socket != null) {
					this.socket.close();
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + " | " + e.toString() + "\n");
				e.printStackTrace();
			}

		}

	}

}
