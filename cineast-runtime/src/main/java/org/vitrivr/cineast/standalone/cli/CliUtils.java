package org.vitrivr.cineast.standalone.cli;

import com.carrotsearch.hppc.procedures.ObjectDoubleProcedure;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.features.RangeBooleanRetriever;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.config.RetrievalRuntimeConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

/**
 * A collection of util methods used by multiple cli classes
 */
public class CliUtils {

  public static void printInfoForObject(String objectId, DBSelector selector) {
    System.out.println("= Retrieving object information for " + objectId + " =");
    try (var objectReader = new MediaObjectReader(selector)) {
      System.out.println(objectReader.lookUpObjectById(objectId));
    }
    System.out.println("= Retrieving object metadata for =");
    try (var reader = new MediaObjectMetadataReader(selector)) {
      List<MediaObjectMetadataDescriptor> metadataDescriptors = reader.lookupMultimediaMetadata(objectId);
      metadataDescriptors.forEach(System.out::println);
    }
  }

  public static void printInfoForSegment(String segmentId, DBSelector selector, String _filterCategory, boolean printObjInfo) {

    System.out.println("= Retrieving segment information for " + segmentId + "=");
    try (var segmentReader = new MediaSegmentReader(selector)) {
      Optional<MediaSegmentDescriptor> segmentDescriptor = segmentReader.lookUpSegment(segmentId);
      segmentDescriptor.ifPresent(System.out::println);
      segmentDescriptor.ifPresent(descriptor -> {
        if (printObjInfo) {
          printInfoForObject(descriptor.getObjectId(), selector);
        }
      });
    }

    System.out.println("= Retrieving segment metadata =");
    try (var reader = new MediaSegmentMetadataReader(selector)) {
      reader.lookupMultimediaMetadata(segmentId).forEach(System.out::println);
    }

    System.out.println("Retrieving all columns for segment " + segmentId);
    RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();

    retrievalRuntimeConfig.getRetrieverCategories().forEach(cat -> {
      if (_filterCategory != null) {
        if (!cat.equals(_filterCategory)) {
          return;
        }
      }
      retrievalRuntimeConfig.getRetrieversByCategory(cat).forEach((ObjectDoubleProcedure<? super Retriever>) (retriever, weight) -> {
        System.out.println("= Retrieving for feature: " + retriever.getClass().getSimpleName() + " =");
        retriever.getEntityNames().forEach(tableName -> {
          selector.open(tableName);
          List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
          if (retriever.getClass() == RangeBooleanRetriever.class) {
            rows = selector.getRows("segmentid", new StringTypeProvider(segmentId));
          }
          rows.forEach(row -> {
            System.out.println("== New row == ");
            row.forEach((key, value) -> System.out.println(tableName + "." + key + " - " + value));
          });
        });
      });
    });

    System.out.println("Done");
  }

  public static void retrieveAndLog(List<Retriever> retrievers, ContinuousRetrievalLogic retrieval, int limit, boolean printDetail, AbstractQueryTermContainer qc) {
    System.out.println("Only printing the first " + limit + " results, change with --limit parameter");
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    retrievers.forEach(retriever -> {
      AtomicBoolean entityExists = new AtomicBoolean(true);
      retriever.getEntityNames().forEach(table -> {
        if (!selector.existsEntity(table)) {
          System.out.println("Entity " + table + " does not exist");
          entityExists.set(false);
        }
      });
      if (!entityExists.get()) {
        System.out.println("Not retrieving for " + retriever.getClass().getSimpleName() + " because entity does not exist");
        return;
      }
      System.out.println("Retrieving for " + retriever.getClass().getSimpleName());
      long start = System.currentTimeMillis();
      List<SegmentScoreElement> results = retrieval.retrieveByRetriever(qc, retriever, new ConstrainedQueryConfig().setMaxResults(limit).setResultsPerModule(limit));
      long stop = System.currentTimeMillis();
      System.out.println("Results for " + retriever.getClass().getSimpleName() + ":, retrieved in " + (stop - start) + "ms");

      for (SegmentScoreElement e : results) {
        System.out.print(e.getSegmentId());
        System.out.print(": ");
        System.out.println(e.getScore());
        if (printDetail) {
          CliUtils.printInfoForSegment(e.getSegmentId(), selector, null, true);
        }
      }
      System.out.println();
    });
  }
}
