package org.vitrivr.cineast.core.db.polypheny;

import io.grpc.netty.NettyChannelBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A wrapper class that exposes the Polypheny DB JDBC {@link Connection} used by Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public final class PolyphenyWrapper implements AutoCloseable {

    /** {@link Logger} used by this PolyphenyWrapper. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Name of the cineast schema in Polypheny DB. */
    public static final String CINEAST_SCHEMA = "cineast";

    /** Store name PostgreSQL instances. */
    public static final String STORE_NAME_POSTGRESQL = "hsqldb";

    /** Store name Cottontail DB instances. */
    public static final String STORE_NAME_COTTONTAIL = "cottontail";

    /** The JDBC {@link Connection} used to communicate with Polypheny DB. */
    final Connection connection;

    public PolyphenyWrapper(DatabaseConfig config) {
        StopWatch watch = StopWatch.createStarted();
        LOGGER.debug("Starting to connect to Polypheny DB at {}:{}", config.getHost(), config.getPort());
        final NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(config.getHost(), config.getPort());
        if (config.getPlaintext()) {
            builder.usePlaintext();
        }

        /* Try to instantiate Polypheny driver. */
        try {
           Class.forName( "org.polypheny.jdbc.Driver" ); /* Make sure, driver was loaded. */
           final Properties properties = new Properties();
           properties.put("username","pa"); /* TODO: Could be configurable :-) */
           this.connection = DriverManager.getConnection(String.format("jdbc:polypheny:http://%s/", config.getHost()), properties);
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Failed to initialize JDBC connection to Polypheny DB due to error: " + e.getMessage());
        }

        watch.stop();
        LOGGER.debug("Connected to Polypheny DB in {} ms at {}:{}", watch.getTime(TimeUnit.MILLISECONDS), config.getHost(), config.getPort());
    }

    public String fqnInput(String entity) {
        return CINEAST_SCHEMA + "." + entity;
    }

    @Override
    public void close(){
        try {
            LOGGER.debug("Closing JDBC connection to Polypheny DB.");
            this.connection.close();
        } catch (SQLException e) {
            LOGGER.error("Closing JDBC connection to Polypheny DB failed: {}", e.getMessage());
        }
    }
}
