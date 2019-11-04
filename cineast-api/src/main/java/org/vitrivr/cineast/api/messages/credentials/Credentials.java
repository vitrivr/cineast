package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Credentials {

private String username, password; //more options to come
  
  @JsonCreator
  public Credentials(@JsonProperty("username") String username, @JsonProperty("password") String password){
    this.username = username;
    this.password = password;
  }
  
  public String getUsername(){
    return this.username;
  }
  
  public String getPassword(){
    return this.password;
  }

  @Override
  public String toString() {
    return String.format("Credentials [username=%s, password=%s]", username, password);
  }
  
}
