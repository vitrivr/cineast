package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
