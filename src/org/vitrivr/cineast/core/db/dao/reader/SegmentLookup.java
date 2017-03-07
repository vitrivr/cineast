package org.vitrivr.cineast.core.db.dao.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.db.DBSelector;

public class SegmentLookup extends AbstractEntityReader {
    /**
     * Default constructor.
     */
	public SegmentLookup(){
		this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
	}

    /**
     * Constructor for SegmentLookup
     *
     * @param selector DBSelector to use for the MultimediaMetadataReader instance.
     */
	public SegmentLookup(DBSelector selector) {
	    super(selector);
        this.selector.open(SegmentDescriptor.ENTITY);
    }
	
	public SegmentDescriptor lookUpShot(String segmentId){
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(SegmentDescriptor.FIELDNAMES[0], segmentId);
		
		if(results.isEmpty()){
			return new SegmentDescriptor();
		}
		
		Map<String, PrimitiveTypeProvider> map = results.get(0);
		
		return mapToDescriptor(map);
		
	}

	private SegmentDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map) {
		PrimitiveTypeProvider idProvider = map.get(SegmentDescriptor.FIELDNAMES[0]);
		PrimitiveTypeProvider mmobjidProvider = map.get(SegmentDescriptor.FIELDNAMES[1]);
		PrimitiveTypeProvider sequenceProvider = map.get(SegmentDescriptor.FIELDNAMES[2]);
		PrimitiveTypeProvider startProvider = map.get(SegmentDescriptor.FIELDNAMES[3]);
		PrimitiveTypeProvider endProvider = map.get(SegmentDescriptor.FIELDNAMES[4]);
		PrimitiveTypeProvider startabsProvider = map.get(SegmentDescriptor.FIELDNAMES[5]);
		PrimitiveTypeProvider endabsProvider = map.get(SegmentDescriptor.FIELDNAMES[6]);


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

		if(startabsProvider.getType() == null) {
			LOGGER.error("No absolute startpoint found in segment.");
			return new SegmentDescriptor();
		}

		if(startabsProvider.getType() != ProviderDataType.FLOAT){
			LOGGER.error("Invalid data type for absolute startpoint in segment, expected float, got {}.", startabsProvider.getType());
			return new SegmentDescriptor();
		}

		if(endabsProvider.getType() == null) {
			LOGGER.error("No absolute endpoint found in segment.");
			return new SegmentDescriptor();
		}

		if(endabsProvider.getType() != ProviderDataType.FLOAT){
			LOGGER.error("Invalid data type for absolute endpoint in segment, expected float, got {}.", endabsProvider.getType());
			return new SegmentDescriptor();
		}

		return new SegmentDescriptor(mmobjidProvider.getString(), idProvider.getString(), sequenceProvider.getInt(), startProvider.getInt(), endProvider.getInt(), startabsProvider.getFloat(), endabsProvider.getFloat());
	}
	
	public Map<String, SegmentDescriptor> lookUpShots(String...ids){
		
		if(ids == null || ids.length == 0){
			return new HashMap<>();
		}
		
		HashMap<String, SegmentDescriptor> _return = new HashMap<>();
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(SegmentDescriptor.FIELDNAMES[0], ids);
		
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
    
    List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(SegmentDescriptor.FIELDNAMES[0], ids);
    
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
		
		List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(SegmentDescriptor.FIELDNAMES[1], objectId);
		
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
}
