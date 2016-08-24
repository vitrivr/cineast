package org.vitrivr.cineast.playground;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.TimeHelper;
import org.vitrivr.cineast.playground.classification.NeuralNet;
import org.vitrivr.cineast.playground.classification.NeuralNetFactory;
import org.vitrivr.cineast.playground.classification.tf.TensorFlowNet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Short demo-class while we are in the process of integrating into the main- codebase
 * Created by silvan on 22.08.16.
 */
public class VGG {

    public static void main(String[] args) throws IOException {

        NeuralNet nn = TensorFlowNet.getCurrentImpl();
        float[] probs = nn.classify(ImageIO.read(new File("src/resources/cat.jpg")));
        String[] labels = nn.getLabels();
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > 0.05) {
                System.out.println("Probability for " + labels[i] + "=" + probs[i]);
            }
        }
    }
}

/**
 * Mock-class for thinking
 */
class nn_feature extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private final NeuralNet net;

    //TODO What is maxDist here?
    protected nn_feature(float maxDist) {
        this(maxDist, NeuralNetFactory.generateDefaultNet());
    }

    //TODO I think we need to create the additional tables here. Where is the proper entrypoint?
    //TODO Create table with Labels here? Or load them from files permanently?
    protected nn_feature(float maxDist, NeuralNet net) {
        super("features_neuralnet", maxDist);
        this.net = net;
    }

    /**
     * Classifies an Image with a neural net
     *
     * @return A float array containing the probabilities given by the neural net.
     */
    private float[] classifyImage(BufferedImage img) {
        return net.classify(img);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.entry();
        TimeHelper.tic();
        if (!phandler.idExists(shot.getId())) {
            BufferedImage keyframe = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
            float[] probs = classifyImage(keyframe);

            //TODO What do we persist here? Labels? Mockvector?
            //TODO Does persist store the vector?
            persist(shot.getId(), new FloatVectorImpl(probs));
            LOGGER.debug("NeuralNet.processShot() done in {}",
                    TimeHelper.toc());
        }
        LOGGER.exit();
    }

    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        LOGGER.entry();
        TimeHelper.tic();
        //TODO Can we just take the most representative frame from the sc? Is that the query image?
        float[] res = classifyImage(sc.getMostRepresentativeFrame().getImage().getBufferedImage());

        //TODO Compare to DB using Labels

        //TODO Maybe do kNN on the 1k-vectors?
        //TODO How do we calculate score?
        List<StringDoublePair> _return = getSimilar(res, qc);
        LOGGER.debug("NeuralNet.getSimilar() done in {}",
                TimeHelper.toc());
        //TODO Do we just return a StringDoublePair or do we return something else?
        return LOGGER.exit(_return);
    }
}
