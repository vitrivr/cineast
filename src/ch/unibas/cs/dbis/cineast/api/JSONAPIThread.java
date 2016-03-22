package ch.unibas.cs.dbis.cineast.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.Socket;
import java.sql.PreparedStatement;
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
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup.ShotDescriptor;
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
				long shotId = queryObject.get("shotid").asLong();
				
				ShotLookup sl = new ShotLookup();
				ShotDescriptor shot = sl.lookUpShot(shotId);
				//List<ShotDescriptor> allShots = sl.lookUpVideo(shot.getVideoId());
				
				//Send metadata
				VideoLookup vl = new VideoLookup();
				VideoLookup.VideoDescriptor descriptor = vl.lookUpVideo(shot.getVideoId());
			
				JsonObject resultobj = JSONEncoder.encodeVideo(descriptor);
				
				vl.close();
				this.printer.print(resultobj.toString());
				this.printer.print(',');
				
				long id = descriptor.getVideoId();
				
				//send shots
				DBSelector selector = new DBSelector();
				ResultSet rset;
				rset = selector.select("SELECT id, startframe, endframe FROM cineast.shots WHERE video = " + id);
				int i = 0;
				while (rset.next()) {
					ShotLookup.ShotDescriptor desc= sl.lookUpShot(rset.getInt(1));
					
					resultobj = JSONEncoder.encodeShot(rset.getInt(1), desc.getVideoId(), desc.getStartFrame(), desc.getEndFrame());
					
					this.printer.print(resultobj.toString());
					this.printer.print(',');
					printer.flush();
					if(i % 20 == 0){
						Thread.sleep(100);
					}
				}

				sl.close();
				selector.close();
				
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

					int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();

					if (list.size() > MAX_RESULTS) {
						list = list.subList(0, MAX_RESULTS);
					}
					videoids = JSONUtils.printVideosBatched(printer, list, videoids);
					shotids = JSONUtils.printShotsBatched(printer, list, shotids);
					JSONUtils.printResultsBatched(printer, list, category, 1);

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
				if(resultCacheName != null && resultCacheName.equalsIgnoreCase("null")){
					resultCacheName = null;
				}
				
				DBResultCache.createIfNecessary(resultCacheName);
				
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
						
						videoids = JSONUtils.printVideosBatched(printer, result, videoids);
						shotids = JSONUtils.printShotsBatched(printer, result, shotids);
						JSONUtils.printResultsBatched(printer, result, category, index);

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
				int limit = query.get("limit") == null ? 1 : query.get("limit").asInt();
				DBSelector selector = new DBSelector();
				ShotLookup sl = new ShotLookup();
				ShotLookup.ShotDescriptor descriptor;
				this.printer.print('[');
				
				PreparedStatement select = selector.createPreparedStatement("(select id, startframe, endframe from cineast.shots WHERE video=? AND startframe<? ORDER BY startframe desc LIMIT ?)UNION(select id, startframe, endframe from cineast.shots WHERE video=? AND endframe>? ORDER BY startframe asc LIMIT ?)");
				
				JsonObject batch = new JsonObject();
				batch.add("type", "batch");
				batch.add("inner", "shot");
				JsonArray array = new JsonArray();
				
				for(int i = 0; i < shotidlist.size(); ++i){
					JsonValue val = shotidlist.get(i);
					long shotid = val.asLong();
					descriptor = sl.lookUpShot(shotid);
					
					select.setInt(1, descriptor.getVideoId());
					select.setInt(2, descriptor.getStartFrame());
					select.setInt(3, limit);
					select.setInt(4, descriptor.getVideoId());
					select.setInt(5, descriptor.getEndFrame());
					select.setInt(6, limit);
					
					
					ResultSet rset = select.executeQuery();
					while(rset != null && rset.next()){
						array.add(JSONEncoder.encodeShot(rset.getLong(1), descriptor.getVideoId(), rset.getLong(2), rset.getLong(3), false));						
					}					
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
