package org.vitrivr.cineast.core.data;

import java.util.EnumSet;
import java.util.HashMap;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public enum MediaType {

    VIDEO(0, "v", "video"),
    IMAGE(1, "i", "image"),
    AUDIO(2, "a", "audio"),
    MODEL3D(3, "m", "3dmodel"),
    IMAGE_SEQUENCE(4, "is", "imagesequence"),
    UNKNOWN(99, "u", "unknown");

    private final int id;
    private final String prefix;
    private final String name;

    /**
     * Constructs a new media type given the id, the prefix and the name.
     *
     * @param id     numeric id to use to identify the type in the persistent storage layer
     * @param prefix the prefix of the MediaType without a trailing delimiter such as '_'
     * @param name   the name of the media type
     */
    MediaType(int id, String prefix, String name) {
        this.id = id;
        this.prefix = prefix;
        this.name = name.trim();
    }

    public int getId() {
        return this.id;
    }

    /**
     * @return the prefix of the MediaType without a trailing delimiter such as '_'.
     */
    public String getPrefix() {
        return this.prefix;
    }

    public String getName() {
        return this.name;
    }

    private static final TIntObjectHashMap<MediaType> idToType = new TIntObjectHashMap<>();
    private static final HashMap<String, MediaType> prefixToType = new HashMap<>();
    private static final HashMap<String, MediaType> nameToType = new HashMap<>();

    public static final char DELIMITER = '_';

    static {
        for (MediaType t : EnumSet.allOf(MediaType.class)) {
            if (idToType.containsKey(t.getId())) {
                throw new IllegalStateException("duplicate id (" + t.getId() + ") in Mediatype: " + t
                        + " collides with " + idToType.get(t.getId()));
            }
            idToType.put(t.getId(), t);

            if (prefixToType.containsKey(t.getPrefix())) {
                throw new IllegalStateException("duplicate prefix (" + t.getPrefix() + ") in Mediatype: "
                        + t + " collides with " + prefixToType.get(t.getPrefix()));
            }
            prefixToType.put(t.getPrefix(), t);

            if (nameToType.containsKey(t.getPrefix())) {
                throw new IllegalStateException("duplicate name (" + t.getName() + ") in Mediatype: " + t
                        + " collides with " + nameToType.get(t.getName()));
            }
            nameToType.put(t.getName().toLowerCase(), t);
        }
    }

    /**
     * @return the MediaType associated with this id or null in case there is none.
     */
    public static MediaType fromId(int id) {
        return idToType.get(id);
    }

    /**
     * @return the MediaType associated with this prefix or null in case there is none.
     */
    public static MediaType fromPrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        return prefixToType.get(prefix.trim().toLowerCase());
    }

    /**
     * @return the MediaType associated with this name or null in case there is none.
     */
    public static MediaType fromName(String name) {
        if (name == null) {
            return null;
        }
        return nameToType.get(name.trim().toLowerCase());
    }

    public static boolean existsId(int id) {
        return idToType.containsKey(id);
    }

    public static boolean existsPrefix(String prefix) {
        return prefixToType.containsKey(prefix);
    }

    public static boolean existsName(String name) {
        return nameToType.containsKey(name.trim().toLowerCase());
    }

    /**
     * generates an id of the form (prefix)_(object id) assuming the delimiter is '_'.
     *
     * @param type     the type for which an id is to be generated
     * @param objectId the globally unique id of the object
     * @throws IllegalArgumentException if objectId is empty
     * @throws NullPointerException     if type or objectId is null
     */
    public static String generateId(MediaType type, String objectId) throws IllegalArgumentException, NullPointerException {
        if (type == null) {
            throw new NullPointerException("Media type cannot be null!");
        }
        if (objectId == null) {
            throw new NullPointerException("Object ID cannot be null!");
        }
        if (objectId.isEmpty()) {
            throw new IllegalArgumentException("Object ID must not be empty!");
        }

        return type.getPrefix() + DELIMITER + objectId;
    }

    /**
     * Parses the provided object ID and decomposes it into a pair containing the {@link MediaType}, the ID of the media object.
     *
     * @param objectId The object ID that should be parsed.
     * @return Resulting pair.
     */
    public static Pair<MediaType,String> parseObjectId(String objectId) {
        final String[] components = objectId.split(String.valueOf(DELIMITER));
        return new ImmutablePair<>(MediaType.fromPrefix(components[0]), components[1]);
    }

    /**
     * generates a segment id of the form (prefix)_(object id)_(sequence number) assuming the
     * delimiter is '_'.
     *
     * @param type           the type for which an id is to be generated
     * @param objectId       the globally unique id of the object (without MediaType prefix)
     * @param sequenceNumber the number of the segment within the object
     * @throws IllegalArgumentException if shot sequence number is negative or objectId is empty
     * @throws NullPointerException     if type or objectId is null
     */
    public static String generateSegmentId(MediaType type, String objectId, long sequenceNumber)
            throws IllegalArgumentException, NullPointerException {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("sequenceNumber must be non-negative");
        }

        return generateId(type, objectId) + DELIMITER + sequenceNumber;

    }

    /**
     * generates a segment id of the form (prefix)_(object id)_(sequence number) assuming the
     * delimiter is '_'
     *
     * @param objectId       the globally unique id of the object (with MediaType prefix)
     * @param sequenceNumber the number of the segment within the object
     * @throws IllegalArgumentException if shot sequence number is negative or objectId is empty
     * @throws NullPointerException     if type or objectId is null
     */
    public static String generateSegmentId(String objectId, long sequenceNumber) throws IllegalArgumentException, NullPointerException {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("sequenceNumber must be non-negative");
        }
        return objectId + DELIMITER + sequenceNumber;
    }

    /**
     * Parses the provided segmentId and decomposes it into a pair containing the ID of the media object
     * and the segment's sequence number.
     *
     * @param segmentId The segment ID that should be parsed.
     * @return Resulting pair.
     */
    public static Pair<String, Long> parsesSegmentId(String segmentId) {
        final String[] components = segmentId.split(String.valueOf(DELIMITER));
        StringBuilder sb = new StringBuilder();
        sb.append(components[0]);
        for(int i = 1; i < components.length - 1; ++i){
            sb.append(DELIMITER);
            sb.append(components[i]);
        }
        return new ImmutablePair<>(sb.toString(), Long.valueOf(components[components.length - 1]));
    }
}
