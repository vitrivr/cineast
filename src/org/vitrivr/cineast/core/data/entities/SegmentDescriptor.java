package org.vitrivr.cineast.core.data.entities;

import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.Shot;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class SegmentDescriptor implements ExistenceCheck {

    private final String segmentId, mmobjId;
    private final int startFrame, endFrame, number;
    private final boolean exists;

    private SegmentDescriptor(String multimediaObjectId, String segmentId, int segmentNumber, int startFrame, int endFrame, boolean exists) {
        this.segmentId = segmentId;
        this.mmobjId = multimediaObjectId;
        this.number = segmentNumber;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.exists = exists;
    }

    public SegmentDescriptor(String multimediaObjectId, String segmentId, int segmentNumber, int startFrame, int endFrame) {
        this(multimediaObjectId, segmentId, segmentNumber, startFrame, endFrame, true);
    }

    public SegmentDescriptor(String videoId, int segmentNumber, int startFrame, int endFrame) {
        this(videoId, Shot.generateShotID(videoId, segmentNumber), segmentNumber, startFrame, endFrame, true);
    }

    public SegmentDescriptor() {
        this("", "", 0, 0, 0, false);
    }

    public String getSegmentId() {
        return segmentId;
    }

    public String getVideoId() {
        return mmobjId;
    }

    public int getSequenceNumber() {
        return this.number;
    }

    public int getFramecount() {
        return endFrame - startFrame + 1;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public int getEndFrame() {
        return endFrame;
    }

    @Override
    public String toString() {
        return "SegmentDescriptor(" + segmentId + ")";
    }

    @Override
    public boolean exists() {
        return this.exists;
    }

}
