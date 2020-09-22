package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.HashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class ExtractorConfig {
    /** Name of the Extractor. Must correspond to the simple-name or the FQN of the respective class.
     *
     * @see org.vitrivr.cineast.core.features.extractor.Extractor
     */
    private String name;

    /** Properties that are being used to initialize the Extractor.
     *
     * @see  org.vitrivr.cineast.core.features.extractor.Extractor
     */
    private HashMap<String, String> properties = new HashMap<>();

    @JsonProperty(required = true)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public HashMap<String, String> getProperties() {
        return properties;
    }
    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    @JsonIgnore
    public Extractor getExtractor() {
        return ReflectionHelper.newExtractor(this.name);
    }

    @JsonIgnore
    public Extractor getExporter() {
        return ReflectionHelper.newExporter(this.name, this.properties);
    }
}
