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

import java.util.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.regex.*;

public class MessageTemplate {

  private final String template;
  private static final Pattern KEY_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

  public MessageTemplate(String path) throws IOException {
    this.template = Files.readString(Paths.get(path));
  }

  public List<String> getKeys() {
    List<String> keys = new ArrayList<>();
    Matcher matcher = KEY_PATTERN.matcher(template);
    while (matcher.find()) {
      keys.add(matcher.group(1));
    }
    return keys;
  }

  public String formatString(Map<String, String> values) {
    if (!getKeys().stream().allMatch(values::containsKey)) {
      throw new IllegalArgumentException("Not all keys are present in the provided map.");
    }
    String result = template;
    for (String key : values.keySet()) {
      result = result.replace("${" + key + "}", values.get(key));
    }
    return result;
  }

  private String getNextPlaceholder(int start) {
    int nextMatchStart = template.indexOf("${", start);
    if (nextMatchStart != -1) {
      return template.substring(nextMatchStart, template.indexOf("}", nextMatchStart) + 1);
    }
    return null;
  }

  public Map<String, String> parseString(String input) {
    input = input.strip();
    Map<String, String> values = new HashMap<>();
    List<String> keys = getKeys();

    Matcher matcher = KEY_PATTERN.matcher(template);
    int lastEnd = 0;
    int i = 0;

    while (matcher.find()) {
      String beforeKey = template.substring(lastEnd, matcher.start());
      lastEnd = matcher.end();

      if (!input.startsWith(beforeKey)) {
        throw new IllegalArgumentException("Input does not match template");
      }

      input = input.substring(beforeKey.length());
      String nextPlaceholder = getNextPlaceholder(lastEnd);

      String value;
      if (nextPlaceholder != null && input.contains(nextPlaceholder)) {
        value = input.substring(0, input.indexOf(nextPlaceholder));
        input = input.substring(value.length());
      } else {
        if (lastEnd < template.length()) {
          String trailingTemplate = template.substring(lastEnd);
          if (input.endsWith(trailingTemplate)) {
            value = input.substring(0, input.length() - trailingTemplate.length());
          } else {
            throw new IllegalArgumentException("Input does not match template");
          }
        } else {
          value = input;
        }
      }

      if (i < keys.size()) {
        values.put(keys.get(i), value);
        i++;
      }
    }

    if (keys.size() != values.size()) {
      throw new IllegalArgumentException("Keys and values count do not match");
    }

    return values;
  }

}