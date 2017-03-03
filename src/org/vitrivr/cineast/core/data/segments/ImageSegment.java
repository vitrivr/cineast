package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.MultiImageFactory;

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

    private VideoFrame videoFrame;

    private String segmentId;

    private String objectId;

    /**
     *
     * @param image
     */
    public ImageSegment(BufferedImage image) {
        this.image = MultiImageFactory.newMultiImage(image);
        this.videoFrame = new VideoFrame(1, this.image);
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
    public List<VideoFrame> getVideoFrames() {
        ArrayList<VideoFrame> list = new ArrayList<>(1);
        list.add(this.videoFrame);
        return list;
    }

    /**
     * Returns a most representative frame - the image.
     *
     * @return
     */
    public VideoFrame getMostRepresentativeFrame() {
        return this.videoFrame;
    }
}
