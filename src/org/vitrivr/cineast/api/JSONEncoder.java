package org.vitrivr.cineast.api;

import java.util.List;

import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
@Deprecated
public final class JSONEncoder {

	private JSONEncoder(){}
	
	public static final JsonObject encodeResult(String shotId, double score, String category, int containerId, boolean includeType){
		JsonObject _return = new JsonObject();
		if(includeType){
			_return.add("type", "result");
		}
		_return.add("shotid", shotId);
		_return.add("score", score);
		_return.add("category", category);
		_return.add("containerid", containerId); //TODO remove
		return _return;
	}
	
	public static final JsonObject encodeResult(String shotId, double score, String category, int containerId){
		return encodeResult(shotId, score, category, containerId, true);
	}
	
	public static final JsonObject encodeResultBatched(List<StringDoublePair> sdpList, String category, int containerId){
		JsonObject _return = new JsonObject();
		_return.add("type", "batch");
		_return.add("inner", "result");
		JsonArray array = new JsonArray();
		for(StringDoublePair sdp : sdpList){
			array.add(encodeResult(sdp.key, sdp.value, category, containerId, false));
		}
		_return.add("array", array);
		return _return;
	}
	
	public static final JsonObject encodeShot(String shotId, String videoId, long startFrame, long endFrame, boolean includeType){
		JsonObject _return = new JsonObject();
		if(includeType){
			_return.add("type", "shot");
		}
		_return.add("shotid", shotId);
		_return.add("videoid", videoId);
		_return.add("start",  startFrame);
		_return.add("end", endFrame);
		return _return;
	}
	
	public static JsonObject encodeShot(MediaSegmentDescriptor sd, boolean includeType){
		return encodeShot(sd.getSegmentId(), sd.getObjectId(), sd.getStart(), sd.getEnd(), includeType);
	}
	
	public static final JsonObject encodeShot(String shotId, String videoId, long startFrame, long endFrame){
		return encodeShot(shotId, videoId, startFrame, endFrame, true);
	}
	
	public static final JsonObject encodeShot(MediaSegmentDescriptor sd){
		return encodeShot(sd, true);
	}
	
	public static final JsonObject encodeShotBatch(Iterable<MediaSegmentDescriptor> sdList){
		JsonObject _return = new JsonObject();
		_return.add("type", "batch");
		_return.add("inner", "shot");
		JsonArray array = new JsonArray();
		for(MediaSegmentDescriptor sd : sdList){
			array.add(encodeShot(sd, false));
		}
		_return.add("array", array);
		return _return;
	}
	
	public static final JsonObject encodeVideo(String name, String videoId, String path, boolean includeType){
		JsonObject _return = new JsonObject();
		if(includeType){
			_return.add("type", "video");
		}
		_return.add("name", name);
		_return.add("videoid", videoId);
		_return.add("path", path);
		return _return;
	}
	
	
	
	public static final JsonObject encodeVideo(String name, String videoId, String path, int width, int height, long frames, double seconds){
		return encodeVideo(name, videoId, path, true);
	}
	

	
	public static final JsonObject encodeVideo(MediaObjectDescriptor vd, boolean includeType){
		return encodeVideo(vd.getName(), vd.getObjectId(), vd.getPath(), includeType);
	}
	
	public static final JsonObject encodeVideo(MediaObjectDescriptor vd){
		return encodeVideo(vd, true);
	}
	
	public static final JsonObject encodeVideoBatch(Iterable<MediaObjectDescriptor> vdList){
		JsonObject _return = new JsonObject();
		_return.add("type", "batch");
		_return.add("inner", "video");
		JsonArray array = new JsonArray();
		for(MediaObjectDescriptor vd : vdList){
			array.add(encodeVideo(vd, false));
		}
		_return.add("array", array);
		return _return;
	}
	
	public static final JsonObject encodeResultName(String resultName){
		JsonObject _return = new JsonObject();
		_return.add("type", "resultname");
		_return.add("name", resultName);
		return _return;
	}
	
}
