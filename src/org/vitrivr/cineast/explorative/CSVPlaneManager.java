package org.vitrivr.cineast.explorative;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.data.Position;
import org.vitrivr.cineast.core.data.StatElement;
import org.vitrivr.cineast.core.data.UniqueElementGrid;

import com.eclipsesource.json.JsonObject;

public class CSVPlaneManager<T extends Printable> implements PlaneManager<T> {

  private final ArrayList<UniqueElementGrid<String>> grids = new ArrayList<>();
  final List<Position> centers = new ArrayList<>();
  
  CSVPlaneManager(File baseFolder){
    if(baseFolder == null){
      throw new IllegalArgumentException("baseFolder cannot be null");
    }
    if(!baseFolder.isDirectory()){
      throw new IllegalArgumentException("'" + baseFolder.getAbsolutePath() + "' is not a directory");
    }
    File[] csvs = baseFolder.listFiles(new FileFilter() {
      
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().matches("\\d+.csv");
      }
    });
    
    for(File csv: csvs){
      try {
        BufferedReader reader = new BufferedReader(new FileReader(csv));
        String line = null;
        UniqueElementGrid<String> grid = new UniqueElementGrid<>();
        StatElement xpos = new StatElement(), ypos = new StatElement();
        while((line = reader.readLine()) != null){
          String[] split = line.split(",");
          int x = Integer.parseInt(split[0]);
          int y = Integer.parseInt(split[1]);
          xpos.add(x);
          ypos.add(y);
          grid.setElement(x, y, split[2]);
        }
        grids.add(grid);
        centers.add(new Position((int)xpos.getAvg(), (int)ypos.getAvg()));
        reader.close();
      } catch (FileNotFoundException e) {
        //this can be ignored
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
  }
  
  @Override
  public String getSingleElement(int level, int x, int y) {
    return this.grids.get(level).get(x, y);
  }

  @Override
  public JsonObject getElementPosition(int level, String id) {
    Position position = this.grids.get(level).getPosition(id);
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("x", position.getX());
    jsonObject.add("y", position.getY());
    return jsonObject;
  }

  @Override
  public String getRepresentativeOfElement(String id, int currentLevel) {
    // TODO 
    return "";
  }

  @Override
  public int getTopLevel() {
    return this.grids.size() - 1;
  }

  @Override
  public Position getCenter() {
    return centers.get(getTopLevel());
  }

}
