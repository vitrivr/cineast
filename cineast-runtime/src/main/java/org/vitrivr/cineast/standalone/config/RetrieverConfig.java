package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.Map;

public class RetrieverConfig {

    private final Class<? extends Retriever> retrieverClass;
    private final double weight;
    private final Map<String, String> properties;

    RetrieverConfig(Class<? extends Retriever> retrieverClass, double weight, Map<String, String> properties){
        this.retrieverClass = retrieverClass;
        this.weight = weight;
        this.properties = properties;
    }

    @JsonCreator
    public RetrieverConfig(
            @JsonProperty(value = "feature", required = true) String retrieverClassName,
            @JsonProperty(value = "weight", required = false, defaultValue = "1.0") Double weight,
            @JsonProperty(value = "properties", required = false) Map<String, String> properties
    ) throws InstantiationException, ClassNotFoundException {
        this.retrieverClass = ReflectionHelper.getClassFromName(retrieverClassName, Retriever.class, ReflectionHelper.FEATURE_MODULE_PACKAGE);
        this.weight = weight;
        this.properties = properties;
    }

    public RetrieverConfig(Class<? extends Retriever> retrieverClass, double weight){
        this(retrieverClass, weight, null);
    }

    public RetrieverConfig(Class<? extends Retriever> retrieverClass){
        this(retrieverClass, 1.0);
    }

    public Class<? extends Retriever> getRetrieverClass(){
        return this.retrieverClass;
    }

    public double getWeight(){
        return this.weight;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

}
