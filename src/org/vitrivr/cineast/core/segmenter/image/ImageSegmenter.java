package org.vitrivr.cineast.core.segmenter.image;

import org.vitrivr.cineast.core.data.segments.ImageSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.segmenter.general.PassthroughSegmenter;

import java.awt.image.BufferedImage;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
public class ImageSegmenter extends PassthroughSegmenter<BufferedImage> {
    /**
     * @param content
     * @return
     */
    @Override
    protected SegmentContainer getSegmentFromContent(BufferedImage content) {
        return new ImageSegment(content);
    }
}
