package org.vitrivr.cineast.core.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.vitrivr.cineast.core.config.Config;

import com.eclipsesource.json.JsonObject;

public class JsonFileWriter extends AbstractPersistencyWriter<JsonObject> {

  private static File baseFolder = new File(Config.getExtractorConfig().getOutputLocation(), "json");
  private PrintWriter out;
  private boolean first = true;
 
  
  @Override
  public boolean open(String name) {
    baseFolder.mkdirs();
    try {
      this.out = new PrintWriter(new File(baseFolder, name + ".json"));
      this.out.println('[');
      return true;
    } catch (FileNotFoundException e) {
      return false;
    }
  }
  
  @Override
  public boolean close() {
    if(out == null){
      return true;
    }
    out.println(']');
    out.flush();
    out.close();
    out = null;
    return true;
  }
  
  @Override
  public boolean persist(PersistentTuple<JsonObject> tuple) {
    this.out.print(this.first ? "" : ",");
    this.out.println(tuple.getPersistentRepresentation().toString());
    this.out.flush();
    this.first = false;
    return true;
    
  }

  public static void setFolder(File outputFolder) {
    if(outputFolder == null){
      throw new NullPointerException("outputfolder cannot be null");
    }
    baseFolder = outputFolder;
    baseFolder.mkdirs();
  }

  @Override
  public boolean idExists(String id) {
    return false;
  }

  @Override
  public boolean exists(String key, String value) {
    return false;
  }

  @Override
  public PersistentTuple<JsonObject> generateTuple(Object... objects) {
    JsonTuple jt = new JsonTuple();
    for(Object o : objects){
      if(o != null){
        jt.addElement(o);
      }
    }
    return jt;
  }

  
  @Override
protected void finalize() throws Throwable {
	close();
	super.finalize();
}


class JsonTuple extends PersistentTuple<JsonObject>{

    @Override
    public JsonObject getPersistentRepresentation() {
      
      int nameIndex = 0;
      
      JsonObject _return = new JsonObject();
      
      for(Object o : this.elements){
        if(o instanceof float[]){
          _return.add(names[nameIndex++], Arrays.toString((float[])o));
        }else if(o instanceof int[]){
          _return.add(names[nameIndex++], Arrays.toString((int[])o));
        }else{
          _return.add(names[nameIndex++], o.toString());
        }
      }
      
      return _return;
    }
    
  }
}
