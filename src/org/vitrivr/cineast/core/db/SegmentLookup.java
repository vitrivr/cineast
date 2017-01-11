package org.vitrivr.cineast.core.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.setup.EntityCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentLookup {

	private static final Logger LOGGER = LogManager.getLogger();
	private final DBSelector selector;
	
	public SegmentLookup(){
		this.selector = Config.getDatabaseConfig().getSelectorSupplier().get();
		this.selector.open(EntityCreator.CINEAST_SEGMENT);
	}
	
	public void close(){
		this.selector.close();
	}
	
	public SegmentDescriptor lookUpShot(String segmentId){
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows("id", segmentId);
		
		if(results.isEmpty()){
			return new SegmentDescriptor();
		}
		
		Map<String, PrimitiveTypeProvider> map = results.get(0);
		
		return mapToDescriptor(map);
		
	}

	private SegmentDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map) {
		PrimitiveTypeProvider idProvider = map.get("id");
		PrimitiveTypeProvider mmobjidProvider = map.get("multimediaobject");
		PrimitiveTypeProvider sequenceProvider = map.get("sequencenumber");
		PrimitiveTypeProvider startProvider = map.get("segmentstart");
		PrimitiveTypeProvider endProvider = map.get("segmentend");
		
		if(idProvider == null){
			LOGGER.error("no id in segment");
			return new SegmentDescriptor();
		}
		
		if(idProvider.getType() != ProviderDataType.STRING){
			LOGGER.error("invalid data type for field id in segment, expected string, got {}", idProvider.getType());
		}
		
		if(mmobjidProvider == null){
			LOGGER.error("no multimediaobject in segment");
			return new SegmentDescriptor();
		}
		
		if(mmobjidProvider.getType() != ProviderDataType.STRING){
			LOGGER.error("invalid data type for field multimediaobject in segment, expected string, got {}", mmobjidProvider.getType());
			return new SegmentDescriptor();
		}
		
		if(sequenceProvider == null){
			LOGGER.error("no sequencenumber in segment");
			return new SegmentDescriptor();
		}
		
		if(sequenceProvider.getType() != ProviderDataType.INT){
			LOGGER.error("invalid data type for field sequencenumber in segment, expected int, got {}", sequenceProvider.getType());
			return new SegmentDescriptor();
		}
		
		if(startProvider == null){
			LOGGER.error("no segmentstart in segment");
			return new SegmentDescriptor();
		}
		
		if(startProvider.getType() != ProviderDataType.INT){
			LOGGER.error("invalid data type for field segmentstart in segment, expected int, got {}", startProvider.getType());
			return new SegmentDescriptor();
		}
		
		if(endProvider == null){
			LOGGER.error("no segmentend in segment");
			return new SegmentDescriptor();
		}
		
		if(endProvider.getType() != ProviderDataType.INT){
			LOGGER.error("invalid data type for field segmentend in segment, expected int, got {}", endProvider.getType());
			return new SegmentDescriptor();
		}
		
		return new SegmentDescriptor(mmobjidProvider.getString(), idProvider.getString(), sequenceProvider.getInt(), startProvider.getInt(), endProvider.getInt());
	}
	
	public Map<String, SegmentDescriptor> lookUpShots(String...ids){
		
		if(ids == null || ids.length == 0){
			return new HashMap<>();
		}
		
		HashMap<String, SegmentDescriptor> _return = new HashMap<>();
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows("id", ids);
		
		if(results.isEmpty()){
			return new HashMap<>();
		}
		
		for(Map<String, PrimitiveTypeProvider> map : results){
			SegmentDescriptor d = mapToDescriptor(map);
			_return.put(d.getSegmentId(), d);
		}
		
		return _return;
	}
	
	public Map<String, SegmentDescriptor> lookUpShots(Iterable<String> ids){
	  if(ids == null){
      return new HashMap<>();
    }
    
    HashMap<String, SegmentDescriptor> _return = new HashMap<>();
    
    List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows("id", ids);
    
    if(results.isEmpty()){
      return new HashMap<>();
    }
    
    for(Map<String, PrimitiveTypeProvider> map : results){
      SegmentDescriptor d = mapToDescriptor(map);
      _return.put(d.getSegmentId(), d);
    }
    
    return _return;
	}

	public List<SegmentDescriptor> lookUpAllSegments(String objectId){
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows("multimediaobject", objectId);
		
		if(results.isEmpty()){
			return new ArrayList<>(0);
		}
		
		ArrayList<SegmentDescriptor> _return = new ArrayList<>(results.size());
		
		for(Map<String, PrimitiveTypeProvider> map : results){
			SegmentDescriptor descriptor = mapToDescriptor(map);
			if(descriptor.exists()){
				_return.add(descriptor);
			}
		}
		
		return _return;
	}


	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
