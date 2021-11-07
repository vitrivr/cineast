package org.vitrivr.cineast.core.data.query.containers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import java.util.Optional;

public class LocationQueryTermContainer extends AbstractQueryTermContainer {

    /** The {@link Location} object contained in this {@link LocationQueryTermContainer}. */
    private final Location location;

    /**
     * Constructs an {@link LocationQueryTermContainer} from JSON data.
     *
     * @param json The JSON data that should be converted.
     */
    public LocationQueryTermContainer(String json) {
        final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
        final JsonNode jsonNode = jsonProvider.toJsonNode(json);
        if (jsonNode != null) {
            this.location = GpsData.parseLocationFromJson(jsonNode).orElseThrow(() -> new IllegalArgumentException("The provided JSON data did not contain valid GPS information."));
        } else {
            throw new IllegalArgumentException("Failed to parse the provided JSON data.");
        }
    }

    /**
     * Constructs an {@link LocationQueryTermContainer} from a {@link Location} object.
     *
     * @param location The JSON data that should be converted.
     */
    public LocationQueryTermContainer(Location location) {
        this.location = location;
    }

    public static LocationQueryTermContainer of(Location location) {
        return new LocationQueryTermContainer(location);
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
