package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;

import java.util.UUID;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class SegmentDescriptor implements ExistenceCheck {
    /** Name of the entity in the persistence layer. */
    public static final String ENTITY  = "cineast_segment";

    /** Field names in the persistence layer.
     *
     *  Order (Important):
     *  - segmentid (PK)
     *  - objectid (FK -> MediaObject)
     *  - segmentnumber
     *  - segmentstart
     *  - segmentend
     */
    public static final String[] FIELDNAMES = {"segmentid", "objectid", "segmentnumber", "segmentstart", "segmentend"};

    private final String segmentId, objectId;
    private final int start, end, number;
    private final boolean exists;

    /**
     * Convenience method to create a SegmentDescriptor marked as new. The method will assign
     * a new ID to this SegmentDescriptor.
     *
     * @param objectId Object
     * @param segmentNumber
     * @param start
     * @param end
     * @return
     */
    public static SegmentDescriptor newSegmentDescriptor(String objectId, int segmentNumber, int start, int end) {
        String segmentId = MediaType.generateSegmentId(objectId, segmentNumber);
        return new SegmentDescriptor(objectId, segmentId, segmentNumber, start, end, false);
    }

    private SegmentDescriptor(String objectId, String segmentId, int segmentNumber, int start, int end, boolean exists) {
        this.segmentId = segmentId;
        this.objectId = objectId;
        this.number = segmentNumber;
        this.start = start;
        this.end = end;
        this.exists = exists;
    }

    public SegmentDescriptor(String objectId, String segmentId, int segmentNumber, int start, int end) {
        this(objectId, segmentId, segmentNumber, start, end, true);
    }

    public SegmentDescriptor() {
        this("", "", 0, 0, 0, false);
    }

    @JsonProperty
    public String getSegmentId() {
        return this.segmentId;
    }

    @JsonProperty
    public String getObjectId() {
        return this.objectId;
    }

    @JsonProperty
    public int getSequenceNumber() {
        return this.number;
    }

    @JsonProperty
    public int getCount() {
        return end - start + 1;
    }

    @JsonProperty
    public int getStart() {
        return start;
    }

    @JsonProperty
    public int getEnd() {
        return end;
    }

    @JsonIgnore
    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public String toString() {
        return "SegmentDescriptor(" + segmentId + ")";
    }


}
