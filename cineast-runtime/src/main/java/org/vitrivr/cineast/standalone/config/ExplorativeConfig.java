package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public class ExplorativeConfig {

    private static String csvPath = "/Users/silvanstich/Library/Containers/com.apple.mail/Data/Library/Mail Downloads/CA01A1C7-162D-454F-9645-8F9250035697/neuralnet_vgg16_fullvector";
    private static String featureName = "nn_vecotrs";
    private static String elementLimit = "1000";
    private static String dataFolder = "data/";
    private static String resultFolder = "results/html/experimental/";
    private static String treeSerializationFileName = "nn_serialized_tree.ser";
    private static String mode = "csv";

    @JsonCreator
    public ExplorativeConfig() {

    }

    public static void readConfig(String file){
        Properties properties = new Properties();
        File propertiesFile = new File(file);
        try {
            FileInputStream fileInputStream = new FileInputStream(propertiesFile);
            properties.load(fileInputStream);
            fileInputStream.close();
        } catch (java.io.IOException e) {
            return; // use hard coded defaults defaults
        }
        csvPath = properties.getProperty("csvPath", csvPath);
        featureName = properties.getProperty("featureName", featureName);
        elementLimit = properties.getProperty("elementLimit", elementLimit);
        dataFolder = properties.getProperty("dataFolder", dataFolder);
        resultFolder = properties.getProperty("resultFolder", resultFolder);
        treeSerializationFileName = properties.getProperty("treeSerializationFileName", treeSerializationFileName);
        mode = properties.getProperty("mode", mode);
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

    public static String getMode() {
        return mode;
    }
}
