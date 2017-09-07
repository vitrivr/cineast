package org.vitrivr.cineast.core.db.dao;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.Tag;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;

public class TagHandler implements Closeable {

  private final PersistencyWriter<?> writer;
  private final DBSelector selector;

  public static final String ENTITY = "cineast_tags";

  public TagHandler(DBSelector selector, PersistencyWriter<?> writer) {
    this.selector = selector;
    this.writer = writer;

    if (this.selector == null) {
      throw new NullPointerException("selector cannot be null");
    }

    if (this.writer == null) {
      throw new NullPointerException("writer cannot be null");
    }

    this.selector.open(ENTITY);
    this.writer.open(ENTITY);
    this.writer.setFieldNames("id", "name", "description");
  }

  public TagHandler() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get(),
        Config.sharedConfig().getDatabase().getWriterSupplier().get());
  }

  public boolean addTag(String id, String name, String description) {
    return this.writer.persist(this.writer.generateTuple(id, name, description));
  }

  public boolean addTag(String id, String name) {
    return addTag(id, name, "");
  }

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

  public List<Tag> getAll() {
    return this.selector.getAll().stream().map(TagHandler::fromMap).filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static Tag fromMap(Map<String, PrimitiveTypeProvider> map) {
    if (map == null || map.isEmpty()) {
      return null;
    }

    if (!map.containsKey("id") || !map.containsKey("name")) {
      return null;
    }

    if (!map.containsKey("description")) {
      return new Tag(map.get("id").getString(), map.get("name").getString());
    } else {
      return new Tag(map.get("id").getString(), map.get("name").getString(),
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
