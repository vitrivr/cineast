package org.vitrivr.cineast.core.db.dao;

import java.io.Closeable;
import java.util.*;
import java.util.stream.Collectors;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.tag.CompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.RelationalOperator;

public class TagHandler implements Closeable {

    private final PersistencyWriter<?> writer;
    private final DBSelector selector;

    /**
     * Name of the entity that contains the {@link Tag}s.
     */
    public static final String TAG_ENTITY_NAME = "cineast_tags";

    public static final String TAG_ID_COLUMNNAME= "id";
    public static final String TAG_NAME_COLUMNNAME = "name";
    public static final String TAG_DESCRIPTION_COLUMNNAME = "description";

    /**
     * A map containing cached {@link Tag}s.
     */
    private final HashMap<String, Tag> tagCache = new HashMap<>();

    /**
     * Constructor for {@link TagHandler}
     *
     * @param selector {@link DBSelector} instanced used for lookup of tags.
     * @param writer   {@link PersistencyWriter} used to persist tags.
     */
    public TagHandler(DBSelector selector, PersistencyWriter<?> writer) {
        this.selector = selector;
        this.writer = writer;

        if (this.selector == null) {
            throw new NullPointerException("selector cannot be null");
        }

        if (this.writer == null) {
            throw new NullPointerException("writer cannot be null");
        }

        this.selector.open(TAG_ENTITY_NAME);
        this.writer.open(TAG_ENTITY_NAME);
        this.writer.setFieldNames(TAG_ID_COLUMNNAME, TAG_NAME_COLUMNNAME, TAG_DESCRIPTION_COLUMNNAME);
    }

    /**
     * Default constructor for {@link TagHandler}. Uses the {@link DBSelector} and {@link PersistencyWriter} from the configuration
     */
    public TagHandler() {
        this(Config.sharedConfig().getDatabase().getSelectorSupplier().get(),
                Config.sharedConfig().getDatabase().getWriterSupplier().get());
    }

    /**
     * Adds and persist a new {@link Tag} entry.
     *
     * @param id ID of the new entry.
     * @param name Name of the new entry.
     * @param description Description of the new entry.
     * @return True on success, false otherwise.
     */
    public boolean addTag(String id, String name, String description) {
        return this.writer.persist(this.writer.generateTuple(id, name, description));
    }

    /**
     * Adds and persist a new {@link Tag} entry.
     *
     * @param tag {@link Tag} that should be added.
     */
    public boolean addTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        return addTag(tag.getId(), tag.getName(), tag.getDescription());
    }

    /**
     * Returns all {@link Tag}s that match the specified name. For matching, case-insensitive a left and right side truncation comparison is used.
     * The matching tags are returned in order of their expected relevance.
     *
     * @param name To value with which to match the {@link Tag}s.
     * @return List of matching {@link Tag}s.
     */
    public List<Tag> getTagsByMatchingName(final String name) {
        final String lname = name.toLowerCase();
        return this.selector.getRows("name", RelationalOperator.ILIKE, lname).stream()
                .map(TagHandler::fromMap)
                .sorted((o1, o2) -> {
                    boolean o1l = o1.getName().toLowerCase().startsWith(lname);
                    boolean o2l = o2.getName().toLowerCase().startsWith(lname);
                    boolean o1e = o1.getName().toLowerCase().equals(lname);
                    boolean o2e = o2.getName().toLowerCase().equals(lname);
                    if (o1e && !o2e) {
                        return -1;
                    } else if (!o1e && o2e) {
                        return 1;
                    } else if (o1l && !o2l) {
                        return -1;
                    } else if (!o1l && o2l) {
                        return 1;
                    } else {
                        return o1.getName().compareTo(o2.getName());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns all {@link Tag}s that are equal to the specified name.
     *
     * @param name To value with which to compare the {@link Tag}s.
     * @return List of matching {@link Tag}s.
     */
    public List<Tag> getTagsByName(String name) {
        List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("name", name);
        ArrayList<Tag> _return = new ArrayList<>(rows.size());
        for (Map<String, PrimitiveTypeProvider> row : rows) {
            Tag t = fromMap(row);
            if (t != null) {
                _return.add(t);
            }
        }
        return _return;
    }

    public Tag getTagById(String id) {
        if (id == null) {
            return null;
        }
        List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("id", id);
        if (rows.isEmpty()) {
            return null;
        }
        return fromMap(rows.get(0));

    }

    public List<Tag> getTagsById(String... ids) {
        if (ids == null) {
            return null;
        }
        List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("id", ids);
        if (rows.isEmpty()) {
            return null;
        }
        ArrayList<Tag> _return = new ArrayList<>(rows.size());
        for (Map<String, PrimitiveTypeProvider> row : rows) {
            Tag t = fromMap(row);
            if (t != null) {
                _return.add(t);
            }
        }
        return _return;

    }

    /**
     * Returns a list of all {@link Tag}s contained in the database.
     * <p>
     * TODO: Maybe should be removed, because ADAMpro caps the resultset anyway?
     *
     * @return List of all {@link Tag}s contained in the database
     */
    public List<Tag> getAll() {
        return this.selector.getAll().stream().map(TagHandler::fromMap).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of all cached {@link Tag}s.
     *
     * @return List of all {@link Tag}s contained in the cache.
     */
    public List<Tag> getAllCached() {
        return new ArrayList<>(this.tagCache.values());
    }

    public void initCache() {
        List<Tag> all = getAll();
        for (Tag tag : all) {
            this.tagCache.put(tag.getId(), tag);
        }
    }

    public void flushCache() {
        this.tagCache.clear();
    }

    public Tag getCachedById(String id) {
        return this.tagCache.get(id);
    }

    public List<Tag> getCachedByName(String name) {
        ArrayList<Tag> _return = new ArrayList<>();
        for (Tag t : this.tagCache.values()) {
            if (t.getName().equals(name)) {
                _return.add(t);
            }
        }
        return _return;
    }

    private static Tag fromMap(Map<String, PrimitiveTypeProvider> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        if (!map.containsKey("id") || !map.containsKey("name")) {
            return null;
        }

        if (!map.containsKey("description")) {
            return new CompleteTag(map.get("id").getString(), map.get("name").getString(), "");
        } else {
            return new CompleteTag(map.get("id").getString(), map.get("name").getString(),
                    map.get("description").getString());
        }

    }

    @Override
    public void close() {
        this.selector.close();
        this.writer.close();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
