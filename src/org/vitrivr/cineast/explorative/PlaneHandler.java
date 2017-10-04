package org.vitrivr.cineast.explorative;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlaneHandler {

    private static final String PATH = "data/csv/";
    private static Map<String, PlaneManager<?>> planeManagers = new HashMap<>();

    public static PlaneManager<?> getSpecificPlaneManager(String featureName) throws IOException, ClassNotFoundException {

        if(planeManagers.isEmpty()) {
          readSerializedPlanManagers();
        }
        if(!planeManagers.containsKey(featureName.toLowerCase())) {
          throw new RuntimeException("Feature has not been processed");
        }

        return planeManagers.get(featureName.toLowerCase());
    }

    private static void readSerializedPlanManagers() throws IOException, ClassNotFoundException {
        File path = new File(PATH);
        if(!path.exists())
         {
          throw new RuntimeException("Folder for serialized PlaneManager does not exist!");
//
//        for(String fileName : path.list()){
//            if(!fileName.matches("plane_manager_[A-z0-9]*.ser")) continue;
//
//            String featureName = fileName.replace("plane_manager_", "").replace(".ser", "").toLowerCase();
//            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(path, fileName)));
//            Object o = objectInputStream.readObject();
//            ImprovedPlaneManager<?> p = (ImprovedPlaneManager<?>) o;
//            planeManagers.put(featureName, p);
//            objectInputStream.close();
//        }
        }
        
        File[] dirs = path.listFiles(new FileFilter() {
          
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory();
          }
        });
        
        for(File dir : dirs){
          planeManagers.put(dir.getName(), new CSVPlaneManager<>(dir));
        }
    }
    
    public static Set<String> getPlaneManagerNames(){
      if(planeManagers.isEmpty()){
        try {
          readSerializedPlanManagers();
        } catch (ClassNotFoundException | IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      Set<String> _return= new HashSet<>();
      _return.addAll(planeManagers.keySet());
      return _return;
    }
}
