package org.vitrivr.cineast;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.explorative.*;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by silvanstich on 08.09.16.
 */
public class SilvanPlayground {
    static Logger logger = LogManager.getLogger();
    static HashMap<String, String> segments;

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
                visualizeTree(hct.getRoot(), new File("results/" + timestamp + "/" + "root"));
            } else {
                logger.info("Started deserialization");
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("data/serialized_tree.ser")));
                Object o = objectInputStream.readObject();
                HCT<HCTFloatVectorValue> hct = (HCT<HCTFloatVectorValue>)o;
                String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
                visualizeTree(hct.getRoot(), new File("results/" + timestamp + "/" + "root"));
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
        segments = new HashMap<>();
        for (Map<String, PrimitiveTypeProvider> segment : allSegments) {
            String segmentId = segment.get("id").getString();
            String multimediaobject = segment.get("multimediaobject").getString();
            segments.put(segmentId, multimediaobject);
        }


        HCT<HCTFloatVectorValue> hct = new HCT<HCTFloatVectorValue>(new DefaultCompactnessCalculation(), new FloatArrayEuclideanDistance());
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
    }

    private static void visualizeCells(List<HCTCell<HCTFloatVectorValue>> cells) throws IOException {
        int cellNr = 0;
        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
        File path = new File("results/" + timestamp + "/");
        if(!path.exists()) path.mkdirs();
        for(HCTCell<HCTFloatVectorValue> cell : cells){
            List<HCTFloatVectorValue> cellValues = cell.getValues();
            int valueNr = 0;
            for(HCTFloatVectorValue cellValue : cellValues){
                BufferedImage img = drawImage(ColorConverter.LabtoRGB(new ReadableLabContainer(cellValue.getVector()[0], cellValue.getVector()[1], cellValue.getVector()[2])));
                saveImgToFS(img, path, "cell_" + cellNr + "_" + valueNr);
                valueNr++;
            }
            cellNr++;
        }
    }

    private static BufferedImage drawImage(RGBContainer rgb){
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(new Color(rgb.toIntColor()));
        graphics.fillRect(0,0, 10, 10);
        graphics.dispose();
        return img;
    }

    private static void saveImgToFS(BufferedImage img, File path, String filename) throws IOException {
        File f = new File(path, filename + ".png");
        ImageIO.write(img, "PNG", f);
    }

    private static void visualizeTree(IHCTCell<HCTFloatVectorValue> cell, File file) throws Exception {
        if(cell.getChildren().size() > 0){
            for(IHCTCell<HCTFloatVectorValue> child : cell.getChildren()){
                if(!file.exists()) file.mkdirs();
                String cell_nbr = "cell_" + cell.getChildren().indexOf(child);
//                visualizeValue(child.getNucleus().getValue().getVector(), new File(file.toString(), cell_nbr + ".png"));
                visualizeValueByThumbnail(child.getNucleus().getValue().getSegment_id(), new File(file.toString(), cell_nbr + ".jpg"));
                File f = new File(cell_nbr);
                visualizeTree(child, new File(file.toString(), f.toString()));
            }
        } else{
            file.mkdirs();
            for (HCTFloatVectorValue value : cell.getValues()) {
//                visualizeValue(value.getVector(), new File(file.toString(), "value_" + cell.getValues().indexOf(value) + ".png"));
                visualizeValueByThumbnail(value.getSegment_id(), new File(file.toString(), "value_" + cell.getValues().indexOf(value) + ".jpg"));
            }

        }
    }

    private static void visualizeValue(float[] nucleus, File file) {
        try {
            BufferedImage img = drawImage(ColorConverter.LabtoRGB(new ReadableLabContainer(nucleus[0], nucleus[1], nucleus[2])));
            ImageIO.write(img, "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            logger.error("Conversion from Lab to RGB failed: " + nucleus);
        }
    }

    private static void visualizeValueByThumbnail(String segementId, File file) throws IOException {
        String multimediaobject = segments.get(segementId);
        File thumbnail = new File("/Applications/XAMPP/htdocs/vitrivr-ui/thumbnails/" + multimediaobject + "/" + segementId + ".jpg");
        if(thumbnail.exists()){
            Files.copy(thumbnail, file);
        } else{
            logger.info("File does not exist: " + thumbnail.getPath());
        }

    }
}