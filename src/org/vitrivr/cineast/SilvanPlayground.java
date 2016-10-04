package org.vitrivr.cineast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.hct.DefaultCompactnessCalculation;
import org.vitrivr.cineast.core.data.hct.HCT;
import org.vitrivr.cineast.core.data.hct.HCTVisualizer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.explorative.*;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by silvanstich on 08.09.16.
 */
public class SilvanPlayground {
    static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("started...");

        try {

            if(args.length == 0){
                buildTree();
                logger.info("Started deserialization");
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("data/serialized_tree.ser")));
                Object o = objectInputStream.readObject();
                HCT<HCTFloatVectorValue> hct = (HCT<HCTFloatVectorValue>)o;
                String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
                HCTVisualizer.visualizeTree(hct.getRoot(), new File("results/" + timestamp + "/" + "root"));
            } else {
                logger.info("Started deserialization");
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("data/serialized_tree.ser")));
                Object o = objectInputStream.readObject();
                HCT<HCTFloatVectorValue> hct = (HCT<HCTFloatVectorValue>)o;
                String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
                HCTVisualizer.visualizeTree(hct.getRoot(), new File("results/" + timestamp + "/" + "root"));
            }

            logger.info("Finished!");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
    }

    private static void buildTree() throws Exception {
        DBSelector dbSelector = Config.getDatabaseConfig().getSelectorSupplier().get();
        dbSelector.open("features_dominantedgegrid16");
        List<Map<String, PrimitiveTypeProvider>> l = dbSelector.getAll();
        List<HCTFloatVectorValue> vectors = new ArrayList<>();
        if (l.size() > 0) {
            for(Map<String, PrimitiveTypeProvider> map: l){
                String id = map.get("id").getString();
                float[] feature = map.get("feature").getFloatArray();
                HCTFloatVectorValue vectorContainer = new HCTFloatVectorValue(feature, id);
                vectors.add(vectorContainer);
            }
        }

        logger.info("Read " + l.size() + " rows.");
        dbSelector.close();

        dbSelector = Config.getDatabaseConfig().getSelectorSupplier().get();
        dbSelector.open("cineast_segment");
        List<Map<String, PrimitiveTypeProvider>> allSegments = dbSelector.getAll();
        HCTVisualizer.segments = new HashMap<>();
        for (Map<String, PrimitiveTypeProvider> segment : allSegments) {
            String segmentId = segment.get("id").getString();
            String multimediaobject = segment.get("multimediaobject").getString();
            HCTVisualizer.segments.put(segmentId, multimediaobject);
        }


        HCT<HCTFloatVectorValue> hct = new HCT<>(new DefaultCompactnessCalculation(), new FloatArrayEuclideanDistance());
        System.in.read();
        int i = 0;
        for (HCTFloatVectorValue vector : vectors) {
            i++;
            hct.insert(vector);
            if(i == 100) break;
        }

        logger.info("All items inserted...");

        logger.info("Start writting HCT to the file system.");
        File folder = new File("data/");
        if(!folder.exists()) folder.mkdirs();
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(folder, "serialized_tree.ser")));
        outputStream.writeObject(hct);
        logger.info("HCT has been written to the file system.");

        HCTVisualizer.draw(hct);
    }

}