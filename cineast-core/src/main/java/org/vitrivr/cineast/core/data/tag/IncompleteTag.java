package org.vitrivr.cineast.core.data.tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IncompleteTag implements WeightedTag {

    private final String id, name, description;
    private final float weight;

    public IncompleteTag(String id, String name, String description) {
        this(id, name, description, 1f);
    }


    /**
     * Constructor for {@link IncompleteTag}. Used to create object from JSON.
     *
     * @param id          The ID of the {@link IncompleteTag}, required.
     * @param name        The name of {@link IncompleteTag}, optional.
     * @param description The description of {@link IncompleteTag}, optional.
     * @param weight      The weight {@link IncompleteTag}, optional, defaults to 1.0
     */
    @JsonCreator
    public IncompleteTag(@JsonProperty(value = "id", required = true) String id,
                         @JsonProperty(value = "name") String name,
                         @JsonProperty(value = "description") String description,
                         @JsonProperty(value = "weight", defaultValue = "1.0f") Float weight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = (weight == null) ? 1f : weight;
    }

    public IncompleteTag(Tag t) {
        this(
                (t != null && t.hasId()) ? t.getId() : null,
                (t != null && t.hasName()) ? t.getName() : null,
                (t != null && t.hasDescription()) ? t.getDescription() : null,
                (t != null && t instanceof WeightedTag) ? ((WeightedTag) t).getWeight() : 1f
        );
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean hasId() {
        return this.id != null && !this.id.isEmpty();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasName() {
        return this.name != null && !this.name.isEmpty();
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean hasDescription() {
        return this.description != null && !this.description.isEmpty();
    }

    @Override
    public float getWeight() {
        return this.weight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Float.floatToIntBits(weight);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IncompleteTag other = (IncompleteTag) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("IncompleteTag [id=%s, name=%s, description=%s, weight=%s]", id, name,
                description, weight);
    }

}
