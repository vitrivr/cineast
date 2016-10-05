package org.vitrivr.cineast.core.data.hct;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.explorative.Plane;
import org.vitrivr.cineast.explorative.HCTFloatVectorValue;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// TODO: 28.09.16 this is a temporary class
public class HCTVisualizer {

    public static HashMap<String, String> segments;

    private static final Logger logger = LogManager.getLogger();

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

    public static BufferedImage drawImage(RGBContainer rgb){
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(new Color(rgb.toIntColor()));
        graphics.fillRect(0,0, 10, 10);
        graphics.dispose();
        return img;
    }

    public static void saveImgToFS(BufferedImage img, File path, String filename) throws IOException {
        File f = new File(path, filename + ".png");
        ImageIO.write(img, "PNG", f);
    }

    public static void visualizeTree(IHCTCell<HCTFloatVectorValue> cell, File file) throws Exception {
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

//    public static void draw(HCT hct) throws Exception{
//
//        HCTFloatVectorValue nucleusValue = (HCTFloatVectorValue) hct.getRoot().getNucleus().getValue();
//        Plane plane = new Plane((List<HCTFloatVectorValue>) hct.getRoot().getValues(), distanceCalculator, representative);
//        plane.processCollection(nucleusValue);
//        PrintWriter printWriter = new PrintWriter(new File("results/html_output.html"));
//        printWriter.print(plane.toHTML());
//        printWriter.flush();
//        printWriter.close();
//    }

}
