package org.vitrivr.cineast.standalone.config.deserializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.DoublePair;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class FeatureCategoriesDeserializer extends JsonDeserializer<List<DoublePair<Class<? extends Retriever>>>> {

    private static Logger LOGGER = LogManager.getLogger();

    /* Class cache used to hold reference to FeatureModule that have already been instantiated. */
    private static final HashMap<String,Class<Retriever>> classCache = new HashMap<>();

    @Override
    public List<DoublePair<Class<? extends Retriever>>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ArrayNode arrayNode = p.readValueAsTree();
        List<DoublePair<Class<? extends Retriever>>> list = new ArrayList<>(arrayNode.size());
        for (JsonNode node : arrayNode) {
            /* Extract feature and weight node. */
            JsonNode featureNode = node.get("feature");
            JsonNode weightNode = node.get("weight");
            if (featureNode == null || weightNode == null) {
              continue;
            }
            String feature = featureNode.asText();
            Double weight = weightNode.asDouble();

            /* Get class for feature. Lookup in HashMap makes sure, that every FeatureModule is only loaded once! */
            Class<Retriever> c = FeatureCategoriesDeserializer.classCache.get(feature);
            if (c == null) {
                try {
                    if (feature.contains(".")) {
                        @SuppressWarnings("unchecked")
                        Class<Retriever> clazz = (Class<Retriever>) Class.forName(feature);
                        c = clazz;
                    } else {
                        c = ReflectionHelper.getClassFromName(feature, Retriever.class, ReflectionHelper.FEATURE_MODULE_PACKAGE);
                    }
                } catch (IllegalArgumentException | ClassNotFoundException | InstantiationException | UnsupportedOperationException e) {
                    LOGGER.log(Level.WARN, "The specified feature '" + feature + "' could not be instantiated: {}",
                        LogHelper.getStackTrace(e));
                }
            }

            /* If class could be fetched, add to list. */
            if (c == null) {
              continue;
            }
            list.add(new DoublePair<Class<? extends Retriever>>(c, weight));
        }

        return list;
    }
}
