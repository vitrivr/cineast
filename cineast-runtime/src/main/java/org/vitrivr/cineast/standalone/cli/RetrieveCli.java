package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.listener.RetrievalResultCSVExporter;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.List;


/**
 * A CLI command that can be used to retrieve data from the database based on an example segment.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "retrieve", description = "Retrieves objects from the database using and example object.")
public class RetrieveCli extends CineastCli {
    @Option(name = { "-s", "--segmentid" }, title = "Segment ID", description = "The ID of the segment to use an example for retrieval.")
    private String segmentId;

    @Option(name = { "-k", "--category" }, title = "Category", description = "Name of the feature category to retrieve.")
    private String category;

    @Option(name = { "-e", "--export" }, title = "Export", description = "Indicates whether the results should be exported. Defaults to false.")
    private boolean export = false;

    public void run() {
        super.run();
        final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());
        if (export) {
            retrieval.addRetrievalResultListener(new RetrievalResultCSVExporter(Config.sharedConfig().getDatabase()));
        }
        final List<SegmentScoreElement> results = retrieval.retrieve(this.segmentId, this.category, QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery()));
        System.out.println("results:");
        for (SegmentScoreElement e : results) {
            System.out.print(e.getSegmentId());
            System.out.print(": ");
            System.out.println(e.getScore());
        }
        System.out.println();
    }
}
