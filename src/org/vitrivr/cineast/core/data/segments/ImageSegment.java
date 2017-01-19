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

    private Frame frame;

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

    /**
     * Returns the median image, which is the actual image.
     *
     * @return
     */
    public MultiImage getAvgImg() {
       return this.image;
    }

    /**
     * Returns the average image, which is the actual image.
     *
     * @return
     */
    public MultiImage getMedianImg() {
        return this.image;
    }

    /**
     * Returns a single frame - the image.
     *
     * @return
     */
    @Override
    public List<Frame> getFrames() {
        ArrayList<Frame> list = new ArrayList<>(1);
        list.add(this.frame);
        return list;
    }

    /**
     * Returns a most representative frame - the image.
     *
     * @return
     */
    public Frame getMostRepresentativeFrame() {
        if (this.frame == null) this.frame = new Frame(1, this.image);
        return this.frame;
    }
}
