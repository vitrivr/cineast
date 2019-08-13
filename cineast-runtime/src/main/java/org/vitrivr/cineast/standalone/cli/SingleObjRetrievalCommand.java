package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.RetrievalRuntimeConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;


/**
 * @author Silvan Heller
 * @version 1.0
 */
@Command(name = "retrieve-single", description = "Retrieves all information from the database for a given segment / object.")
public class SingleObjRetrievalCommand implements Runnable {

    @Option(name = {"-s", "--segmentid"}, title = "Segment ID", description = "The ID of the segment to use an example for retrieval.")
    private String segmentId;

    public void run() {
        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

        System.out.println("Retrieving all columns named 'feature' for segment " + segmentId);
        System.out.println("Errors during DB-Lookup are most probably due to that specific feature not storing its information in the 'feature' column");

        final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());

        RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();

        retrievalRuntimeConfig.getRetrieverCategories().forEach(cat -> retrievalRuntimeConfig.getRetrieversByCategory(cat).forEachEntry((retriever, weight) -> {
            if (retriever.getTableName().isEmpty()) {
                return true;
            }
            System.out.println("Retrieving for feature: " + retriever.getClass().getSimpleName());
            selector.open(retriever.getTableName().get());
            selector.getFeatureVectorsGeneric("id", segmentId, "feature").forEach(res -> System.out.println(retriever.getTableName().get() + ": " + res));
            return true;
        }));

        System.out.println("Done");
    }
}
