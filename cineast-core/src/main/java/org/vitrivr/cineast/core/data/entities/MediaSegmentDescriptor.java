package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class MediaSegmentDescriptor implements ExistenceCheck {
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
     *  - startabs
     */
    public static final String[] FIELDNAMES = {"segmentid", "objectid", "segmentnumber", "segmentstart", "segmentend", "segmentstartabs", "segmentendabs"};

    private final String segmentId, objectId;
    private final int start, end, number;
    private final float startabs, endabs;
    private final boolean exists;

    /**
     * Convenience method to create a MediaSegmentDescriptor marked as new. The method will assign
     * a new ID to this MediaSegmentDescriptor.
     *
     * @param objectId Object
     * @param segmentNumber
     * @param start
     * @param end
     * @return
     */
    public static MediaSegmentDescriptor newSegmentDescriptor(String objectId, int segmentNumber, int start, int end, float startabs, float endabs) {
        String segmentId = MediaType.generateSegmentId(objectId, segmentNumber);
        return new MediaSegmentDescriptor(objectId, segmentId, segmentNumber, start, end, startabs, endabs, false);
    }

    private MediaSegmentDescriptor(String objectId, String segmentId, int segmentNumber, int start, int end, float startabs, float endabs, boolean exists) {
        this.segmentId = segmentId;
        this.objectId = objectId;
        this.number = segmentNumber;
        this.start = start;
        this.end = end;
        this.startabs = startabs;
        this.endabs = endabs;
        this.exists = exists;
    }

    public MediaSegmentDescriptor(String objectId, String segmentId, int segmentNumber, int start, int end, float startabs, float endabs) {
        this(objectId, segmentId, segmentNumber, start, end, startabs, endabs, true);
    }

    public MediaSegmentDescriptor() {
        this("", "", 0, 0, 0, 0.0f, 0.0f, false);
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

    @JsonProperty
    public float getStartabs() {
        return this.startabs;
    }

    @JsonProperty
    public float getEndabs() {
        return this.endabs;
    }

    @JsonIgnore
    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }


}
