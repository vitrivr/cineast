package org.vitrivr.cineast.core.db.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.dao.reader.DatabaseLookupException;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.Closeable;
import java.util.*;

import static org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor.ENTITY;
import static org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor.FIELDNAMES;

public class MediaSegmentMetadataHandler implements Closeable {

  private final PersistencyWriter<?> writer;
  private final DBSelector selector;

  private static final Logger LOGGER = LogManager.getLogger();

  public MediaSegmentMetadataHandler(DBSelector selector, PersistencyWriter<?> writer) {
    this.selector = selector;
    this.writer = writer;

    if (this.selector == null) {
      throw new NullPointerException("selector cannot be null");
    }

    if (this.writer == null) {
      throw new NullPointerException("writer cannot be null");
    }

    this.selector.open(ENTITY);
    this.writer.open(ENTITY);
    this.writer.setFieldNames(FIELDNAMES);
  }




  public boolean addDescriptor(MediaSegmentMetadataDescriptor descriptor){
    if (descriptor == null){
      return false;
    }

    return this.writer.persist(this.toPersistenTuple(descriptor));

  }

  public boolean addDescriptors(Collection<MediaSegmentMetadataDescriptor> descriptors){

    if (descriptors == null){
      return false;
    }

    if(descriptors.isEmpty()){
      return true;
    }

    ArrayList<PersistentTuple> tuples = new ArrayList<>(descriptors.size());

    for(MediaSegmentMetadataDescriptor descriptor : descriptors){
      tuples.add(this.toPersistenTuple(descriptor));
    }

    return this.writer.persist(tuples);

  }

  public List<MediaSegmentMetadataDescriptor> getDescriptors(String segmentId) {
    if (segmentId == null || segmentId.isEmpty()){
      return Collections.emptyList();
    }

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows(FIELDNAMES[0], new StringTypeProvider(segmentId));
    ArrayList<MediaSegmentMetadataDescriptor> _return = new ArrayList<>(rows.size());
    for (Map<String, PrimitiveTypeProvider> row : rows) {
      MediaSegmentMetadataDescriptor d = null;
      try {
        d = new MediaSegmentMetadataDescriptor(row);
      } catch (DatabaseLookupException e) {
        LOGGER.error(LogHelper.getStackTrace(e));
      }
      if (d != null) {
        _return.add(d);
      }
    }
    return _return;
  }

  @Override
  public void close() {
    this.selector.close();
    this.writer.close();
  }


  private PersistentTuple toPersistenTuple(MediaSegmentMetadataDescriptor descriptor){
    return this.writer.generateTuple(descriptor.getSegmentId(), descriptor.getDomain(), descriptor.getKey(), descriptor.getValue());
  }
}
