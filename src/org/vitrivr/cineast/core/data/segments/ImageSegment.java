package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.SegmentContainer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.01.17
 */
public class ImageSegment implements SegmentContainer {

    private MultiImage image;

    private String segmentId;

    private String objectId;

    /**
     *
     * @param image
     */
    public ImageSegment(BufferedImage image) {
        this.image = MultiImageFactory.newMultiImage(image);
    }

    /**
     * @return a unique id of this
     */
    @Override
    public String getId() {
        return this.segmentId;
    }

    /**
     * @param id
     * @return a unique id of this
     */
    @Override
    public void setId(String id) {
        this.segmentId = id;
    }

    @Override
    public String getSuperId() {
        return this.objectId;
    }

    /**
     * @param id
     */
    @Override
    public void setSuperId(String id) {
        this.objectId = id;
    }

    public MultiImage getAvgImg() {
       return this.image;
    }

    public MultiImage getMedianImg() {
        return this.image;
    }

    @Override
    public List<Frame> getFrames() {
        ArrayList<Frame> list = new ArrayList<>(1);
        list.add(Frame.EMPTY_FRAME);
        return list;
    }
}
