package org.vitrivr.cineast.playground.classification.tf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.tensorflow;
import org.vitrivr.cineast.core.util.TimeHelper;
import org.vitrivr.cineast.playground.ImageCropper;
import org.vitrivr.cineast.playground.label.SynLabelProvider;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;

/**
 * VGG-16 model as provided by https://github.com/ry/tensorflow-vgg16
 * <p>
 * Be careful when creating multiple tf-instances since the models are quite big
 * <p>
 * Created by silvan on 23.08.16.
 */
class VGG16Model implements TensorFlowModel {

    private SynLabelProvider labelProvider;
    private static final Logger LOGGER = LogManager.getLogger();
    private final tensorflow.Session session = new tensorflow.Session(new tensorflow.SessionOptions());

    /**
     * TODO Does this work inside the jar
     *
     * @param model  Where is the tf-graph
     * @param labels Where are the labels
     */
    VGG16Model(String model, File labels) {
        LOGGER.entry();
        loadGraph(model);
        labelProvider = new SynLabelProvider(labels);
        LOGGER.exit();
    }

    /**
     * If labels are already inside the DB
     */
    VGG16Model(String model) {
        //TODO Init LabelProvider with Models from DB
        loadGraph(model);
        throw new UnsupportedOperationException();
    }

    private void loadGraph(String model) {
        TimeHelper.tic();
        tensorflow.GraphDef def = new tensorflow.GraphDef();
        tensorflow.ReadBinaryProto(tensorflow.Env.Default(),
                model, def);
        tensorflow.Status s = session.Create(def);
        if (!s.ok()) {
            LOGGER.error("Error while loading graph");
            throw new RuntimeException(s.error_message().getString());
        }
        LOGGER.debug("Loaded Graph in {}", TimeHelper.toc());
    }

    @Override
    public float[] classify(BufferedImage img) {
        LOGGER.entry();
        TimeHelper.tic();
        //crop
        BufferedImage cropped = ImageCropper.scaleAndCropImage(img, 224, 224);

        //fill first layer
        tensorflow.Tensor inputs = new tensorflow.Tensor(
                tensorflow.DT_FLOAT, new tensorflow.TensorShape(1, 224, 224, 3));
        //For some weird reason the nn wants to have height*width.
        FloatBuffer fb = inputs.createBuffer();
        float[] data = new float[224 * 224 * 3];
        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                Color c = new Color(cropped.getRGB(x, y));
                data[y * (224 * 3) + x * 3] = (float) c.getRed() / 255f;
                data[y * (224 * 3) + x * 3 + 1] = (float) c.getGreen() / 255f;
                data[y * (224 * 3) + x * 3 + 2] = (float) c.getBlue() / 255f;
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
        //Magic Number because VGG=1k labels
        float[] _return = new float[1000];
        for (int i = 0; i < res.limit(); i++) {
            _return[i] = res.get(i);
        }

        LOGGER.debug("Image classified by VGG16 in {} msec", TimeHelper.toc());
        return LOGGER.exit(_return);
    }

    @Override
    public String[] getLabels() {
        return labelProvider.getLabels();
    }
}
