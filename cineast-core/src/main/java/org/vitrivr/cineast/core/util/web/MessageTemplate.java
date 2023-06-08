package org.vitrivr.cineast.core.util.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTemplate {

  private final String template;

  public MessageTemplate(String path) throws IOException {
    this.template = Files.readString(new File(path).toPath());
  }

  public List<String> getKeys(){
    List<String> keys = new ArrayList<>();
    Matcher matcher = Pattern.compile("\\$\\{(.*?)\\}").matcher(template);
    while(matcher.find()){
      keys.add(matcher.group(1));
    }
    return keys;
  }


  public String formatString(Map<String,String> values){
    // if not all keys are present, throw exception
    for(String key : getKeys()){
      if(!values.containsKey(key)){
        throw new IllegalArgumentException("Missing key " + key);
      }
    }
    String result = template;
    for(String key : values.keySet()){
      result = result.replace("${" + key + "}", values.get(key));
    }
    return result;
  }

  public Map<String,String> parseString(String input){
    Map<String,String> values = new HashMap<>();

    String[] parts = template.split("\\$\\{.*?\\}");
    String[] valueParts = new String[parts.length - 1];

    for (int i = 0; i < parts.length - 1; i++) {
      input = input.substring(parts[i].length());
      valueParts[i] = input.substring(0, input.indexOf(parts[i+1]));
    }

    List<String> keys = getKeys();
    for (int i = 0; i < keys.size(); i++) {
      values.put(keys.get(i), valueParts[i]);
    }

    return values;
  }



}
