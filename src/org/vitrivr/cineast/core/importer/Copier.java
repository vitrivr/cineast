package org.vitrivr.cineast.core.importer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * Copies data from an {@link Importer} to a {@link PersistencyWriter}
 */
public class Copier {

  private final String entityName;
  private final Importer<?> importer;
  private final PersistencyWriter<?> writer;

  public Copier(String entityName, Importer<?> importer) {
    this(entityName, importer, Config.sharedConfig().getDatabase().getWriterSupplier().get());
  }

  public Copier(String entityName, Importer<?> importer, PersistencyWriter<?> writer) {
    this.entityName = entityName;
    this.importer = importer;
    this.writer = writer;
  }

  public void copy() {
    Map<String, PrimitiveTypeProvider> map = this.importer.readNextAsMap();

    if (map == null) {
      return;
    }

    Set<String> keyset = map.keySet();
    String[] names = new String[keyset.size()];

    int i = 0;
    for (String name : keyset) {
      names[i++] = name;
    }

    this.writer.open(entityName);
    this.writer.setFieldNames(names);

    Object[] objects = new Object[names.length];

    do {
      for (i = 0; i < names.length; ++i) {
        objects[i] = PrimitiveTypeProvider.getObject(map.get(names[i]));
      }
      persistTuple(this.writer.generateTuple(objects));
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } while ((map = this.importer.readNextAsMap()) != null);

    this.writer.close();
  }

  private void persistTuple(PersistentTuple tuple) {
    this.writer.persist(tuple);
  }

  public void copyBatched(int batchSize) {

    if (batchSize <= 0) {
      copy();
      return;
    }

    Map<String, PrimitiveTypeProvider> map = this.importer.readNextAsMap();

    if (map == null) {
      return;
    }

    Set<String> keyset = map.keySet();
    String[] names = new String[keyset.size()];

    int i = 0;
    for (String name : keyset) {
      names[i++] = name;
    }

    this.writer.open(entityName);
    this.writer.setFieldNames(names);

    Object[] objects = new Object[names.length];

    ArrayList<PersistentTuple> tupleCache = new ArrayList<>(batchSize);

    do {
      for (i = 0; i < names.length; ++i) {
        objects[i] = PrimitiveTypeProvider.getObject(map.get(names[i]));
      }
      PersistentTuple tuple = this.writer.generateTuple(objects);
      tupleCache.add(tuple);
      if (tupleCache.size() >= batchSize) {
        this.writer.persist(tupleCache);
        tupleCache.clear();
      }

    } while ((map = this.importer.readNextAsMap()) != null);

    this.writer.persist(tupleCache);

    this.writer.close();
  }

  @Override
  protected void finalize() throws Throwable {
    this.writer.close();
    super.finalize();
  }

}
