package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object to store a list of string IDs.
 *
 * @author lucaro
 * @created 10.05.17
 */
public class IdList {

  /**
   * List of String IDs to be stored by this object.
   */
  private List<String> ids;

  /**
   * Constructor for the IdList object. Creates the internal list representation from the given
   * array.
   *
   * @param ids Array of IDs.
   */
  public IdList(String[] ids) {
    this.ids = Arrays.asList(ids);
  }

  /**
   * Constructor for the IdList object. Creates a new list instance from the given list.
   *
   * @param ids List of IDs.
   */
  @JsonCreator
  public IdList(@JsonProperty("ids") List<String> ids) {
    this.ids = new ArrayList<>(ids);
  }

  /**
   * Getter for array of IDs.
   *
   * @return Array of String
   */
  public String[] getIds() {
    return this.ids.toArray(new String[0]);
  }

  /**
   * Getter for list of IDs.
   *
   * @return List of String
   */
  @JsonIgnore
  public List<String> getIdList() {
    return this.ids;
  }

  @Override
  public String toString() {
    return "IdList{" +
        "ids=" + Arrays.toString(ids.toArray()) +
        '}';
  }
}
