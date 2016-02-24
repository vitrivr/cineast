package ch.unibas.cs.dbis.cineast.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.db.DBResultCache;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup;
import ch.unibas.cs.dbis.cineast.core.db.VideoLookup;
import ch.unibas.cs.dbis.cineast.core.util.ContinousRetrievalLogic;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

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

			// TODO Enum
			switch (clientJSON.get("queryType").asString()) {
			/*
			 * Input: queryContainer: A sketch or a picture Input: category:
			 * Which category of features does the client want to use
			 * 
			 * Output: Sorted list with shots and scores and which category of
			 * features have been used
			 */
			case "sketch": {
				JsonObject queryObject = clientJSON.get("query").asObject();
				String category = queryObject.get("category").asString();
				QueryContainer qc = JSONUtils.queryContainerFromJSON(queryObject);
				String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString(); 
				List<LongDoublePair> result = ContinousRetrievalLogic.retrieve(qc, category, resultCacheName);

				_return.add("shots", JSONUtils.getShotList(result, new ShotLookup()));
				_return.add("scores", JSONUtils.getPairLists(result));
				_return.add("category", category);
				String resultName = DBResultCache.newCachedResult(result);
				_return.add("resultname", resultName);
				break;
			}

				/*
				 * Input: id of a shot(?)
				 * 
				 * Output: Similar to shot
				 */
			case "id": {
				JsonObject queryObject = clientJSON.get("query").asObject();
				String category = queryObject.get("category").asString();
				long id = queryObject.get("id").asLong();
				String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString(); 
				List<LongDoublePair> result = ContinousRetrievalLogic.retrieve(id, category, resultCacheName);

				_return.add("shots", JSONUtils.getShotList(result, new ShotLookup()));
				_return.add("scores", JSONUtils.getPairLists(result));
				_return.add("category", category);
				String resultName = DBResultCache.newCachedResult(result); 
				_return.add("resultname", JSONUtils.formatResultName(resultName));
				break;
			}

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
				long id = queryObject.get("id").asLong();
				
				//Send metadata
				VideoLookup vl = new VideoLookup();
				VideoLookup.VideoDescriptor descriptor = vl.lookUpVideo(id);
				JsonObject resultobj = new JsonObject();
				resultobj.add("type", "video");
				resultobj.add("name", descriptor.getName());
				resultobj.add("videoid",descriptor.getVideoId());
				resultobj.add("path", descriptor.getPath());
				resultobj.add("width", descriptor.getWidth());
				resultobj.add("height", descriptor.getHeight());
				resultobj.add("frames", descriptor.getFramecount());
				resultobj.add("seconds", descriptor.getSeconds());
				vl.close();
				this.printer.print(resultobj.toString()+",\n");
				
				//send shots
				DBSelector selector = new DBSelector();
				ResultSet rset;
				rset = selector.select("SELECT id, startframe, endframe FROM cineast.shots WHERE video = " + id);
				ShotLookup sl = new ShotLookup();
				while (rset.next()) {
					ShotLookup.ShotDescriptor desc= sl.lookUpShot(rset.getInt(1));
					
					resultobj = new JsonObject();
					resultobj.add("type", "shot");
					resultobj.add("shotid", rset.getInt(1));
					resultobj.add("videoid", desc.getVideoId());
					resultobj.add("start", desc.getStartFrame());
					resultobj.add("end", desc.getEndFrame());
					this.printer.print(resultobj.toString()+",\n");
				}
				sl.close();
				selector.close();
				// _return.add("category", category);
				break;
			}

			case "relevanceFeedback": {
				JsonObject queryObject = clientJSON.get("query").asObject();
				JsonArray categories = queryObject.get("categories").asArray();
				JsonArray parr = queryObject.get("positive").asArray();
				JsonArray narr = queryObject.get("negative").asArray();
				TLongHashSet shotids = new TLongHashSet();
				TIntHashSet videoids = new TIntHashSet();
				List<LongDoublePair> result;
				TLongDoubleHashMap map;

				String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString(); 
				
				for (JsonValue category : categories) {
					map = new TLongDoubleHashMap();

					for (JsonValue _el : parr) {
						long _shotid = _el.asLong();
						result = ContinousRetrievalLogic.retrieve(_shotid, category.asString(), resultCacheName);
						for (LongDoublePair pair : result) {
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
						long _shotid = _el.asLong();
						result = ContinousRetrievalLogic.retrieve(_shotid, category.asString(), resultCacheName);
						for (LongDoublePair pair : result) {
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
					List<LongDoublePair> list = new ArrayList<>(map.size());
					long[] keys = map.keys();
					for (long key : keys) {
						double val = map.get(key);
						if (val > 0) {
							list.add(new LongDoublePair(key, val));
						}
					}

					Collections.sort(list, LongDoublePair.COMPARATOR);

					int MAX_RESULTS = Config.maxResults();

					if (list.size() > MAX_RESULTS) {
						list = list.subList(0, MAX_RESULTS);
					}

					JSONUtils.printResults(printer, list, category);

					shotids = JSONUtils.printShots(printer, list, shotids);
					videoids = JSONUtils.printVideos(printer, list, videoids);
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
				TLongHashSet shotids = new TLongHashSet();
				TIntHashSet videoids = new TIntHashSet();

				String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString(); 
				int index = 1;
				for (Iterator<JsonValue> it = queryArray.iterator(); it.hasNext(); ++index) {

					JsonObject query = it.next().asObject();
					for (JsonValue category : query.get("categories").asArray()) {
						List<LongDoublePair> result;
						if (query.get("id") != null && query.get("id").asLong() > 0) {
							long id = query.get("id").asLong();
							result = ContinousRetrievalLogic.retrieve(id, category.asString(), resultCacheName);
						} else {
							QueryContainer qc = JSONUtils.queryContainerFromJSON(query);
							result = ContinousRetrievalLogic.retrieve(qc, category.asString(), resultCacheName);
						}
						JSONUtils.printResults(printer, result, category, index);

						shotids = JSONUtils.printShots(printer, result, shotids);
						videoids = JSONUtils.printVideos(printer, result, videoids);

						// this.printer.print(_return.toString()+",\n");
					}
				}

				String resultName = DBResultCache.newCachedResult(shotids);
				JSONUtils.printResultName(printer, resultName);

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
				DBSelector selector = new DBSelector();
				ShotLookup sl = new ShotLookup();
				ShotLookup.ShotDescriptor descriptor;
				JsonArray _ret = new JsonArray();
				
				/*
				 * In Java, a JsonArray is implemented as a List indexed over ints
				 * Since a shotid is a long, we return an array in which an element looks like this:
				 * [22 - shotid, [id, startframe, endframe] - previous shot, [id, startframe, endframe] - following shot]
				 */
				for(JsonValue val: shotidlist){
					long shotid = val.asLong();
					descriptor = sl.lookUpShot(shotid);
					
					JsonArray shot = new JsonArray();
					shot.add(shotid);
					ResultSet rset = selector.select("(select id, startframe, endframe from shots WHERE video="+descriptor.getVideoId() +" AND startframe<"+descriptor.getStartFrame()+" ORDER BY startframe desc LIMIT 1)UNION(select id, startframe, endframe from shots WHERE video="+descriptor.getVideoId()+" AND endframe>"+descriptor.getEndFrame()+" ORDER BY startframe asc LIMIT 1)");
					while(rset.next()){
						//System.err.println(rset.getLong(1)+"  | "+rset.getLong(2) +" | "+rset.getLong(3)+" | "+descriptor.getStartFrame()+" | "+descriptor.getEndFrame());
						JsonObject metadata = new JsonObject();
						metadata.add("shotid", rset.getLong(1));
						metadata.add("startframe", rset.getLong(2));
						metadata.add("endframe", rset.getLong(3));
						shot.add(metadata);
					}
					_ret.add(shot);
				}
				this.printer.print(_ret.toString());
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
		} catch (SQLException e) {
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
