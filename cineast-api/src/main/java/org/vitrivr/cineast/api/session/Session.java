package org.vitrivr.cineast.api.session;

import java.util.UUID;

public class Session {

  private final String id;
  private final SessionType type;
  private long validUntil;

  /**
   * 
   * @param id
   *          unique session id
   * @param lifetime
   *          session life time in seconds
   */
  protected Session(String id, SessionType type, long lifetime) {
    if(id == null){
      throw new NullPointerException("session id cannot be null");
    }
    if(type == null){
      throw new NullPointerException("session type cannot be null");
    }
    this.id = id;
    this.type = type;
    this.validUntil = (System.currentTimeMillis() / 1000) + lifetime;
  }

  protected Session(SessionType type, long lifetime) {
    this(UUID.randomUUID().toString(), type, lifetime);
  }

  public boolean isValid() {
    return this.validUntil >= (System.currentTimeMillis() / 1000);
  }

  public String getSessionId() {
    return this.id;
  }

  public void extendLifeTime(int seconds) {
    this.validUntil += seconds;
  }

  public void setLifeTime(int seconds) {
    this.validUntil = (System.currentTimeMillis() / 1000) + seconds;
  }

  public long getRemainingLifeTime() {
    return this.validUntil - (System.currentTimeMillis() / 1000);
  }

  public long getEndTimeStamp() {
    return this.validUntil;
  }
  
  public SessionType getSessionType(){
    return this.type;
  }

}
