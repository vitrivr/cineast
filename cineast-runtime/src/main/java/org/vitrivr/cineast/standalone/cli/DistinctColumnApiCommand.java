package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import java.util.Map;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;

@Command(name = "distinct-column", description = "Retrieves all distinct elements from the database for a given table and a given column")
public class DistinctColumnApiCommand implements Runnable {

    @Option(name = {"--table"}, title = "Table", description = "Table in the underlying database")
    @Required
    private String table;

    @Option(name = {"--column"}, title = "Column", description = "Column of the specified table")
    @Required
    private String column;

    @Override
    public void run() {
        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        selector.open(table);
        long start = System.currentTimeMillis();
        Map<String, Integer> distinct = selector.countDistinctValues(column);
        long stop = System.currentTimeMillis();
        System.out.println("Retrieved distinct elements in " + (stop - start) + " ms");
        System.out.println("Printing distinct elements for " + table + "." + column);
        distinct.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}
