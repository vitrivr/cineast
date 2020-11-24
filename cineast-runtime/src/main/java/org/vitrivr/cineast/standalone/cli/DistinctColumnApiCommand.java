package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;
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

  @Option(name = {"--limit"}, title = "Limit", description = "Minimum occurences to be printed")
  private int limit = -1;

  @Override
  public void run() {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    selector.open(table);
    long start = System.currentTimeMillis();
    Map<String, Integer> distinct = selector.countDistinctValues(column);
    long stop = System.currentTimeMillis();
    System.out.println("Retrieved distinct elements in " + (stop - start) + " ms");
    System.out.println("Printing distinct elements for " + table + "." + column);
    AtomicInteger dynamicLimit = new AtomicInteger(limit);
    distinct.entrySet().stream()
        .filter(el -> el.getValue() >= dynamicLimit.get())
        .sorted(Comparator.comparingInt((ToIntFunction<Entry<String, Integer>>) Entry::getValue).reversed())
        .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    selector.close();
  }
}
