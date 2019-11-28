package org.vitrivr.cineast.standalone.cli;


import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Index.IndexType;
import com.github.rvesse.airline.annotations.Command;
import java.util.HashSet;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * A CLI command that can be used to setup all indices for the entities.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "create-index", description = "Creates indices for all entities specified in the config")
public class CreateIndicesCommand implements Runnable {

  @Override
  public void run() {
    final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
    if (!(ec instanceof CottontailEntityCreator)) {
      System.err.println("Index creation currently only supported for cottontail");
    }

    final CottontailEntityCreator ctec = (CottontailEntityCreator) ec;

    if (ctec != null) {
      /* Collects all the relevant retriever classes based on the application config. */
      final HashSet<Retriever> retrievers = new HashSet<>();
      for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
        retrievers.addAll(Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keySet());
      }

      System.out.println("Setting up basic entities...");

      ctec.createIndex(MediaObjectDescriptor.ENTITY, MediaObjectDescriptor.FIELDNAMES[0], IndexType.HASH_UQ);
      ctec.createIndex(MediaObjectMetadataDescriptor.ENTITY, MediaObjectMetadataDescriptor.FIELDNAMES[0], IndexType.HASH);
      ctec.createIndex(MediaSegmentMetadataDescriptor.ENTITY, MediaSegmentMetadataDescriptor.FIELDNAMES[0], IndexType.HASH);
      ctec.createIndex(MediaSegmentDescriptor.ENTITY, MediaSegmentDescriptor.FIELDNAMES[0], IndexType.HASH_UQ);
      ctec.createIndex(MediaSegmentDescriptor.ENTITY, MediaSegmentDescriptor.FIELDNAMES[1], IndexType.HASH);
      ctec.createIndex(TagReader.TAG_ENTITY_NAME, "id", IndexType.HASH_UQ);
      ctec.createIndex(TagReader.TAG_ENTITY_NAME, TagReader.TAG_NAME_COLUMNNAME, IndexType.HASH);

      System.out.println("...done");

      System.out.println("Optimizing");
      OptimizeEntitiesCommand.optimizeAllCottontailEntities();

      System.out.println("Index creation complete!");
      /* Closes the EntityCreator. */
      ec.close();
    }
  }
}
