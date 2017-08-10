package org.vitrivr.cineast.core.data.query.containers;

public class TextQueryContainer extends QueryContainer {

  private final String text;
  
  public TextQueryContainer(String text){
    this.text = text == null ? "" : text;
  }
  
  @Override
  public String getText(){
    return this.text;
  }
  
}
