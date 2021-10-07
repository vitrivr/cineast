package org.vitrivr.cineast.standalone.cli.db;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.SplittableRandom;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;

/**
 * Runs a Polypheny DB benchmark based on a feature category.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Command(name = "polypheny-benchmark", description = "Drop a specific table")
public class PolyphenyBenchmarkCommand implements Runnable {


    @Option(name = {"--host"}, description = "The host IP or name where Polypheny DB runs.")
    private String host = "localhost";

    @Option(name = {"--limit"}, description = "The number of features to retrieve (the k in kNN).")
    private int limit = 500;

    @Option(name = {"--repeat"}, description = "The number of repetitions.")
    private int repeat = 5;

    @Option(name = {"--table"}, description = "The feature table to benchmark.")
    @Required
    private String table;

    /** The JDBC {@link Connection} used to communicate with Polypheny DB. */
    private Connection connection;

    /** The dimensionality */
    private int dimensionality = 0;

    /** {@link SplittableRandom} used to generate random query vectors. */
    private final SplittableRandom random = new SplittableRandom();

    private boolean prepare() {
        /* Try to instantiate Polypheny driver. */
        try {
            if (this.connection == null) {
                Class.forName( "org.polypheny.jdbc.Driver" ); /* Make sure, driver was loaded. */
                final Properties properties = new Properties();
                properties.put("username","pa"); /* TODO: Could be configurable :-) */
                this.connection = DriverManager.getConnection(String.format("jdbc:polypheny:http://%s/", this.host), properties);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Preparation failed because Cineast failed to initialize JDBC connection to Polypheny DB due to an error: " + e.getMessage());
            return false;
        }

        try (final PreparedStatement stmt = this.connection.prepareStatement("SELECT * FROM cineast." + table + " LIMIT 1")) {
            try (final ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.println("Preparation failed because Cineast failed to fetch an example row form cineast." + table + "; table seems to be empty.");
                    return false;
                }
                final Object array = rs.getArray("feature").getArray();
                if (array instanceof float[]) {
                    this.dimensionality = ((float[]) array).length;
                } else {
                    System.err.println("Preparation failed because feature column of cineast." + table + " does not seem to be a float array.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Preparation failed because Cineast failed to fetch an example row form cineast. "  + table + "; due to an error: " + e.getMessage());
            return false;
        }
        return true;
    }


    @Override
    public void run() {
        /* Run preparation. */
        if (!this.prepare()) return;

        /* Warmup. */
        //this.executeTraditional(this.randomVector());
        this.executeJoin(this.randomVector());

        /* Executes workloads. */
        double duration_traditional_ms = 0.0;
        double duration_join_ms = 0.0;

        for (int r = 0; r < this.repeat; r++) {
            final float[] query = this.randomVector();
            duration_traditional_ms += (this.executeTraditional(query) / 1000.0);
            duration_join_ms += (this.executeJoin(query) / 1000.0);
        }
        System.out.println("Traditional workload on 'cineast." + this.table + "' (d=" + this.dimensionality + ") took " + (duration_traditional_ms/this.repeat) + "s on average (" + this.repeat + " repetitions).");
        System.out.println("JOIN workload on 'cineast." + this.table + "' (d=" + this.dimensionality + ") took " + (duration_join_ms/this.repeat) + "s on average (" + this.repeat + " repetitions).");
    }

    /**
     * Executes a traditional Cineast workload, which first fetches the required features using NNS and
     * then fetches the multimedia object and segments in a second query.
     */
    private long executeTraditional(float[] query) {
        final LinkedList<String> segmentids = new LinkedList<>();
        final LinkedList<String> objectids = new LinkedList<>();
        final HashSet<String> names = new HashSet<>();
        final long start = System.currentTimeMillis();

        /* 1: Perform NNS. */
        try (final PreparedStatement statement = this.connection.prepareStatement("SELECT id, distance(feature," + toVectorString(query) + ",'L2') as dist FROM cineast." + this.table + " ORDER BY dist ASC LIMIT " + this.limit)) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    segmentids.add(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error occurred during query execution in: " + e.getMessage());
            return -1L;
        }

        /* 2: Fetch all segments. */
        try (final PreparedStatement statement = this.prepareInStatement("cineast.cineast_segment", "segmentid", segmentids)) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    objectids.add(rs.getString("objectid"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error occurred during query execution in: " + e.getMessage());
            return -1L;
        }

        /* 3: Fetch all objects. */
        try (final PreparedStatement statement = this.prepareInStatement("cineast.cineast_multimediaobject", "objectid", objectids)) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error occurred during query execution in: " + e.getMessage());
            return -1L;
        }

        return System.currentTimeMillis() - start;
    }

    /**
     * Executes a JOIN-based Cineast workload, which first fetches the required features using NNS and
     * then fetches the multimedia object and segments in a second query.
     */
    private long executeJoin(float[] query) {
        final HashSet<String> names = new HashSet<>();
        final long start = System.currentTimeMillis();

        /* 1: Perform NNS. */
        try (final PreparedStatement statement = this.connection.prepareStatement(""
                + "SELECT * FROM (SELECT id, distance(feature," + toVectorString(query) + ",'L2') AS dist FROM cineast." + this.table + " ORDER BY dist ASC LIMIT " + this.limit + ") as feature "
                + "INNER JOIN cineast.cineast_segment AS segment ON (feature.id = segment.segmentid) "
                + "INNER JOIN cineast.cineast_multimediaobject AS object ON (segment.objectid = object.objectid)")) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error occurred during query execution in: " + e.getMessage());
            return -1L;
        }

        return System.currentTimeMillis() - start;
    }

    /**
     * Generates and returns a new random query.
     *
     * @return Float array representing a new random query.
     */
    private float[] randomVector() {
        /* Prepare query vector. */
        final float[] vector = new float[this.dimensionality];
        for (int i = 0; i< vector.length; i++) {
            vector[i] = (float)this.random.nextDouble();
        }
        return vector;
    }

    /**
     * Converts a flat vector to a string representation usable by Polypheny DB.
     *
     * @param vector {@link Distance} The float vector to convert.
     * @return The resulting name.
     */
    private static String toVectorString(float[] vector) {
        final StringBuilder arrayString = new StringBuilder("ARRAY[");
        int i = 0;
        for (float v : vector) {
            if (i++ > 0) arrayString.append(",");
            arrayString.append(v);
        }
        arrayString.append("]");
        return arrayString.toString();
    }

    /**
     * Prepares a prepared statement {@link PreparedStatement} for a query with a single IN predicate.
     *
     * @param fieldName Name of the field that should be queried.
     * @param values Values to use in query.
     * @return {@link PreparedStatement}
     * @throws SQLException
     */
    private PreparedStatement prepareInStatement(String table, String fieldName, Iterable<String> values) throws SQLException {
        /* Prepare query (apparently, JDBC doesn't support value binding for IN predicates).*/
        final StringBuilder stringStatement = new StringBuilder("SELECT * FROM " + table + " WHERE " + fieldName + " IN (");
        int index = 0;
        for (String v : values) {
            if (index++ > 0) stringStatement.append(",");
            stringStatement.append("'");
            stringStatement.append(v);
            stringStatement.append("'");
        }
        stringStatement.append(")");

        return this.connection.prepareStatement(stringStatement.toString());
    }
}
