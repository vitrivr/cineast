package org.vitrivr.cineast.playground.classification.tf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.tensorflow;
import org.vitrivr.cineast.core.util.TimeHelper;
import org.vitrivr.cineast.playground.ImageCropper;
import org.vitrivr.cineast.playground.label.VGGLabelProvider;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;

/**
 * Stub for the VGG-16 model as provided by https://github.com/ry/tensorflow-vgg16
 * <p>
 * Be careful when creating multiple tf-instances since the tensorflow-javabinding is singleton
 * <p>
 * Created by silvan on 23.08.16.
 */
public class VGG16Model implements TensorFlowModel {

    private VGGLabelProvider labelProvider;
    private static final Logger LOGGER = LogManager.getLogger();
    private final tensorflow.Session session;

    /**
     * TODO Does this work inside the jar
     *
     * @param model  Where is the tf-graph
     * @param labels Where are the labels
     */
    public VGG16Model(String model, File labels) {
        LOGGER.entry();
        TimeHelper.tic();
        session = new tensorflow.Session(new tensorflow.SessionOptions());
        tensorflow.GraphDef def = new tensorflow.GraphDef();
        tensorflow.ReadBinaryProto(tensorflow.Env.Default(),
                model, def);
        tensorflow.Status s = session.Create(def);
        if (!s.ok()) {
            LOGGER.error("Error while loading graph");
            throw new RuntimeException(s.error_message().getString());
        }
        LOGGER.debug("Loaded Graph in {}", TimeHelper.toc());

        labelProvider = new VGGLabelProvider(labels);
        LOGGER.exit();
    }

    @Override
    public float[] classify(BufferedImage img) {
        LOGGER.entry();
        TimeHelper.tic();
        //crop
        BufferedImage cropped = ImageCropper.scaleImage(img, 224, 224);

        //fill first layer
        tensorflow.Tensor inputs = new tensorflow.Tensor(
                tensorflow.DT_FLOAT, new tensorflow.TensorShape(1, 224, 224, 3));
        //For some weird reason the nn wants to have height*width.
        FloatBuffer fb = inputs.createBuffer();
        float[] data = new float[224 * 224 * 3];
        for (int x = 0; x < 224; x++) {
            for (int y = 0; y < 224; y++) {
                Color c = new Color(cropped.getRGB(y, x));
                data[x * (224 * 3) + y * 3] = (float) c.getRed() / 255f;
                data[x * (224 * 3) + y * 3 + 1] = (float) c.getGreen() / 255f;
                data[x * (224 * 3) + y * 3 + 2] = (float) c.getBlue() / 255f;
            }
        }
        fb.put(data);

        //prepare outputs & run session
        tensorflow.TensorVector outputs = new tensorflow.TensorVector();
        outputs.resize(0);
        tensorflow.Status s = session.Run(new tensorflow.StringTensorPairVector(new String[]{"images"}, new tensorflow.Tensor[]{inputs}),
                new tensorflow.StringVector("prob"), new tensorflow.StringVector(), outputs);
        if (!s.ok()) {
            throw new RuntimeException(s.error_message().getString());
        }
        FloatBuffer res = outputs.get(0).createBuffer();
        //Magic Number
        float[] _return = new float[1000];
        for(int i=0;i<res.limit();i++){
            _return[i] = res.get(i);
        }

        LOGGER.debug("Image classified by VGG16 in {}", TimeHelper.toc());
        return LOGGER.exit(_return);
    }

    @Override
    public String[] getLabels() {
        return labelProvider.getLabels();
    }
}
