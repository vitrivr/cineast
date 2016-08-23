package org.vitrivr.cineast.playground;
import org.bytedeco.javacpp.tensorflow;
import org.vitrivr.cineast.playground.label.LabelProvider;
import org.vitrivr.cineast.playground.label.VGGLabelProvider;

import static org.bytedeco.javacpp.tensorflow.DT_STRING;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;


/**
 * Created by silvan on 22.08.16.
 */
public class VGG {

    /** Short demo-class while we are in the process of integrating into the main- codebase */
    public static void main(String[] args){

        LabelProvider labeler = new VGGLabelProvider(new File("src/resources/caffe/synset.txt"));

        //Load Graph
        final tensorflow.Session session = new tensorflow.Session(new tensorflow.SessionOptions());
        tensorflow.GraphDef def = new tensorflow.GraphDef();
        tensorflow.ReadBinaryProto(tensorflow.Env.Default(),
                "src/resources/caffe/vgg16-20160129.tfmodel", def);
        tensorflow.Status s = session.Create(def);
        if (!s.ok()) {
            System.out.println("Error While loading graph");
            throw new RuntimeException(s.error_message().getString());
        }
        System.out.println("Loaded Graph");

        File file = new File("src/resources/banana.png");
        BufferedImage im = null;
        try {
            im = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Loaded picture");

        tensorflow.Tensor inputs = new tensorflow.Tensor(
                tensorflow.DT_FLOAT, new tensorflow.TensorShape(1,224, 224, 3));

        FloatBuffer fb = inputs.createBuffer();

        System.out.println(inputs.tensor_data().toString());
        float[] data = new float[224*224*3];
        for(int x = 0; x<224; x++){
            for(int y = 0; y<224; y++){
                Color c = new Color(im.getRGB(x,y));
                data[x*(224*3)+y*3+0] = c.getRed();
                data[x*(224*3)+y*3+1] = c.getGreen();
                data[x*(224*3)+y*3+2] = c.getBlue();
            }
        }
        fb.put(data);
        System.out.println(fb.get(0));
        System.out.println(inputs.tensor_data().toString());

        tensorflow.TensorVector outputs = new tensorflow.TensorVector();
        outputs.resize(0);

        System.out.println("Running Session");
        s = session.Run(new tensorflow.StringTensorPairVector(new String[] {"images"}, new tensorflow.Tensor[] {inputs}),
                new tensorflow.StringVector("prob"), new tensorflow.StringVector(), outputs);
        if (!s.ok()) {
            throw new RuntimeException(s.error_message().getString());
        }
        System.out.println("Done!");

        FloatBuffer output = outputs.get(0).createBuffer();
        for (int k=0; k < output.limit(); ++k){
            if(output.get(k)>0.01){
                System.out.println("Probability for "+labeler.getLabel(k)+": " + output.get(k));
            }
        }
    }
}
