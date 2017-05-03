package org.vitrivr.cineast.core.data;

import static org.junit.jupiter.api.Assertions.*;

import org.vitrivr.cineast.core.data.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LocationTest {
  @Test
  @DisplayName("Null Island")
  public void testNullIsland() {
    testLocationValues(0f, 0f, 0f, 0f);
  }

  @Test
  @DisplayName("Positive Values")
  public void testPositiveValues() {
    testLocationValues(47.23f, 7.34f, 47.23f, 7.34f);
  }

  @Test
  @DisplayName("Negative Values")
  public void testNegativeValues() {
    testLocationValues(-13.163077f, -72.5473746f, -13.163077f,-72.5473746f);
  }

  @Test
  @DisplayName("Latitude Clamping")
  public void testLatitudeClamping() {
    testLocationValues(90f, 0f, 123f, 0f);
    testLocationValues(-90f, 0f, -123f, 0f);
  }

  @Test
  @DisplayName("Longitude Wrapping")
  public void testLongitudeWrapping() {
    testLocationValues(0f, 0f, 0f, 360f);
    testLocationValues(0f, -135f, 0f, 225f);
    testLocationValues(0f, -180f, 0f, 180f);
  }

  private static void testLocationValues(float expectedLat, float expectedLng, float actualLat, float actualLng) {
    Location l = Location.of(actualLat, actualLng);
    assertEquals(expectedLat, l.getLatitude(), 1e-5f,
        "Parsed latitude from Location did not match expected");
    assertEquals(expectedLng, l.getLongitude(),1e-5f,
        "Parsed longitude from Location did not match expected");
  }
}
