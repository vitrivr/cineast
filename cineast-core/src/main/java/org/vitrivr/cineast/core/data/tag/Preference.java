package org.vitrivr.cineast.core.data.tag;

public enum Preference {
  MUST("must"), COULD("could"), NOT("not");

  private String preference;

  private Preference(String s) {
    preference = s;
  }

  public String toString() {
    return this.preference;
  }

  public String getPreference() {
    return this.preference;
  }
}
