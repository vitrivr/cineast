package org.vitrivr.cineast.core.db.dao;

import static org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor.ENTITY;
import static org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor.FIELDNAMES;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.dao.reader.DatabaseLookupException;
import org.vitrivr.cineast.core.util.LogHelper;

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

  public MediaSegmentMetadataHandler() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get(),
        Config.sharedConfig().getDatabase().getWriterSupplier().get());
  }


  public boolean addDescriptor(MediaSegmentMetadataDescriptor descriptor){
    if (descriptor == null){
      return false;
    }

    return this.writer.persist(this.writer.generateTuple(descriptor));

  }

  public List<MediaSegmentMetadataDescriptor> getDescriptors(String segmentId) {
    if (segmentId == null || segmentId.isEmpty()){
      return Collections.emptyList();
    }

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows(FIELDNAMES[0], segmentId);
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

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }
}
