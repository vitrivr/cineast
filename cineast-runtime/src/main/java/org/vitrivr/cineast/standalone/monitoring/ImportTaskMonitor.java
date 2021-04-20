package org.vitrivr.cineast.standalone.monitoring;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * @author silvan on 19.11.18.
 */
public class ImportTaskMonitor {

    private static boolean initalized = false;
    private static Summary executionTime;
    private static Summary batchImportTime;
    private static Gauge importProgress;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        if (initalized) {
            LOGGER.info("Already initalized, Returning");
            return;
        }
        if (!Config.sharedConfig().getMonitoring().enablePrometheus) {
            LOGGER.warn("Prometheus not enabled, returning");
            return;
        }
        LOGGER.info("Initalizing Prometheus Extraction Task Monitor");
        executionTime = Summary.build().name("cineast_importtask")
                .help("Time for import of 1 item")
                .labelNames("Taskname").quantile(0.5, 0.05).quantile(0.9, 0.01).register();
        batchImportTime = Summary.build().name("cineast_batchinserttime")
                .help("Time for import of items on entity")
                .labelNames("Entity", "Items").quantile(0.5, 0.05).quantile(0.9, 0.01).register();
        importProgress = Gauge.build().name("cineast_importgrogress").register();
    }

    /**
     * You can call this method without worrying if prometheus support is enabled
     */
    public static void reportExecutionTime(String name, long milliseconds) {
        if (executionTime != null) {
            executionTime.labels(name).observe(milliseconds);
        }
    }

    /**
     * You can call this method without worrying if prometheus support is enabled
     */
    public static void reportImportProgress(int insertedTuples, String entityName, long executiontime) {
        if (importProgress != null && batchImportTime != null) {
            importProgress.labels(entityName).inc(insertedTuples);
            batchImportTime.labels(entityName, String.valueOf(insertedTuples)).observe(executiontime);
        }
    }
}
