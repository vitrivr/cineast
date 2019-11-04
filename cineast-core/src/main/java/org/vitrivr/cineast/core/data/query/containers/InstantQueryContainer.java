package org.vitrivr.cineast.core.data.query.containers;

import com.google.common.base.MoreObjects;
import org.vitrivr.cineast.core.data.GpsData;

import java.time.Instant;
import java.util.Optional;

public class InstantQueryContainer extends QueryContainer {
    private final Instant instant;

    /**
     * Constructs an {@link InstantQueryContainer} from string data
     *
     * @param data The string data that should be converted.
     */
    public InstantQueryContainer(String data) {
        this(GpsData.parseInstant(data).orElseThrow(() -> new IllegalArgumentException("The provided JSON data did not contain valid GPS information.")));
    }

    /**
     * Constructs an {@link InstantQueryContainer} from an {@link Instant}
     *
     * @param instant {@link Instant} from which to construct the {@link InstantQueryContainer}
     */
    public InstantQueryContainer(Instant instant) {
        this.instant = instant;
    }

    public static InstantQueryContainer of(Instant instant) {
        return new InstantQueryContainer(instant);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("instant", getInstant()).toString();
    }

    @Override
    public Optional<Instant> getInstant() {
        return Optional.of(this.instant);
    }
}
