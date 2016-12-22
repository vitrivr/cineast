package org.vitrivr.cineast.core.data;

import java.util.EnumSet;
import java.util.HashMap;

import gnu.trove.map.hash.TIntObjectHashMap;

public enum MediaType {

  VIDEO(0, "v");
  
  
  private final int id;
  private final String prefix;
  
  /**
   * 
   * @param id numeric id to use to identify the type in the persistent storage layer
   * @param prefix the prefix of the MediaType without a trailing delimiter such as '_'
   */
  MediaType(int id, String prefix){
    this.id = id;
    this.prefix = prefix;
  }
  
  public int getId(){
    return this.id;
  }
  
  /**
   * @return the prefix of the MediaType without a trailing delimiter such as '_'
   */
  public String getPrefix(){
    return this.prefix;
  }
  
  
  
  private static final TIntObjectHashMap<MediaType> idToType = new TIntObjectHashMap<>();
  private static final HashMap<String, MediaType> prefixToType = new HashMap<>();
  
  static{
    for(MediaType t : EnumSet.allOf(MediaType.class)){
      idToType.put(t.getId(), t);
      prefixToType.put(t.getPrefix(), t);
    }
  }
  
  /**
   * @return the MediaType associated with this id or null in case there is none
   */
  public static final MediaType fromId(int id){
    return idToType.get(id);
  }
  
  /**
   * @return the MediaType associated with this prefix or null in case there is none
   */
  public static final MediaType fromPrefix(String prefix){
    if(prefix == null){
      return null;
    }
    return prefixToType.get(prefix);
  }
  
  public static final boolean existsId(int id){
    return idToType.containsKey(id);
  }
  
  public static final boolean existsPrefix(String prefix){
    return prefixToType.containsKey(prefix);
  }
}
