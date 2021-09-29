package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class ImageSegment implements SegmentContainer {

    private MultiImage image;

    private VideoFrame videoFrame;

    private String segmentId;

    private String objectId;

    /**
     * Default constructor.
     *
     * @param image The {@link BufferedImage} to create a segment from.
     * @param factory The {@link CachedDataFactory} used to convert to a {@link MultiImage}. Handles caching!
     */
    public ImageSegment(BufferedImage image, CachedDataFactory factory) {
        this.image = factory.newMultiImage(image);
        this.videoFrame = new VideoFrame(1, 0, this.image, new VideoDescriptor(25, 40, this.image.getWidth(), this.image.getHeight()));
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
    @Override
    public MultiImage getAvgImg() {
       return this.image;
    }

    /**
     * Returns the average image, which is the actual image.
     *
     * @return
     */
    @Override
    public MultiImage getMedianImg() {
        return this.image;
    }

    /**
     * Returns a single frame - the image.
     *
     * @return
     */
    @Override
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
    @Override
    public VideoFrame getMostRepresentativeFrame() {
        return this.videoFrame;
    }
}
