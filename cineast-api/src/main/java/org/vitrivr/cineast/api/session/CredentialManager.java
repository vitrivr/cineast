package org.vitrivr.cineast.api.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.vitrivr.cineast.core.data.messages.credentials.Credentials;
import org.vitrivr.cineast.core.util.LogHelper;

public class CredentialManager {

  private CredentialManager(){}
  
  private static final Logger LOGGER = LogManager.getLogger();
  private static final File storedUserFile = new File("users"); //TODO move to config
  
  private static HashMap<String, User> userMap = new HashMap<>();
  
  static{
    if(storedUserFile.exists()){
      try {
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(storedUserFile));
        @SuppressWarnings("unchecked")
        HashMap<String, User> users = (HashMap<String, User>) oin.readObject();
        userMap.putAll(users);
        oin.close();
      } catch (ClassNotFoundException | IOException e) {
        LOGGER.error("could not load user file: {}", LogHelper.getStackTrace(e));
      }
      
    }
  }
  
  public static SessionType authenticate(Credentials credentials){
    if(credentials == null || credentials.getUsername() == null){
      LOGGER.error("cannot authenticate when credentials are null");
      return SessionType.UNAUTHENTICATED;
    }
    
    User user = userMap.get(credentials.getUsername());
    
    if(user == null){
      LOGGER.error("cannot authenticate unknown user '{}'", credentials.getUsername());
      return SessionType.UNAUTHENTICATED;
    }
    
    if(!user.checkPassword(credentials.getPassword())){
      LOGGER.error("cannot authenticate user '{}', wrong password", credentials.getUsername());
      return SessionType.UNAUTHENTICATED;
    }
    
    if(user.isAdmin){
      LOGGER.info("authenticated admin user '{}'", credentials.getUsername());
      return SessionType.ADMIN;
    }
    
    LOGGER.info("authenticated user '{}'", credentials.getUsername());
    return SessionType.USER;
    
  }
  
  private static void storeUsers(){
    try {
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storedUserFile));
      oos.writeObject(userMap);
      oos.flush();
      oos.close();
    } catch (IOException e) {
      LOGGER.error("could not store user file: {}", LogHelper.getStackTrace(e));
    }
  }
  
  public static boolean createUser(String username, String password, boolean admin){
    if(username == null || password == null){
      LOGGER.error("cannot create user username or password null");
      return false;
    }
    if(userMap.containsKey(username)){
      LOGGER.error("user '{}' already exists", username);
      return false;
    }
    
    User u = User.newUser(username, password, admin);
    userMap.put(username, u);
    storeUsers();
    LOGGER.info("created user '{}'", username);
    return true;
    
  }
  
  private static class User implements Serializable{

    private static final long serialVersionUID = -7409429664837688894L;
    public final String username, passwordHash;
    public final boolean isAdmin;
    
    private User(String username, String passwordHash, boolean isAdmin){
      this.username = username;
      this.passwordHash = passwordHash;
      this.isAdmin = isAdmin;
    }
    
    static User newUser(String username, String password, boolean isAdmin){
      return new User(username, BCrypt.hashpw(password, BCrypt.gensalt(12)), isAdmin);
    }
    
    boolean checkPassword(String password){
      if(password == null){
        return false;
      }
      return BCrypt.checkpw(password, this.passwordHash);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (isAdmin ? 1231 : 1237);
      result = prime * result + ((passwordHash == null) ? 0 : passwordHash.hashCode());
      result = prime * result + ((username == null) ? 0 : username.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      User other = (User) obj;
      if (isAdmin != other.isAdmin) {
        return false;
      }
      if (passwordHash == null) {
        if (other.passwordHash != null) {
          return false;
        }
      } else if (!passwordHash.equals(other.passwordHash)) {
        return false;
      }
      if (username == null) {
        if (other.username != null) {
          return false;
        }
      } else if (!username.equals(other.username)) {
        return false;
      }
      return true;
    }
    
  }
  
}
