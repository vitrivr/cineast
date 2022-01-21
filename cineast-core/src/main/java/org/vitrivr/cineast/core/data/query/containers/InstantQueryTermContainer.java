package org.vitrivr.cineast.core.data.query.containers;

import com.google.common.base.MoreObjects;
import java.time.Instant;
import java.util.Optional;
import org.vitrivr.cineast.core.data.GpsData;

/**
 * A query based on similarity to an {@link Instant} in time.
 */
public class InstantQueryTermContainer extends AbstractQueryTermContainer {

  private final Instant instant;

  /**
   * Constructs an {@link InstantQueryTermContainer} from string data
   *
   * @param data The string data that should be converted.
   */
  public InstantQueryTermContainer(String data) {
    this(GpsData.parseInstant(data).orElseThrow(() -> new IllegalArgumentException("The provided JSON data did not contain valid GPS information.")));
  }

  /**
   * Constructs an {@link InstantQueryTermContainer} from an {@link Instant}
   *
   * @param instant {@link Instant} from which to construct the {@link InstantQueryTermContainer}
   */
  public InstantQueryTermContainer(Instant instant) {
    this.instant = instant;
  }

  public static InstantQueryTermContainer of(Instant instant) {
    return new InstantQueryTermContainer(instant);
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
