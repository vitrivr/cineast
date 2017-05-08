package org.vitrivr.cineast.api.session;

import java.util.UUID;

public class Session {

  private final String id;
  private long validUntil;
  
  /**
   * 
   * @param id unique session id
   * @param lifetime session life time in seconds
   */
  protected Session(String id, long lifetime){
    this.id = id;
    this.validUntil = (System.currentTimeMillis() / 1000) + lifetime;
  }
  
  protected Session(long lifetime){
    this(UUID.randomUUID().toString(), lifetime);
  }
  
  public boolean isValid(){
    return this.validUntil >= (System.currentTimeMillis() / 1000);
  }
  
  public String getSessionId(){
    return this.id;
  }
  
  public void extendLifeTime(int seconds){
    this.validUntil += seconds;
  }
  
  public void setLifeTime(int seconds){
    this.validUntil = (System.currentTimeMillis() / 1000) + seconds;
  }
  
  public long getRemainingLifeTime(){
    return this.validUntil - (System.currentTimeMillis() / 1000);
  }
  
  public long getEndTimeStamp(){
    return this.validUntil;
  }
  
}
