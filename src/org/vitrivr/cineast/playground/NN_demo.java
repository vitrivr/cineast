package org.vitrivr.cineast.playground;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Short demo-class while we are in the process of integrating into the main- codebase
 * Created by silvan on 22.08.16.
 */
public class NN_demo {

    public static void main(String[] args) throws IOException {

        NeuralNet nn = Config.getNeuralNetConfig().getNeuralNetFactory().get();
        BufferedImage keyframe = ImageIO.read(new File("src/resources/nun_penguins.jpeg"));
        float[] probs = new float[1000];
        Arrays.fill(probs, 0f);
        Position[] positions = new Position[]{Positions.CENTER, Positions.TOP_RIGHT, Positions.BOTTOM_RIGHT, Positions.BOTTOM_LEFT, Positions.TOP_LEFT};
        List<List<String>> labels = nn.getAllLabels();
        
        int posidx = 0;
        for(Position pos: positions){
            System.out.println(posidx);
            BufferedImage cropped = Thumbnails.of(keyframe).size(224, 224).crop(pos).asBufferedImage();
            ImageIO.write(cropped, "png", new File("src/resources/crop_"+posidx+++".png"));
            float[] curr = nn.classify(Thumbnails.of(keyframe).size(224, 224).crop(pos).asBufferedImage());
            for (int i = 0; i < curr.length; i++) {
                if (curr[i] > 0.05) {
                    //Wow, Java 8 added a built-in functionality for converting a list to a string.
                    System.out.println("Probability for " + String.join(", ", labels.get(i)) + "=" + curr[i]+"... "+pos.toString());
                }
            }
            probs = maxpool(curr, probs);
        }

        //float[] probs = nn.classify(ImageIO.read(new File("thumbnails/v1/v1_20.jpg")));
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > 0.05) {
                //Wow, Java 8 added a built-in functionality for converting a list to a string.
                System.out.println("Probability for " + String.join(", ", labels.get(i)) + "=" + probs[i]);
            }
        }
    }

    /**
     * Takes the maximum value at each position
     */
    private static float[] maxpool(float[] curr, float[] probs) {
        if(curr.length!=probs.length){
            throw new IllegalArgumentException("Float[] need to have the same size");
        }
        float[] _ret = new float[curr.length];
        for(int i = 0; i<curr.length;i++){
            if(curr[i]>probs[i]){
                _ret[i]= curr[i];
            } else{
                _ret[i]=probs[i];
            }
        }
        return _ret;
    }
}

