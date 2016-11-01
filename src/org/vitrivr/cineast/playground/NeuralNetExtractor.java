package org.vitrivr.cineast.playground;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.*;
import org.vitrivr.cineast.core.db.MultimediaObjectLookup.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.decode.video.VideoDecoder;
import org.vitrivr.cineast.core.features.extractor.DefaultExtractorInitializer;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.NeuralNetVGG16Feature;
import org.vitrivr.cineast.core.runtime.ShotDispatcher;
import org.vitrivr.cineast.core.segmenter.ShotSegmenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Ideally, this class should extract the NN-Features for every movie which does not have that shot processed yet
 * Created by silvan on 31.10.16.
 */
public class NeuralNetExtractor {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args){

        DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();

        MultimediaObjectLookup ml = new MultimediaObjectLookup();
        LOGGER.debug("Looking up all Multimedia objects... ");
        List<MultimediaObjectDescriptor> videos = ml.getAllVideos();
        for (MultimediaObjectDescriptor video : videos) {
            LOGGER.info("Starting to extract for video {} at location {}", video.getName(), "osvc_1.0/"+video.getPath());
            File location = new File("oscv_1.0/"+video.getPath());
            if(!location.exists()){
                LOGGER.warn("Could not find video {} at path {}", video.getName(), location.getAbsolutePath());
                continue;
            }
            VideoDecoder vd = Config.getDecoderConfig().newVideoDecoder(location);

            SegmentLookup lookup = new SegmentLookup();
            List<SegmentLookup.SegmentDescriptor> knownShots = lookup.lookUpAllSegments(video.getId());
            lookup.close();

            ShotSegmenter segmenter = new ShotSegmenter(vd, video.getId(), Config.getDatabaseConfig().getWriterSupplier().get(), knownShots);

            ArrayList<Extractor> extractors = new ArrayList();
            extractors.add(new NeuralNetVGG16Feature());
            ShotDispatcher dispatcher = new ShotDispatcher(extractors, new DefaultExtractorInitializer(), segmenter);

            dispatcher.run();
        }
        ml.close();
    }
}
