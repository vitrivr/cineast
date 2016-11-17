package org.vitrivr.cineast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.hct.DefaultCompactnessCalculation;
import org.vitrivr.cineast.core.data.hct.HCT;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.explorative.*;


import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;


public class SilvanPlayground {
    private static Logger logger = LogManager.getLogger();
    public static HashMap<String, String> segments = new HashMap<>();;

    public static void main(String[] args) {
        processCSVValues();
    }

    private static void processFeatureFromDB() {
        long startTime;

        logger.info("started...");

        try {
            String featureName = "features_averagecolor";
            logger.info("Proccessing HCT for '" + featureName + "'...");

            readSegementsFromDB();
            List<HCTFloatVectorValue> vectors = readFeaturesFromDB(featureName);

            System.out.println("Press any key to start...");
            //System.in.read();

            startTime = System.currentTimeMillis();
            logger.info("Creating HCT...");
            HCT<HCTFloatVectorValue> hct = buildTree(vectors);
            logger.info("All items inserted...");

            logger.info("# of elements in tree by traversion: " + hct.traverse(hct.getRootCell(), 0));

            logger.info("Start writting HCT to the file system...");
            File folder = new File("data/");
            if(!folder.exists()) folder.mkdirs();
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(folder, "serialized_tree.ser")));
            outputStream.writeObject(hct);
            logger.info("HCT has been written to the file system.");

            logger.info("Traversion started...");
//            hct.traverseTreeHorizontal(new PlaneManager<>(new FloatArrayEuclideanDistance(), featureName.toLowerCase()));
            hct.traverseTreeHorizontal(new ImprovedPlaneManager<>(new FloatArrayEuclideanDistance(), featureName.toLowerCase()));
            logger.info("Traversion finished!");

            logger.info("Show json request");
            PlaneManager pm = RequestHandler.getSpecificPlaneManager(featureName);
            System.out.println(pm.getElementField(0, 240, 240, 245, 245));

            logger.info("Started deserialization");
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("data/serialized_tree.ser")));
            Object o = objectInputStream.readObject();
            HCT<HCTFloatVectorValue> hct2 = (HCT<HCTFloatVectorValue>)o;
            logger.info("Finished deserialization!");

            logger.info("# of cache access is " + FloatArrayEuclideanDistance.cacheCounter + " | # of calculations is " + FloatArrayEuclideanDistance.calculationCounter);
            logger.info("Finished! Total duration (without DB request time): " + (System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
    }

    private static HCT<HCTFloatVectorValue> buildTree(List<HCTFloatVectorValue> vectors) throws Exception {
        HCT<HCTFloatVectorValue> hct = new HCT<>(new DefaultCompactnessCalculation(), new FloatArrayEuclideanDistance());
        int i = 0;
        int limit = Integer.parseInt(ExplorativeConfig.getElementLimit());
        for (HCTFloatVectorValue vector : vectors) {
            i++;
            hct.insert(vector);
            if(i == limit) break;
        }
        return hct;
    }

    private static void readAndInsertFeatureFromCSV(HCT<HCTFloatVectorValue> hct, String path) throws Exception {
        File[] files = new File(path).listFiles();
        int id = 0;
        for(File f : files){
            String content = com.google.common.io.Files.toString(f, Charset.defaultCharset());
            String[] values = content.split(",");
            float[] floatValues = new float[values.length];
            for(int i = 0; i < values.length; i++){
                floatValues[i] = Float.parseFloat(values[i]);
            }
            HCTFloatVectorValue floatVectorValue = new HCTFloatVectorValue(floatValues, String.valueOf(id));
            mockSegments(String.valueOf(id));
            hct.insert(floatVectorValue);
            id++;
        }
    }

    private static void processCSVValues(){
        long startTime;

        logger.info("started...");

        try {
            String featureName = ExplorativeConfig.getFeatureName();
            logger.info("Proccessing HCT for '" + featureName + "'...");

            HCT<HCTFloatVectorValue> hct = new HCT<>(new DefaultCompactnessCalculation(), new FloatArrayEuclideanDistance());

            logger.info("Start reading values from csv...");
            readAndInsertFeatureFromCSV(hct, ExplorativeConfig.getCsvPath());
            logger.info("End reading values from csv.");

            startTime = System.currentTimeMillis();

            logger.info("# of elements in tree by traversion: " + hct.traverse(hct.getRootCell(), 0));

            logger.info("Start writting HCT to the file system...");
            File folder = new File("data/");
            if (!folder.exists()) folder.mkdirs();
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(folder, "serialized_tree.ser")));
            outputStream.writeObject(hct);
            logger.info("HCT has been written to the file system.");

            logger.info("Traversion started...");
//            hct.traverseTreeHorizontal(new PlaneManager<>(new FloatArrayEuclideanDistance(), featureName.toLowerCase()));
            hct.traverseTreeHorizontal(new ImprovedPlaneManager<>(new FloatArrayEuclideanDistance(), featureName.toLowerCase()));
            logger.info("Traversion finished!");

            logger.info("Show json request");
            PlaneManager pm = RequestHandler.getSpecificPlaneManager(featureName);
            System.out.println(pm.getElementField(0, 240, 240, 245, 245));

            logger.info("Started deserialization");
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(ExplorativeConfig.getDataFolder(), ExplorativeConfig.getTreeSerializationFileName())));
            Object o = objectInputStream.readObject();
            HCT<HCTFloatVectorValue> hct2 = (HCT<HCTFloatVectorValue>) o;
            logger.info("Finished deserialization!");

            logger.info("# of cache access is " + FloatArrayEuclideanDistance.cacheCounter + " | # of calculations is " + FloatArrayEuclideanDistance.calculationCounter);
            logger.info("Finished! Total duration (without DB request time): " + (System.currentTimeMillis() - startTime));
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    private static void mockSegments(String id){
        if(!segments.containsKey(id)){
            segments.put(id, id);
        }
    }

    private static void readSegementsFromDB() {
        DBSelector dbSelector;

        dbSelector = Config.getDatabaseConfig().getSelectorSupplier().get();
        dbSelector.open("cineast_segment");
        List<Map<String, PrimitiveTypeProvider>> allSegments = dbSelector.getAll();
        for (Map<String, PrimitiveTypeProvider> segment : allSegments) {
            String segmentId = segment.get("id").getString();
            String multimediaobject = segment.get("multimediaobject").getString();
            segments.put(segmentId, multimediaobject);
        }
    }

    private static List<HCTFloatVectorValue> readFeaturesFromDB(String featureName) {
        DBSelector dbSelector = Config.getDatabaseConfig().getSelectorSupplier().get();
        dbSelector.open(featureName);
        List<Map<String, PrimitiveTypeProvider>> l = dbSelector.getAll();
        List<HCTFloatVectorValue> vectors = new ArrayList<>();
        if (l.size() > 0) {
            for(Map<String, PrimitiveTypeProvider> map: l){
                String id = map.get("id").getString();
                float[] feature = map.get("feature").getFloatArray();
                HCTFloatVectorValue vectorContainer = new HCTFloatVectorValue(feature, segments.get(id) + "/" + id);
                vectors.add(vectorContainer);
            }
        }

        logger.info("Read " + l.size() + " rows.");
        dbSelector.close();
        return vectors;
    }
}