package org.vitrivr.cineast.api.messages.pose;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.util.web.ImageParser;

import java.awt.image.BufferedImage;

public class SkelLookup implements Message {
    /** List of object ID's for which metadata should be looked up. */
    private final MultiImage img;

    public SkelLookup(BufferedImage image, CachedDataFactory factory) {
        this.img = factory.newInMemoryMultiImage(image);
    }

    @JsonCreator
    public SkelLookup(@JsonProperty("img") String img) {
        this(ImageParser.dataURLtoBufferedImage(img), CachedDataFactory.getDefault());
    }

    /**
     * Getter for {@link MultiImage}
     *
     * @return MultiImage
     */
    public MultiImage getImg() {
        return img;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.S_LOOKUP;
    }
}
