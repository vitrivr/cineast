package org.vitrivr.cineast.core.data.entities;

import static org.vitrivr.cineast.core.util.CineastConstants.SEGMENT_ID_COLUMN_QUALIFIER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.ExistenceCheck;

/**
 * Describes a media segment in the Cineast data model i.e. a part of a media object (e.g. a series of frames in a video
 * or an image in an image sequence). The cardinality of a media object to its segments is 1 to (1..n) depending on the
 * type of media.
 *
 */
public final class MediaSegmentDescriptor implements ExistenceCheck {
    /** Name of the entity in the persistence layer. */
    public static final String ENTITY  = "cineast_segment";

    /** Name of the column wih the FK */
    public static final String OBJECT_ID_COL_NAME = "objectid";
    /** Name of the column with the segment number */
    public static final String SEGMENT_NO_COL_NAME = "segmentnumber";
    /** Name of the column with segmentstart value*/
    public static final String SEGMENT_START_COL_NAME = "segmentstart";
    /** Name of the column with segmentend value */
    public static final String SEGMENT_END_COL_NAME = "segmentend";
    /** Name of the column with segmetnstartabs value */
    public static final String SEGMENT_STARTABS_COL_NAME = "segmentstartabs";
    /** Name of the column with segmentendabs value*/
    public static final String SEGMENT_ENDABS_COL_NAME = "segmentendabs";

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
    public static final String[] FIELDNAMES = {
        SEGMENT_ID_COLUMN_QUALIFIER,
        OBJECT_ID_COL_NAME,
        SEGMENT_NO_COL_NAME,
        SEGMENT_START_COL_NAME,
        SEGMENT_END_COL_NAME,
        SEGMENT_STARTABS_COL_NAME,
        SEGMENT_ENDABS_COL_NAME};

    private final String segmentId, objectId;
    private final int start, end, number;
    private final float startabs, endabs;
    private final boolean exists;

    /**
     * Constructor for {@link MediaSegmentDescriptor}.
     *
     * @param objectId ID of the {@link MediaObjectDescriptor} this {@link MediaSegmentDescriptor} belongs to.
     * @param segmentId ID of the {@link MediaSegmentDescriptor}.
     * @param segmentNumber Relative position of the {@link MediaSegmentDescriptor} within the {@link MediaObjectDescriptor} (starts with 1)
     * @param start Start of the {@link MediaSegmentDescriptor} within the {@link MediaObjectDescriptor} in frames (e.g. for videos or audio).
     * @param end End of the {@link MediaSegmentDescriptor} within the {@link MediaObjectDescriptor} in frames (e.g. for videos or audio).
     * @param startabs Absolute start of the {@link MediaSegmentDescriptor} within the {@link MediaObjectDescriptor} in seconds (e.g. for videos or audio).
     * @param endabs Absolute end of the {@link MediaSegmentDescriptor} within the {@link MediaObjectDescriptor} in seconds (e.g. for videos or audio).
     * @param exists Whether or not this {@link MediaSegmentDescriptor} exists in the underlying database.
     */
    public MediaSegmentDescriptor(String objectId, String segmentId, int segmentNumber, int start, int end, float startabs, float endabs, boolean exists) {
        this.segmentId = segmentId;
        this.objectId = objectId;
        this.number = segmentNumber;
        this.start = start;
        this.end = end;
        this.startabs = startabs;
        this.endabs = endabs;
        this.exists = exists;
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

    /**
     * Note: this property is called **segmentnumber** in the data model now.
     */
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
