package org.vitrivr.cineast.core.data;

import java.util.EnumSet;
import java.util.HashMap;

import gnu.trove.map.hash.TIntObjectHashMap;

public enum MediaType {

  VIDEO(0, "v", "video");

  private final int id;
  private final String prefix, name;

  /**
   * 
   * @param id
   *          numeric id to use to identify the type in the persistent storage layer
   * @param prefix
   *          the prefix of the MediaType without a trailing delimiter such as '_'
   * @param name
   *          the name of the media type
   */
  MediaType(int id, String prefix, String name) {
    this.id = id;
    this.prefix = prefix;
    this.name = name.trim();
  }

  public int getId() {
    return this.id;
  }

  /**
   * @return the prefix of the MediaType without a trailing delimiter such as '_'
   */
  public String getPrefix() {
    return this.prefix;
  }

  public String getName() {
    return this.name;
  }

  private static final TIntObjectHashMap<MediaType> idToType = new TIntObjectHashMap<>();
  private static final HashMap<String, MediaType> prefixToType = new HashMap<>();
  private static final HashMap<String, MediaType> nameToType = new HashMap<>();
  
  public static final char DELIMITER = '_';

  static {
    for (MediaType t : EnumSet.allOf(MediaType.class)) {
      if (idToType.containsKey(t.getId())) {
        throw new IllegalStateException("duplicate id (" + t.getId() + ") in Mediatype: " + t
            + " collides with " + idToType.get(t.getId()));
      }
      idToType.put(t.getId(), t);

      if (prefixToType.containsKey(t.getPrefix())) {
        throw new IllegalStateException("duplicate prefix (" + t.getPrefix() + ") in Mediatype: "
            + t + " collides with " + prefixToType.get(t.getPrefix()));
      }
      prefixToType.put(t.getPrefix(), t);

      if (nameToType.containsKey(t.getPrefix())) {
        throw new IllegalStateException("duplicate name (" + t.getName() + ") in Mediatype: " + t
            + " collides with " + nameToType.get(t.getName()));
      }
      nameToType.put(t.getName().toLowerCase(), t);
    }
  }

  /**
   * @return the MediaType associated with this id or null in case there is none
   */
  public static final MediaType fromId(int id) {
    return idToType.get(id);
  }

  /**
   * @return the MediaType associated with this prefix or null in case there is none
   */
  public static final MediaType fromPrefix(String prefix) {
    if (prefix == null) {
      return null;
    }
    return prefixToType.get(prefix);
  }

  /**
   * @return the MediaType associated with this name or null in case there is none
   */
  public static final MediaType fromName(String name) {
    if (name == null) {
      return null;
    }
    return nameToType.get(name.trim().toLowerCase());
  }

  public static final boolean existsId(int id) {
    return idToType.containsKey(id);
  }

  public static final boolean existsPrefix(String prefix) {
    return prefixToType.containsKey(prefix);
  }

  public static final boolean existsName(String name) {
    return nameToType.containsKey(name.trim().toLowerCase());
  }
  
  
  /**
   * generates an id of the form (prefix)_(object id) assuming the delimiter is '_'
   * @param type the type for which an id is to be generated
   * @param objectId the globally unique id of the object
   * @throws IllegalArgumentException if objectId is empty
   * @throws NullPointerException if type or objectId is null
   */
  public static final String generateId(MediaType type, String objectId) throws IllegalArgumentException, NullPointerException{
    if(type == null){
      throw new NullPointerException("type cannot be null");
    }
    if(objectId == null){
      throw new NullPointerException("object id cannot be null");
    }
    if(objectId.isEmpty()){
      throw new IllegalArgumentException("sequenceNumber must be non-negative");
    }
    
    return type.getPrefix() + DELIMITER + objectId;
    
  }
  
  /**
   * generates an id of the form (prefix)_(object id)_(sequence number) assuming the delimiter is '_'
   * @param type the type for which an id is to be generated
   * @param objectId the globally unique id of the object
   * @param sequenceNumber the number of the segment within the object
   * @throws IllegalArgumentException if shot sequence number is negative or objectId is empty
   * @throws NullPointerException if type or objectId is null
   */
  public static final String generateId(MediaType type, String objectId, long sequenceNumber) throws IllegalArgumentException, NullPointerException{
    if(sequenceNumber < 0){
      throw new IllegalArgumentException("sequenceNumber must be non-negative");
    }
    
    return generateId(type, objectId) + DELIMITER + sequenceNumber;
    
  }
}
