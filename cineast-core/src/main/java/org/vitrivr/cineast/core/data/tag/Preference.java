package org.vitrivr.cineast.core.data.tag;

public enum Preference {
  MUST("must"), COULD("could"), NOT("not");

  private String preference;

  Preference(String s) {
    if (s.isEmpty()) {
      throw new IllegalArgumentException("Preference " + s + " cannot be null");
    }
    preference = s;
  }

  public String toString() {
    return this.preference;
  }

  public String getPreference() {
    return this.preference;
  }
}
