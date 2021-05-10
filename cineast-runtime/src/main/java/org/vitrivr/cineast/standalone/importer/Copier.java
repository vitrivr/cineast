package org.vitrivr.cineast.standalone.importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.monitoring.ImportTaskMonitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Copies data from an {@link Importer} to a {@link PersistencyWriter}
 */
public class Copier implements AutoCloseable {

    private final String entityName;
    private final Importer<?> importer;
    private final PersistencyWriter<?> writer;
    private static final Logger LOGGER = LogManager.getLogger();

    public Copier(String entityName, Importer<?> importer) {
        this(entityName, importer, Config.sharedConfig().getDatabase().getWriterSupplier().get());
    }

    public Copier(String entityName, Importer<?> importer, PersistencyWriter<?> writer) {
        this.entityName = entityName;
        this.importer = importer;
        this.writer = writer;
    }

    public void copy() {
        this.copyFrom(this.importer);
    }

    public void copyFrom(Importer<?> importer) {
        Map<String, PrimitiveTypeProvider> map = importer.readNextAsMap();

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
        } while ((map = importer.readNextAsMap()) != null);

    }

    private void persistTuple(PersistentTuple tuple) {
        this.writer.persist(tuple);
    }

    public void copyBatched(int batchSize) {
        this.copyBatchedFrom(batchSize, this.importer);
    }

    public void copyBatchedFrom(int batchSize, Importer<?> importer) {

        if (batchSize <= 0) {
            copy();
            return;
        }

        Map<String, PrimitiveTypeProvider> map = importer.readNextAsMap();

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

        long start = System.currentTimeMillis();
        ImportTaskMonitor.reportImportProgress(0, entityName, 0);
        do {
            for (i = 0; i < names.length; ++i) {
                objects[i] = PrimitiveTypeProvider.getObject(map.get(names[i]));
            }
            PersistentTuple tuple = this.writer.generateTuple(objects);
            tupleCache.add(tuple);
            if (tupleCache.size() >= batchSize) {
                this.writer.persist(tupleCache);
                long stop = System.currentTimeMillis();
                LOGGER.trace("Inserted {} elements in {} ms", tupleCache.size(), stop-start);
                ImportTaskMonitor.reportImportProgress(tupleCache.size(), entityName, stop - start);
                tupleCache.clear();
                start = System.currentTimeMillis();
            }

        } while ((map = importer.readNextAsMap()) != null);

        this.writer.persist(tupleCache);
        long stop = System.currentTimeMillis();
        LOGGER.trace("Inserted {} elements in {} ms", tupleCache.size(), stop-start);
        ImportTaskMonitor.reportImportProgress(tupleCache.size(), entityName, stop - start);

    }


    @Override
    public void close() {
        this.writer.close();
    }
}
