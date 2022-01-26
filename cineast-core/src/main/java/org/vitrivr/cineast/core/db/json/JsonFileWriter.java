package org.vitrivr.cineast.core.db.json;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class JsonFileWriter extends AbstractPersistencyWriter<JsonObject> {

  private File baseFolder;
  private PrintWriter out;
  private boolean first = true;

  public JsonFileWriter(File baseFolder) {
    this.baseFolder = baseFolder;
  }


  @Override
  public boolean open(String name) {
    baseFolder.mkdirs();
    if (this.out != null && !this.out.checkError()) {
      return false;
    }
    try {
      this.out = new PrintWriter(new File(baseFolder, name + ".json"));
      this.out.println('[');
      return true;
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  @Override
  public synchronized void close() {
    if (out == null) {
      return;
    }
    out.println();
    out.println(']');
    out.flush();
    out.close();
    out = null;
  }

  @Override
  public boolean persist(PersistentTuple tuple) {
    synchronized (out) {
      if (!this.first) {
        this.out.println(',');
      }
      this.out.print(this.getPersistentRepresentation(tuple).toString());
      this.out.flush();
      this.first = false;
    }

    return true;

  }

  @Override
  public boolean persist(List<PersistentTuple> tuples) {
    boolean success = true;
    for (PersistentTuple tuple : tuples) {
      if (!persist(tuple)) {
        success = false;
      }
    }
    return success;
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
  public JsonObject getPersistentRepresentation(PersistentTuple tuple) {

    int nameIndex = 0;

    JsonObject _return = new JsonObject();

    for (Object o : tuple.getElements()) {
      if (o instanceof float[]) {
        _return.add(names[nameIndex++], toArray((float[]) o));
      } else if (o instanceof ReadableFloatVector) {
        _return
            .add(names[nameIndex++], toArray(ReadableFloatVector.toArray((ReadableFloatVector) o)));
      } else if (o instanceof int[]) {
        _return.add(names[nameIndex++], toArray((int[]) o));
      } else if (o instanceof boolean[]) {
        _return.add(names[nameIndex++], toArray((boolean[]) o));
      } else if (o instanceof Integer) {
        _return.add(names[nameIndex++], new JsonPrimitive((int) o));
      } else if (o instanceof Float) {
        _return.add(names[nameIndex++], new JsonPrimitive((float) o));
      } else if (o instanceof Long) {
        _return.add(names[nameIndex++], new JsonPrimitive((long) o));
      } else if (o instanceof Double) {
        _return.add(names[nameIndex++], new JsonPrimitive((double) o));
      } else if (o instanceof Boolean) {
        _return.add(names[nameIndex++], new JsonPrimitive((boolean) o));
      } else {
        _return.add(names[nameIndex++], new JsonPrimitive(o.toString()));
      }
    }

    return _return;
  }

  @Override
  public int batchSize() {
    return 1;
  }

  private static JsonArray toArray(boolean[] arr) {
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < arr.length; ++i) {
      jarr.add(arr[i]);
    }
    return jarr;
  }

  private static JsonArray toArray(float[] arr) {
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < arr.length; ++i) {
      jarr.add(arr[i]);
    }
    return jarr;
  }

  private static JsonArray toArray(int[] arr) {
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < arr.length; ++i) {
      jarr.add(arr[i]);
    }
    return jarr;
  }
}
