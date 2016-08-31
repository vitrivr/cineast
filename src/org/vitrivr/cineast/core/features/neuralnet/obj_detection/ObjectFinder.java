package org.vitrivr.cineast.core.features.neuralnet.obj_detection;

import net.coobird.thumbnailator.Thumbnails;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Playground-class to implement something along the lines of figure 7 http://arxiv.org/pdf/1311.2901v3.pdf
 * <p>
 * Created by silvan on 24.08.16.
 */
public class ObjectFinder {

    private BufferedImage img;
    private NeuralNet net;

    public static void main(String[] args) throws IOException {
        if(!new File("src/resources/finder").exists()){
            new File("src/resources/finder").mkdirs();
        }
        ObjectFinder obj = new ObjectFinder(ImageIO.read(new File("src/resources/bbb-splash.png")), Config.getNeuralNetConfig().getNeuralNetFactory().get());
        obj.showHeatMap();
    }

    public ObjectFinder(BufferedImage img, NeuralNet net) {
        this.img = img;
        this.net = net;
    }

    /**
     * Moves a grey rectangle to get a heatmap
     */
    public void showHeatMap() throws IOException {
        float[] res = net.classify(img);
        for(int i = 0; i<res.length; i++){
            if(res[i]>0.05){
                //System.out.println(net.getAllLabels()[i]+" "+res[i]);
            }
        }
        int desiredIdx = getMax(res);
        float desiredVal = res[desiredIdx];
        //System.out.println("Desired Label: "+net.getAllLabels()[desiredIdx]);
        BufferedImage greyHeatmap = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        BufferedImage greyClasses = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Color[] cols = new Color[]{Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.gray, Color.green, Color.red, Color.yellow, Color.BLACK, Color.MAGENTA, Color.LIGHT_GRAY, Color.ORANGE, Color.PINK};
        int colindex = 1;
        Map<Integer, Integer> labelMap = new HashMap<Integer, Integer>();

        int move = 300;
        for (int x = 0; x < img.getWidth(); x += move) {
            for (int y = 0; y < img.getHeight(); y += move) {
                BufferedImage grey = this.addGreyRectangle(img, x-move, y-move, move*3, move*3);
                ImageIO.write(grey,"png",new File("src/resources/finder/zzgrey_"+x+"_"+y+".png"));
                BufferedImage cropped = Thumbnails.of(img).sourceRegion(x, y, move, move).size(move, move).asBufferedImage();
                ImageIO.write(cropped, "png", new File("src/resources/finder/zzcropped_"+x+"_"+y+".png"));

                float[] greyprobs = net.classify(grey);
                //Math.min because hiding some areas might provide even better classification results
                Color desired = new Color(1 - Math.min(greyprobs[desiredIdx]/desiredVal,1), 0, 0);
                int leader = getMax(greyprobs);

                //System.out.println(x+", "+y+", "+ greyprobs[desiredIdx]+" Classified: "+net.getAllLabels()[leader] +" with prob: " +greyprobs[leader]);
                //draw greyHeatmap
                for (int i = 0; i < move; i++) {
                    for (int j = 0; j < move; j++) {
                        if (x+ i >= greyHeatmap.getWidth() || y+ j >= greyHeatmap.getHeight()) continue;
                        greyHeatmap.setRGB(x + i, y + j, desired.getRGB());
                        if(!labelMap.containsKey(leader)){
                            if(greyprobs[leader]<0.1){
                                labelMap.put(leader, Color.BLACK.getRGB());
                            }else{
                                labelMap.put(leader, cols[colindex].getRGB());
                                colindex++;
                            }
                            //System.out.println(colindex+" | "+net.getAllLabels()[leader]);
                        }
                        greyClasses.setRGB(x+i, y+j, labelMap.get(leader));
                    }
                }
            }
        }
        ImageIO.write(greyHeatmap, "png", new File("src/resources/finder/greyHeatmap.png"));
        ImageIO.write(greyClasses, "png", new File("src/resources/finder/labels.png"));
    }

    /**
     * get index of the max-value of an unsorted float-array. Returns -1 if array is empty
     * Has "Race-condition" if two values are the same in the sense that the comparison is >=
     */
    public static int getMax(float[] arr) {
        float max = Float.MIN_VALUE;
        int idx = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] >= max) {
                idx = i;
                max = arr[i];
            }
        }
        return idx;
    }

    /**
     * Draws a grey rectangle on the image starting @ x,y. Don't worry about overflow
     */
    public BufferedImage addGreyRectangle(BufferedImage base, int x, int y, int width, int height) throws IOException {
        BufferedImage _return = deepCopy(base);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (x + i >= base.getWidth() || y + j >= base.getHeight() || x+i<0 || y+j<0) {
                    continue;
                }
                _return.setRGB(x + i, y + j, Color.GRAY.getRGB());
            }
        }
        ImageIO.write(_return,"png",new File("src/resources/finder/zzgrey_"+x+"_"+y+".png"));
        return _return;
    }

    /**
     * http://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
     * Creates a deep Copy of a bufferedImage
     */
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
