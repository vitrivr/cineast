package org.vitrivr.cineast.core.data.queries;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.WebUtils;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class ImageQueryTerm extends QueryTerm {

    /**
     * Raw image-data in base64 encoding.
     */
    private final String imageData;

    /**
     *
     */
    private BufferedImage image;

    /**
     *
     * @param imageData
     * @param categories
     */
    @JsonCreator
    public ImageQueryTerm(@JsonProperty("image") String imageData,
                          @JsonProperty("categories") String[] categories,
                          @JsonProperty("weight") float weight) {
        super(categories, weight);
        this.imageData = imageData;
    }

    /**
     * Returns the image resulting from the stored image-data. This image lazily calculated
     * and stored for further reference.
     *
     * @return BufferedImage
     */
    public BufferedImage getImage() {
        if (this.image == null) {
            this.image = WebUtils.dataURLtoBufferedImage(this.imageData);
        }
        return this.image;
    }
}
