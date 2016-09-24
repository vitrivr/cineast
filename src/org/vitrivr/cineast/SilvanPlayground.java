package org.vitrivr.cineast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.explorative.HCT;
import org.vitrivr.cineast.explorative.HCTCell;
import org.vitrivr.cineast.explorative.SimpleFloatMathematics;



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
    static int initialDimension = 0;
    static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("started...");

        try {

            if(args.length == 0){
                buildTree();
            } else {
                logger.info("Started deserialization");
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("data/serialized_tree.ser")));
                Object o = objectInputStream.readObject();
                HCT<Float> hct = (HCT<Float>)o;
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
        dbSelector.open("features_averagecolor");
        List<PrimitiveTypeProvider> l = dbSelector.getAll("feature");
        List<float[]> features = new ArrayList<>();
        if (l.size() > 0) {
            for(PrimitiveTypeProvider ptp : l){
                features.add(ptp.getFloatArray());
            }
        }

        logger.info("Read " + features.size() + " rows.");
        dbSelector.close();

        HCT<Float> hct = new HCT<>( new SimpleFloatMathematics());
        System.in.read();
        int i = 0;
        for (float[] feature : features) {
            List<Float> featureEntryList = new ArrayList<>();
            for(float f : feature){
                featureEntryList.add(f);
            }
            i++;
            hct.insert(featureEntryList);
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

    private static void visualizeCells(List<HCTCell<Float>> cells) throws IOException {
        int cellNr = 0;
        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date(System.currentTimeMillis()));
        File path = new File("results/" + timestamp + "/");
        if(!path.exists()) path.mkdirs();
        for(HCTCell<Float> cell : cells){
            List<List<Float>> cellValues = cell.getValues();
            int valueNr = 0;
            for(List<Float> cellValue : cellValues){
                BufferedImage img = drawImage(ColorConverter.LabtoRGB(new ReadableLabContainer(cellValue.get(0), cellValue.get(1), cellValue.get(2))));
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

    private static void visualizeTree(HCTCell<Float> cell, File file) throws Exception {
        if(cell.getChildren().size() > 0){
            for(HCTCell<Float> child : cell.getChildren()){
                if(!file.exists()) file.mkdirs();
                String cell_nbr = "cell_" + cell.getChildren().indexOf(child);
                visualizeValue(child.getNucleus().getValue(), new File(file.toString(), cell_nbr + ".png"));
                File f = new File(cell_nbr);
                visualizeTree(child, new File(file.toString(), f.toString()));
            }
        } else{
            file.mkdirs();
            for (List<Float> value : cell.getValues()) {
                visualizeValue(value, new File(file.toString(), "value_" + cell.getValues().indexOf(value) + ".png"));
            }

        }
    }

    private static void visualizeValue(List<Float> nucleus, File file) {
        BufferedImage img = drawImage(ColorConverter.LabtoRGB(new ReadableLabContainer(nucleus.get(0), nucleus.get(1), nucleus.get(2))));
        try {
            ImageIO.write(img, "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}