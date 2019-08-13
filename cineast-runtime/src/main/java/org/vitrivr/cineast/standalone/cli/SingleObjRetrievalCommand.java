package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.RetrievalRuntimeConfig;


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

        System.out.println("Retrieving all columns for segment " + segmentId);

        RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();

        retrievalRuntimeConfig.getRetrieverCategories().forEach(cat -> retrievalRuntimeConfig.getRetrieversByCategory(cat).forEachEntry((retriever, weight) -> {
            System.out.println("= Retrieving for feature: " + retriever.getClass().getSimpleName() + " =");
            retriever.getTableNames().forEach(tableName -> {
                selector.open(tableName);
                selector.getRows("id", segmentId).forEach(row -> {
                    System.out.println("== New Row == ");
                    row.entrySet().forEach(entry -> {
                        System.out.println(tableName + "." + entry.getKey() + " - " + entry.getValue());
                    });
                });
            });
            return true;
        }));

        System.out.println("Done");
    }
}
