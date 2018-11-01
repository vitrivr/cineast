package org.vitrivr.cineast.core.importer;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.IntTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.decode.shotboundary.TrecvidMasterShotReferenceDecoder;
@Deprecated
public class TRECVIDShotBoundaryImporter implements Importer<MediaSegmentDescriptor> {

  private Iterator<MediaSegmentDescriptor> iter;
  
  public TRECVIDShotBoundaryImporter(File msr, String videoId){
    this.iter = TrecvidMasterShotReferenceDecoder.decode(msr, videoId).iterator();
  }
  
  @Override
  public MediaSegmentDescriptor readNext() {
    if(this.iter.hasNext()){
      return this.iter.next();
    }
    return null;
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(MediaSegmentDescriptor data) {
    HashMap<String, PrimitiveTypeProvider> _return = new HashMap<>(5);
    _return.put("id", new StringTypeProvider(data.getSegmentId()));
    _return.put("multimediaobject", new StringTypeProvider(data.getObjectId()));
    _return.put("sequencenumber", new IntTypeProvider(data.getSequenceNumber()));
    _return.put("segmentstart", new IntTypeProvider(data.getStart()));
    _return.put("segmentend", new IntTypeProvider(data.getEnd()));
    return _return;
  }

}
