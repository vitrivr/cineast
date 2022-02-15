package org.vitrivr.cineast.core.db;

import java.io.File;
import java.util.function.Supplier;
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

/**
 * Enumaration of all {@link DataSource}s available to Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public enum DataSource {
  NONE,
  JSON,
  COTTONTAIL,
  POLYPHENY,
  INMEMORY,

  @Deprecated
  ADAMPRO;

  /**
   * Returns a new {@link PersistencyWriterSupplier}
   *
   * @param config The {@link DatabaseConfig} to use.
   * @return Resulting {@link PersistencyWriterSupplier}
   */
  public PersistencyWriterSupplier getWriterSupplier(DatabaseConfig config) {
    switch (this) {
      case NONE:
        return NoDBWriter::new;
      case COTTONTAIL:
        return () -> new CottontailWriter(new CottontailWrapper(config.getHost(), config.getPort()), config.getBatchsize());
      case POLYPHENY:
        return () -> new PolyphenyWriter(new PolyphenyWrapper(config.getHost(), config.getPort()), config.getBatchsize());
      case JSON:
        return () -> new JsonFileWriter(new File(config.getHost()));
      case ADAMPRO:
        return () -> new ADAMproWriter(new ADAMproWrapper(config));
      default:
        throw new IllegalStateException("No supplier for " + this + " selector.");
    }
  }

  /**
   * Returns a new {@link DBSelectorSupplier}
   *
   * @param config The {@link DatabaseConfig} to use.
   * @return Resulting {@link DBSelectorSupplier}
   */
  public DBSelectorSupplier getSelectorSupplier(DatabaseConfig config) {
    switch (this) {
      case NONE:
        return NoDBSelector::new;
      case COTTONTAIL:
        return () -> new CottontailSelector(new CottontailWrapper(config.getHost(), config.getPort()));
      case POLYPHENY:
        return () -> new PolyphenySelector(new PolyphenyWrapper(config.getHost(), config.getPort()));
      case JSON:
        return () -> new JsonSelector(new File(config.getHost()));
      case ADAMPRO:
        return () -> new ADAMproSelector(new ADAMproWrapper(config));
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
      case COTTONTAIL:
        return () -> new CottontailEntityCreator(new CottontailWrapper(config.getHost(), config.getPort()));
      case POLYPHENY:
        return () -> new PolyphenyEntityCreator(new PolyphenyWrapper(config.getHost(), config.getPort()));
      case ADAMPRO:
        return () -> new ADAMproEntityCreator(new ADAMproWrapper(config));
      default:
        throw new IllegalStateException("No supplier for " + this + " entity creator.");
    }
  }
}

