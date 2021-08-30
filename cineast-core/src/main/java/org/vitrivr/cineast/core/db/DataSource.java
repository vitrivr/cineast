package org.vitrivr.cineast.core.db;

import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.adampro.ADAMproEntityCreator;
import org.vitrivr.cineast.core.db.adampro.ADAMproSelector;
import org.vitrivr.cineast.core.db.adampro.ADAMproWrapper;
import org.vitrivr.cineast.core.db.adampro.ADAMproWriter;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWriter;
import org.vitrivr.cineast.core.db.json.JsonFileWriter;
import org.vitrivr.cineast.core.db.json.JsonSelector;
import org.vitrivr.cineast.core.db.memory.InMemoryEntityCreator;
import org.vitrivr.cineast.core.db.polypheny.PolyphenyEntityCreator;
import org.vitrivr.cineast.core.db.polypheny.PolyphenySelector;
import org.vitrivr.cineast.core.db.polypheny.PolyphenyWrapper;
import org.vitrivr.cineast.core.db.polypheny.PolyphenyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.NoEntityCreator;

import java.io.File;
import java.util.function.Supplier;

/**
 * Enumaration of all {@link DataSource}s available to Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public enum DataSource {
    NONE,
    JSON,
    ADAMPRO,
    COTTONTAIL,
    POLYPHENY,
    INMEMORY;

    /**
     * Returns a new {@link Supplier} for an {@link PersistencyWriter}
     *
     * @param config The {@link DatabaseConfig} to use.
     * @return Resulting {@link Supplier<PersistencyWriter>}
     */
    public Supplier<PersistencyWriter<?>> getWriterSupplier(DatabaseConfig config) {
        switch (this) {
            case NONE:
                return NoDBWriter::new;
            case ADAMPRO:
                return () -> new ADAMproWriter(new ADAMproWrapper(config));
            case COTTONTAIL:
                return () -> new CottontailWriter(new CottontailWrapper(config));
            case POLYPHENY:
                return () -> new PolyphenyWriter(new PolyphenyWrapper(config));
            case JSON:
                return () -> new JsonFileWriter(new File(config.getHost()));
            default:
                throw new IllegalStateException("No supplier for " + this + " selector.");
        }
    }

    /**
     * Returns a new {@link Supplier} for a {@link DBSelector}
     *
     * @param config The {@link DatabaseConfig} to use.
     * @return Resulting {@link Supplier<DBSelector>}
     */
    public Supplier<DBSelector> getSelectorSupplier(DatabaseConfig config) {
        switch (this) {
            case NONE:
                return NoDBSelector::new;
            case ADAMPRO:
                return () -> new ADAMproSelector(new ADAMproWrapper(config));
            case COTTONTAIL:
                return () -> new CottontailSelector(new CottontailWrapper(config));
            case POLYPHENY:
                return () -> new PolyphenySelector(new PolyphenyWrapper(config));
            case JSON:
                return () -> new JsonSelector(new File(config.getHost()));
            default:
                throw new IllegalStateException("No supplier for " + this + " selector.");
        }
    }

    /**
     * Returns a new {@link Supplier} for an {@link EntityCreator}
     *
     * @param config The {@link DatabaseConfig} to use.
     * @return Resulting {@link Supplier<EntityCreator>}
     */
    public Supplier<EntityCreator> getEntityCreatorSupplier(DatabaseConfig config) {
        switch (this) {
            case NONE:
                return NoEntityCreator::new;
            case INMEMORY:
                return InMemoryEntityCreator::new;
            case ADAMPRO:
                return () -> new ADAMproEntityCreator(new ADAMproWrapper(config));
            case COTTONTAIL:
                return () -> new CottontailEntityCreator(new CottontailWrapper(config));
            case POLYPHENY:
                return () -> new PolyphenyEntityCreator(new PolyphenyWrapper(config));
            default:
                throw new IllegalStateException("No supplier for " + this + " entity creator.");
        }
    }
}

