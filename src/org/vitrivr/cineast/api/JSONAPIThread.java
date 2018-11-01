package org.vitrivr.cineast.api;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.art.modules.visualization.VisualizationCache;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.Position;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.query.containers.ImageQueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.explorative.PlaneHandler;
import org.vitrivr.cineast.explorative.PlaneManager;

/**
 * Handles connection to and from the Client As the name of the class suggests,
 * communication is done via JSON-Objects
 */
@Deprecated
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

        MediaSegmentReader sl = new MediaSegmentReader();
        MediaSegmentDescriptor shot = sl.lookUpSegment(shotId).get();
        //List<ShotDescriptor> allShots = sl.lookUpVideo(shot.getObjectId());

        //Send metadata
        MediaObjectReader vl = new MediaObjectReader();
        MediaObjectDescriptor descriptor = vl.lookUpObjectById(shot.getObjectId());

        JsonObject resultobj = JSONEncoder.encodeVideo(descriptor);

//        vl.close();
        this.printer.print(resultobj.toString());
        this.printer.print(',');

        sl.close();
        vl.close();
        break;
      }

        /*
         * Input: id: ID of a shot
         *
         * Output: Information about the shot- startframe, endframe etc.
         */
        case "shot": {
          String shotId = clientJSON.get("shotid").asString();

          MediaSegmentReader sl = new MediaSegmentReader();
          MediaSegmentDescriptor shot = sl.lookUpSegment(shotId).get();

          JsonObject resultobj = new JsonObject();
          resultobj.add("type", "submitShot").add("videoId", shot.getObjectId()).add("start", shot.getStart()).add("end", shot.getEnd());

          this.printer.print(resultobj.toString());
          this.printer.print(',');

          sl.close();

          break;
        }


      case "relevanceFeedback": {
        JsonObject queryObject = clientJSON.get("query").asObject();
        JsonArray categories = queryObject.get("categories").asArray();
        JsonArray parr = queryObject.get("positive").asArray();
        JsonArray narr = queryObject.get("negative").asArray();
        HashSet<String> shotids = new HashSet<>();
        HashSet<String> videoids = new HashSet<>();
        List<SegmentScoreElement> result;
        TObjectDoubleHashMap<String> map;

        //String resultCacheName = clientJSON.get("resultname") == null ? null : clientJSON.get("resultname").asString(); 
        QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());
        
        
        for (JsonValue category : categories) {
          map = new TObjectDoubleHashMap<>();

          for (JsonValue _el : parr) {
            String _shotid = _el.asString();
            result = ContinuousRetrievalLogic.retrieve(_shotid, category.asString(), qconf);
            for (SegmentScoreElement element : result) {
              String segmentId = element.getSegmentId();
              double score = element.getScore();
              if (Double.isInfinite(score) || Double.isNaN(score)) {
                continue;
              }
              map.adjustOrPutValue(segmentId, score, score);
            }
          }
          for (JsonValue _el : narr) {
            String _shotid = _el.asString();
            result = ContinuousRetrievalLogic.retrieve(_shotid, category.asString(), qconf);
            for (SegmentScoreElement element : result) {
              String segmentId = element.getSegmentId();
              double score = element.getScore();
              if (Double.isInfinite(score) || Double.isNaN(score)) {
                continue;
              }
              map.adjustOrPutValue(segmentId, -score, -score);
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

          int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();

          if (list.size() > MAX_RESULTS) {
            list = list.subList(0, MAX_RESULTS);
          }
          videoids = JSONUtils.printVideosBatched(printer, list, videoids);
          shotids = JSONUtils.printShotsBatched(printer, list, shotids);
          JSONUtils.printResultsBatched(printer, list, category.asString(), 1);

        }
        
//        String resultName = DBResultCache.newCachedResult(shotids);
//        JSONUtils.printResultName(printer, resultName);
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
        
        QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());
        
//        DBResultCache.createIfNecessary(resultCacheName);
        
        int index = 1;
        for (Iterator<JsonValue> it = queryArray.iterator(); it.hasNext(); ++index) {

          JsonObject query = it.next().asObject();
          for (JsonValue category : query.get("categories").asArray()) {

            List<SegmentScoreElement> scores;
            if (query.get("id") != null) {
              String id = query.get("id").asString();
              scores = ContinuousRetrievalLogic.retrieve(id, category.asString(), qconf);
            } else {
              ImageQueryContainer qc = JSONUtils.queryContainerFromJSON(query);
              scores = ContinuousRetrievalLogic.retrieve(qc, category.asString(), qconf);
            }
            List<StringDoublePair> pairs = scores.stream().map(e -> new StringDoublePair(e.getSegmentId(), e.getScore())).collect(
                Collectors.toList());
            
            videoids = JSONUtils.printVideosBatched(printer, pairs, videoids);
            shotids = JSONUtils.printShotsBatched(printer, pairs, shotids);
            JSONUtils.printResultsBatched(printer, pairs, category.asString(), index);

          }
        }

//        String resultName = DBResultCache.newCachedResult(shotids);
//        JSONUtils.printResultName(printer, resultName);

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
        
        QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());
        
//        DBResultCache.createIfNecessary(resultCacheName);
        
        HashMap<String, ArrayList<ImageQueryContainer>> categoryMap = new HashMap<>();
        
        for(JsonValue jval : queryArray){
          JsonObject jobj = jval.asObject();
          ImageQueryContainer qc = JSONUtils.queryContainerFromJSON(jobj);
          if(qc.getWeight() == 0f || jobj.get("categories") == null){
            continue;
          }
          for(JsonValue c : jobj.get("categories").asArray()){
            String category = c.asString();
            if(!categoryMap.containsKey(category)){
              categoryMap.put(category, new ArrayList<ImageQueryContainer>());
            }
            categoryMap.get(category).add(qc);
          }
        }
        
        Set<String> categories = categoryMap.keySet();
        

        List<SegmentScoreElement> result;
        for(String category : categories){
          TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
          for(ImageQueryContainer qc : categoryMap.get(category)){
            
            float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation 
            
            if(qc.hasId()){
              result = ContinuousRetrievalLogic.retrieve(qc.getId(), category, qconf);
            }else{
              result = ContinuousRetrievalLogic.retrieve(qc, category, qconf);
            }
            for (SegmentScoreElement element : result) {
              String segmentId = element.getSegmentId();
              double score = element.getScore();
              if (Double.isInfinite(score) || Double.isNaN(score)) {
                continue;
              }
              map.adjustOrPutValue(segmentId, score * weight, score * weight);
            }
            
            List<StringDoublePair> list = new ArrayList<>(map.size());
            Set<String> keys = map.keySet();
            for (String key : keys) {
              double val = map.get(key);
              if (val > 0) {
                list.add(new StringDoublePair(key, val));
              }
            }

            Collections.sort(list, StringDoublePair.COMPARATOR);

            int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();

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
        int limit = query.get("limit") == null ? 5 : query.get("limit").asInt();
        MediaSegmentReader sl = new MediaSegmentReader();
//        MediaSegmentReader.MediaSegmentDescriptor descriptor;
        this.printer.print('[');
        
        
        JsonObject batch = new JsonObject();
        batch.add("type", "batch");
        batch.add("inner", "shot");
        JsonArray array = new JsonArray();
        
        for(int i = 0; i < shotidlist.size(); ++i){
          
          JsonValue val = shotidlist.get(i);
          String shotid = val.asString();
          MediaSegmentDescriptor descriptor = sl.lookUpSegment(shotid).get();
          
          String video = descriptor.getObjectId();
          int startSegment = Math.max(1, descriptor.getSequenceNumber() - limit);
          int endSegment = descriptor.getSequenceNumber() + limit;
          
          List<MediaSegmentDescriptor> all = sl.lookUpSegmentsOfObject(video);

          for(MediaSegmentDescriptor sd : all){
            if(sd.getSequenceNumber() >= startSegment && sd.getSequenceNumber() <= endSegment){
              array.add(JSONEncoder.encodeShot(sd));
            }
          }
    
        }
        batch.add("array", array);
        printer.println(batch.toString());
        this.printer.print(']');
        this.printer.flush();
        this.printer.close();
        sl.close();
        
        LOGGER.debug("Context API call ending");
        break;
      }
//      case "getLabels":{
//        LOGGER.debug("Label API call starting");
//        JsonArray jsonConcepts = new JsonArray();
//        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
//        selector.open(NeuralNetFeature.getClassTableName());
//
//        List<PrimitiveTypeProvider> queryRes = selector.getAll(NeuralNetFeature.getHumanLabelColName());
//        HashSet<String> labels = new HashSet<>(queryRes.size());
//        //Eliminate Duplicates
//        for(PrimitiveTypeProvider el : queryRes){
//          labels.add(el.getString());
//        }
//        for(String el : labels) {
//          jsonConcepts.add(el);
//        }
//        _return.set("concepts", jsonConcepts);
//        selector.close();
//        LOGGER.debug("Concepts API call ending");
//        break;
//      }

      case "getMultimediaobjects":{
        List<MediaObjectDescriptor> multimediaobjectIds = new MediaObjectReader().getAllObjects();

        JsonArray movies = new JsonArray();
        for(MediaObjectDescriptor descriptor: multimediaobjectIds){
          JsonObject resultobj = JSONEncoder.encodeVideo(descriptor);
          movies.add(resultobj);
        }

        _return.set("multimediaobjects", movies);
        break;
      }

      case "getSegments":{
        String multimediaobjectId = clientJSON.get("multimediaobjectId").asString();
        List<MediaSegmentDescriptor> segments = new MediaSegmentReader().lookUpSegmentsOfObject(multimediaobjectId);
        Collections.sort(segments, new SegmentDescriptorComparator());

        JsonArray list = new JsonArray();
        for (MediaSegmentDescriptor segment: segments) {
          list.add(segment.getSegmentId());
        }

        _return.set("segments", list);
        break;
      }

      case "getVisualizations":{
        JsonArray visual = new JsonArray();
        for(Class<? extends Visualization> visualization: Config.sharedConfig().getVisualization().getVisualizations()){
          Visualization obj = visualization.newInstance();
          JsonObject element = new JsonObject();
          element.add("className", visualization.getCanonicalName());
          element.add("displayName", obj.getDisplayName());

          JsonArray types = new JsonArray();
          for(VisualizationType t: obj.getVisualizations()){
            types.add(t.toString());
          }
          element.add("visualizationTypes", types);
          visual.add(element);
        }
        _return.set("visualizations", visual);
        break;
      }

      case "getVisualizationCategories":{
        JsonArray visual = new JsonArray();
        for(String el: Config.sharedConfig().getVisualization().getVisualizationCategories()){
          visual.add(el);
        }
        _return.set("visualizationCategories", visual);
        break;
      }

      case "getArt":{
        VisualizationType visualizationType = VisualizationType.valueOf(clientJSON.get("visualizationType").asString());
        Class<?> visualizationClass;
        try {
          visualizationClass = Class.forName(clientJSON.get("visualization").asString());
        } catch (ClassNotFoundException e){
          _return.add("visualizationError", "Invalid visualizationClass!");
          break;
        }
        if(!Config.sharedConfig().getVisualization().isValidVisualization(visualizationClass)){
          _return.add("visualizationError", "Invalid visualizationClass!");
          break;
        }
        List<String> objectIds = new ArrayList<String>();
        switch(visualizationType){
          case VISUALIZATION_SEGMENT:
            objectIds.add(clientJSON.get("segmentId").asString());
            break;
          case VISUALIZATION_MULTIMEDIAOBJECT:
            objectIds.add(clientJSON.get("multimediaobjectId").asString());
            break;
          case VISUALIZATION_MULTIPLESEGMENTS:
            for(JsonValue i:clientJSON.get("segmentIds").asArray()) {
              objectIds.add(i.asString());
            }
            break;
          default:
            LOGGER.error("Missing VisualizationType in API implementation!");
            break;
        }
        Visualization visualization = (Visualization)visualizationClass.newInstance();
        String result = VisualizationCache.getFromCache(visualization.getDisplayName(), visualizationType, objectIds);
        if(result == null) {
          visualization.init(Config.sharedConfig().getDatabase().getSelectorSupplier());
          switch(visualizationType){ //I'm not completely happy that I have to use the same switch cases again, should be possible in a better way
            case VISUALIZATION_MULTIMEDIAOBJECT:
              result = visualization.visualizeMultimediaobject(objectIds.get(0));
              break;
            case VISUALIZATION_SEGMENT:
              result = visualization.visualizeSegment(objectIds.get(0));
              break;
            case VISUALIZATION_MULTIPLESEGMENTS:
              result = visualization.visualizeMultipleSegments(objectIds);
              break;
            default:
              break;
          }
          VisualizationCache.cacheResult(visualization.getDisplayName(), visualizationType, objectIds, result);
        }
        _return.add("resultData", result);
        _return.add("resultType", visualization.getResultType().toString());
        visualization.finish();
        break;
      }


      case "meta":{
        JsonObject query = clientJSON.get("query").asObject();
        JsonArray idList = query.get("idlist").asArray();
        ArrayList<String> ids = new ArrayList<>(idList.size());
        for(JsonValue v : idList){
          ids.add(v.asString());
        }
        MediaSegmentReader lookup = new MediaSegmentReader();
        Map<String, MediaSegmentDescriptor> segments = lookup.lookUpSegments(ids);
        lookup.close();
        
        HashSet<String> mmobjectIds = new HashSet<>();
        for(MediaSegmentDescriptor descriptor : segments.values()){
          mmobjectIds.add(descriptor.getObjectId());
        }
        
        MediaObjectReader mmlookup = new MediaObjectReader();
        Map<String, MediaObjectDescriptor> mmobjects = mmlookup.lookUpObjects(mmobjectIds);
        mmlookup.close();
        
        printer.print(JSONEncoder.encodeVideoBatch(mmobjects.values()));
        printer.println(",");
        printer.print(JSONEncoder.encodeShotBatch(segments.values()).toString());
        printer.println(",");
        
        break;
      }

      case "explorative_tile_position": {
        LOGGER.debug("Explorative_Tile Position API call starting");

        String featureName = clientJSON.get("featureName").asString();
        String id = clientJSON.get("id").asString();
        int level = clientJSON.get("level").asInt();

        PlaneManager<?> specificPlaneManager = PlaneHandler.getSpecificPlaneManager(featureName);
        JsonObject jsonObject = specificPlaneManager.getElementPosition(level, id);

        JsonObject batch = new JsonObject();
        batch.add("type", "explorative_position");
        batch.add("msg", jsonObject);
        printer.print("[");
        printer.println(batch.toString());
        printer.print("]");
        printer.flush();
        printer.close();
        break;
      }

        case "explorative_tiles": {

          LOGGER.debug("Explorative_Tiles API call starting");

          String featureName = clientJSON.get("featureName").asString();
          int level = clientJSON.get("level").asInt();
          JsonArray requested = clientJSON.get("requested").asArray();
          JsonArray response = new JsonArray();
          PlaneManager<?> specificPlaneManager = PlaneHandler.getSpecificPlaneManager(featureName);
          for (JsonValue jsonValue : requested.asArray()) {
            JsonObject xyObject = jsonValue.asObject();
            int x = xyObject.get("x").asInt();
            int y = xyObject.get("y").asInt();
            String img = specificPlaneManager.getSingleElement(level, x, y);
            String shotid = "";

            String representativeId = "";
            if (!img.isEmpty()){
              shotid = img.substring(img.indexOf("/")+1);
              representativeId = specificPlaneManager.getRepresentativeOfElement(img, level);
            }
            JsonObject singleTile = new JsonObject()
                .add("x", x)
                .add("y", y)
                .add("img", img)
                .add("representative", representativeId)
                .add("shotid", shotid);
            response.add(singleTile);
          }

          JsonObject batch = new JsonObject();
          batch.add("type", "explorative_tiles");
          batch.add("response", response);
          printer.print("[");
          printer.println(batch.toString());
          printer.print("]");
          printer.flush();
          printer.close();

          break;
        }

        case "explorative_tile_representative": {
          String featureName = clientJSON.get("featureName").asString();
          int level = clientJSON.get("level").asInt();
          String id = clientJSON.get("id").asString();
          PlaneManager<?> specificPlaneManager = PlaneHandler.getSpecificPlaneManager(featureName);
          String representativeId = specificPlaneManager.getRepresentativeOfElement(id, level);
          if (representativeId == null || representativeId.isEmpty()){
            throw new Exception("RepresentativeID is empty");
          }
          level++;
          JsonObject jsonObject = specificPlaneManager.getElementPosition(level, representativeId);

          JsonObject batch = new JsonObject();
          batch.add("type", "explorative_tile_representative");
          batch.add("msg", jsonObject);
          printer.print("[");
          printer.println(batch.toString());
          printer.print("]");
          printer.flush();
          printer.close();

          break;
        }

        case "getFeatureNames": {
          LOGGER.debug("Label API call starting");
          JsonArray jsonConcepts = new JsonArray();

//          File folder = new File("data/serialized/");
//          if(!folder.exists()){
//            break;
//          }
          //String[] processedFeatures = folder.list();
          Set<String> processedFeatures = PlaneHandler.getPlaneManagerNames();
          for(String featureName : processedFeatures){
//            if(!el.matches("plane_manager_[A-z0-9]*.ser")) continue;
//            String featureName = el.replace("plane_manager_", "").replace(".ser", "").toLowerCase();
            PlaneManager<?> specificPlaneManager = PlaneHandler.getSpecificPlaneManager(featureName);
            int topLevel = specificPlaneManager.getTopLevel();
            Position center = specificPlaneManager.getCenter();
            jsonConcepts.add(new JsonObject()
                .add("id", featureName)
                .add("text", featureName)
                .add("topLevel", topLevel)
                .add("x", center.getX())
                .add("y", center.getY())
                .add("identifier", specificPlaneManager.getSingleElement(topLevel, center.getX(), center.getY())));
          }
          _return.set("response", jsonConcepts);
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
