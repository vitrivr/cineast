package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.TensorFlow;
import org.tensorflow.proto.framework.OpList;

import java.io.File;
import java.util.Locale;

public class TFExtension {

    private TFExtension() {
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String OS_SELECTOR = (System.getProperty("os.name").split(" ")[0] + "_" + System.getProperty("os.arch")).toLowerCase(Locale.ROOT);

    private static final String TFTEXT_BASE = "resources/tf_text/" + TensorFlow.version();

    private static boolean loadLibraries(File... libraries) {
        if (libraries == null) {
            return false;
        }
        boolean success = true;
        for (File lib : libraries) {
            try {
                OpList list = TensorFlow.loadLibrary(lib.getAbsolutePath());
                LOGGER.info("loaded {} ops from {}", list.getOpCount(), lib.getName());
            } catch (UnsatisfiedLinkError e) {
                LOGGER.error("could not load {}", lib.getName());
                success = false;
            }
        }
        return success;
    }

    private static boolean textLoaded = false, textLoadAttempted = false;

    /**
     * Tries to load extension libraries of Tensorflow-Text
     * @return true if libraries were loaded successfully
     */
    public static synchronized boolean loadTFText() {
        if (textLoadAttempted) {
            return textLoaded;
        }
        LOGGER.info("Attempting to load extensions for TensorFlow-Text");
        textLoadAttempted = true;
        File tensorflowTextBase = new File(TFTEXT_BASE + "/" + OS_SELECTOR);
        System.out.println(tensorflowTextBase.getAbsolutePath());
        File[] libraries = tensorflowTextBase.listFiles(f -> f.getName().endsWith(".so"));
        textLoaded = loadLibraries(libraries);
        if (textLoaded) {
            LOGGER.info("Successfully loaded extensions for TensorFlow-Text");
        } else {
            LOGGER.error("Failed to load extensions for TensorFlow-Text");
        }
        return textLoaded;
    }

}
