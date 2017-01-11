package org.vitrivr.cineast.core.data.entities;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */

/* TODO: @Ralph - Review and finalize. */
public class MultimediaMetadataDescriptor {
    private final String multimediaObjectId;
    private final String key;
    private final String value;

    /**
     *
     * @param multimediaObjectId
     * @param key
     * @param value
     * @return
     */
    public static MultimediaMetadataDescriptor makeImageDescriptor(String multimediaObjectId, String key, String value) {
        return new MultimediaMetadataDescriptor(multimediaObjectId, key, value);
    }

    /**
     *
     * @param multimediaObjectId
     * @param key
     * @param value
     */
    public MultimediaMetadataDescriptor(String multimediaObjectId, String key, String value) {
        this.multimediaObjectId = multimediaObjectId;
        this.key = key;
        this.value = value;
    }

    /**
     *
     * @return
     */
    public String getMultimediaObjectId() {
        return multimediaObjectId;
    }
    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
}

