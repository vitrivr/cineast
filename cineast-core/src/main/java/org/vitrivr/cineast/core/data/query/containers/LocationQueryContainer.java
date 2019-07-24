package org.vitrivr.cineast.core.data.query.containers;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.Location;

import com.google.common.base.MoreObjects;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

public class LocationQueryContainer extends QueryContainer {

    /** The {@link Location} object contained in this {@link LocationQueryContainer}. */
    private final Location location;

    /**
     * Constructs an {@link LocationQueryContainer} from JSON data.
     *
     * @param json The JSON data that should be converted.
     */
    public LocationQueryContainer(String json) {
        final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
        final JsonNode jsonNode = jsonProvider.toJsonNode(json);
        if (jsonNode != null) {
            this.location = GpsData.parseLocationFromJson(jsonNode).orElseThrow(() -> new IllegalArgumentException("The provided JSON data did not contain valid GPS information."));
        } else {
            throw new IllegalArgumentException("Failed to parse the provided JSON data.");
        }
    }

    /**
     * Constructs an {@link LocationQueryContainer} from a {@link Location} object.
     *
     * @param location The JSON data that should be converted.
     */
    public LocationQueryContainer(Location location) {
        this.location = location;
    }

    public static LocationQueryContainer of(Location location) {
        return new LocationQueryContainer(location);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("location", getLocation()).toString();
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.of(location);
    }
}
