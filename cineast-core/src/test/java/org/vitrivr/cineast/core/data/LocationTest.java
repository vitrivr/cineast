package org.vitrivr.cineast.core.data;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocationTest {
  private static final float COORDINATES_DELTA = 1e-5f;

  @Test
  @DisplayName("Null Island")
  public void testNullIsland() {
    assertFixedCoordinates(0f, 0f);
  }

  @Test
  @DisplayName("Positive Values")
  public void testPositiveValues() {
    assertFixedCoordinates(47.23f, 7.34f);
  }

  @Test
  @DisplayName("Negative Values")
  public void testNegativeValues() {
    assertFixedCoordinates(-13.163077f, -72.5473746f);
  }

  @Test
  @DisplayName("Latitude Clamping")
  public void testLatitudeClamping() {
    assertNormalizedCoordinates(90f, 0f, 123f, 0f);
    assertNormalizedCoordinates(-90f, 0f, -123f, 0f);
  }

  @Test
  @DisplayName("Longitude Wrapping")
  public void testLongitudeWrapping() {
    assertNormalizedCoordinates(0f, 0f, 0f, 360f);
    assertNormalizedCoordinates(0f, -135f, 0f, 225f);
    assertNormalizedCoordinates(0f, -180f, 0f, 180f);
  }

  @Test
  @DisplayName("Invalid Float Array")
  public void testInvalidFloatArray() {
    List<float[]> invalidArrays = ImmutableList
        .of(new float[] {}, new float[]{ 0f }, new float[]{ 0f, 1f, 2f });
    for (float[] array : invalidArrays) {
      assertThrows(IllegalArgumentException.class, () -> Location.of(array));
    }
  }

  @Test
  @DisplayName("NaN Values")
  public void testNanValues() {
    assertThrows(IllegalArgumentException.class, () -> Location.of(Float.NaN, 0f));
    assertThrows(IllegalArgumentException.class, () -> Location.of(0f, Float.NaN));
  }

  private static void assertFixedCoordinates(float latitude, float longitude) {
    for (Location location : getTestLocations(latitude, longitude)) {
      assertLocationEquals(latitude, longitude, location);
    }
  }

  private static void assertNormalizedCoordinates(float expectedLat, float expectedLng, float actualLat, float actualLng) {
    for (Location location : getTestLocations(actualLat, actualLng)) {
      assertLocationEquals(expectedLat, expectedLng, location);
    }
  }

  private static List<Location> getTestLocations(float latitude, float longitude) {
    return ImmutableList.of(
        Location.of(latitude, longitude),
        Location.of(new float[] { latitude, longitude })
    );
  }

  private static void assertLocationEquals(float expectedLatitude, float expectedLongitude, Location actual) {
    assertEquals(expectedLatitude, actual.getLatitude(), COORDINATES_DELTA,
        "Latitude of Location did not match expected");
    assertEquals(expectedLongitude, actual.getLongitude(), COORDINATES_DELTA,
        "Longitude of Location did not match expected");

    assertEquals(expectedLatitude, actual.getElement(0), COORDINATES_DELTA,
        "First element of Location did not match expected");
    assertEquals(expectedLongitude, actual.getElement(1), COORDINATES_DELTA,
        "Second element of Location did not match expected");
  }
}
