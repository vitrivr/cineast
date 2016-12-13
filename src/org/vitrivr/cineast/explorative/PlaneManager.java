package org.vitrivr.cineast.explorative;

import org.vitrivr.cineast.core.data.Position;

import com.eclipsesource.json.JsonObject;

public interface PlaneManager<T extends Printable> {

  String getSingleElement(int level, int x, int y);

  JsonObject getElementPosition(int level, String id);

  String getRepresentativeOfElement(String id, int currentLevel);

  int getTopLevel();

  Position getCenter();

}