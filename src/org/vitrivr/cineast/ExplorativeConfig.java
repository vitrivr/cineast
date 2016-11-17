package org.vitrivr.cineast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;


public class ExplorativeConfig {

    private static String csvPath = "/Users/silvanstich/Downloads/keyframes.crop.fovea";
    private static String featureName = "csv_value";
    private static String elementLimit = "1000000";
    private static String dataFolder = "data/";
    private static String resultFolder = "results/html/experimental/";
    private static String treeSerializationFileName = "serialized_tree.ser";

    public void readConfig(){
        Properties properties = new Properties();
        File propertiesFile = new File("properties.config");
        try {
            FileInputStream fileInputStream = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            return; // use in code defaults
        }
        csvPath = properties.getProperty("csvPath", csvPath);
        featureName = properties.getProperty("featureName", featureName);
        elementLimit = properties.getProperty("elementLimit", elementLimit);
        dataFolder = properties.getProperty("dataFolder", dataFolder);
        resultFolder = properties.getProperty("resultFolder", resultFolder);
        treeSerializationFileName = properties.getProperty("treeSerializationFileName", treeSerializationFileName);
    }

    public static String getCsvPath() {
        return csvPath;
    }

    public static String getFeatureName() {
        return featureName;
    }

    public static String getElementLimit() {
        return elementLimit;
    }

    public static String getDataFolder() {
        return dataFolder;
    }

    public static String getResultFolder() {
        return resultFolder;
    }

    public static String getTreeSerializationFileName() {
        return treeSerializationFileName;
    }
}
