package org.vitrivr.cineast.api.session;

import java.util.HashMap;
import java.util.Set;

public class SessionManager {

  private SessionManager() {
  }

  private static HashMap<String, Session> sessions = new HashMap<>();

  public static Session newSession(int lifetime) {
    Session s;
    synchronized (sessions) {
      do {
        s = new Session(lifetime);
      } while (sessions.containsKey(s.getSessionId()));
      sessions.put(s.getSessionId(), s);
    }
    return s;
  }
  
  public static void endSession(String sessionId){
    synchronized (sessions) {
     sessions.remove(sessionId); 
    }
  }
  
  public static Session get(String sessionId){
    synchronized (sessions) {
      return sessions.get(sessionId);
    }
  }

  public static void cleanup() {
    synchronized (sessions) {
      Set<String> keys = sessions.keySet();
      for (String key : keys) {
        if (!sessions.get(key).isValid()) {
          sessions.remove(key);
        }
      }
    }
  }

}
