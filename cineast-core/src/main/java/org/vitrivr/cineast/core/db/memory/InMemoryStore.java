package org.vitrivr.cineast.core.db.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.StampedLock;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


/**
 * This is a very simple in-memory key-value store implementation. It revolves around {@link Entity} objects, that hold
 * instance of {@link PersistentTuple}s. Obtaining such a {@link PersistentTuple} by key can be done in O(1). All other
 * operations take at least linear time to complete.
 *
 *
 * {@link InMemoryStore} can be used as a storage engine for Cineast. That is, there are implementations of
 * {@link PersistencyWriter} and {@link DBSelector} for this store.
 *
 * @see InMemoryWriter
 * @see InMemoryEntityCreator
 *
 * @author Ralph Gasser
 * @version 1.0
 */
public class InMemoryStore {

  /** Shared instance of {@link InMemoryStore}. */
  private static final InMemoryStore SHARED_STORE = new InMemoryStore();

  /** List of {@link Entity} objects held by this {@link InMemoryStore}. */
  private final Map<String,Entity> entities = new HashMap<>();

  /** Stamped lock to mediate access to {@link InMemoryStore}. */
  private final StampedLock storeLock = new StampedLock();

  /**
   * Access to an application wide singleton instance of {@link InMemoryStore}.
   *
   * @return Singleton instance of {@link InMemoryStore}.
   */
  public static InMemoryStore sharedInMemoryStore() {
    return SHARED_STORE;
  }

  public InMemoryStore() {}

  /**
   * Returns the {@link Entity} for the given name or an empty {@link Optional}, if that
   * {@link Entity} doesn't exist.
   *
   * @param name Name of the {@link Entity} to return.
   * @return An optional {@link Entity}
   */
  public Optional<Entity> getEntity(String name) {
    final long stamp = this.storeLock.readLock();
    try {
      return Optional.ofNullable(this.entities.get(name));
    } finally {
      this.storeLock.unlockRead(stamp);
    }
  }

  /**
   * Checks if this {@link InMemoryStore} has an {@link Entity} with the given name.
   *
   * @param name The name to check.
   * @return True if {@link Entity} exists, false otherwise.
   */
  public boolean hasEntity(String name) {
    final long stamp = this.storeLock.readLock();
    try {
      return this.entities.containsKey(name);
    } finally {
      this.storeLock.unlockRead(stamp);
    }
  }

  /**
   * Creates and returns the {@link Entity} for the given name or an empty {@link Optional}, if that
   * {@link Entity} already exists and hence wasn't created.
   *
   * @param name Name of the {@link Entity} to create.
   * @param columns The list of columns to create.
   * @return An optional {@link Entity}
   */
  public Optional<Entity> createEntity(String name, String... columns) {
    final long stamp = this.storeLock.writeLock();
    try {
      return Optional.ofNullable(this.entities.putIfAbsent(name, new Entity(columns)));
    } finally {
      this.storeLock.unlockWrite(stamp);
    }
  }

  /**
   * Drops the {@link Entity} for the given name if such an entity exists.
   *
   * @param name Name of the {@link Entity} to drop.
   * @return True if {@link Entity} was dropped, false otherwise.
   */
  public boolean dropEntity(String name) {
    final long stamp = this.storeLock.writeLock();
    try {
      return this.entities.remove(name) != null;
    } finally {
      this.storeLock.unlockWrite(stamp);
    }
  }

  /**
   * Drops all {@link Entity} contained in this {@link InMemoryStore}.
   */
  public void dropAll() {
    final long stamp = this.storeLock.writeLock();
    try {
      this.entities.clear();
    } finally {
      this.storeLock.unlockWrite(stamp);
    }
  }

  /**
   * An individual {@link Entity} in the {@link InMemoryStore}.
   *
   * @author Ralph Gasser
   * @version 1.0
   */
  class Entity implements Iterable<PersistentTuple> {

    /** The {@link java.util.Map} that holds all the data stored in this {@link org.vitrivr.cineast.core.db.memory.InMemoryStore.Entity}. */
    private final Map<String, PersistentTuple> store = new TreeMap<>();

    /** Name of the columns held by this {@link Entity}. */
    private final String[] columns;

    /** Stamped lock to mediate access to {@link Entity}. */
    private final StampedLock lock = new StampedLock();

    /**
     * Default constructor.
     *
     * @param columns List of columns held by this {@link Entity}
     */
    public Entity(String... columns) {
      this.columns = columns;
    }

    /**
     * Adds a {@link PersistentTuple} for the given key to the store.
     *
     * @param key   The key to use.
     * @param value The {@link PersistentTuple} to add.
     */
    public boolean put(String key, PersistentTuple value) {
      final long stamp = this.lock.writeLock();
      try {
        if (value.getElements().size() == this.columns.length) {
          this.store.put(key, value);
          return true;
        } else {
          return false;
        }
      }finally {
        this.lock.unlockWrite(stamp);
      }
    }

    /**
     * Fetches and returns the {@link PersistentTuple} for the given key.
     *
     * @param key Key to retrieve the {@link PersistentTuple} for.
     * @return Optional {@link PersistentTuple}
     */
    public Optional<PersistentTuple> get(String key) {
      final long stamp = this.lock.readLock();
      try {
        return Optional.ofNullable(this.store.get(key));
      } finally {
        this.lock.unlockRead(stamp);
      }
    }

    /**
     * Deletes the entry for the given key.
     *
     * @param key Key to delete the entry for.
     * @return True on success. False otherwise
     */
    public boolean delete(String key) {
      final long stamp = this.lock.writeLock();
      try {
        return this.store.remove(key) != null;
      } finally {
        this.lock.unlockWrite(stamp);
      }
    }

    /**
     * Deletes all entries in this {@link Entity}
     */
    public void truncate() {
      final long stamp = this.lock.writeLock();
      try {
        this.store.clear();
      } finally {
        this.lock.unlockWrite(stamp);
      }
    }

    /**
     * Returns true if this {@link Entity} contains a {@link PersistentTuple} for the given key, and
     * false otherwise.
     *
     * @param key The key to look up.
     * @return True if {@link Entity} contains a {@link PersistentTuple} for the given key, and
     * false otherwise
     */
    public boolean has(String key) {
      final long stamp = this.lock.readLock();
      try {
        return this.store.containsKey(key);
      } finally {
        this.lock.unlockRead(stamp);
      }
    }

    /**
     * Returns an {@link Iterator} for the values contained in this {@link Entity}.
     *
     * @return {@link Iterator} for the values contained in this {@link Entity}
     */
    public Iterator<PersistentTuple> iterator() {
      final long stamp = this.lock.readLock();
      return new Iterator<PersistentTuple>() {

        final Iterator<String> inner = Entity.this.store.keySet().iterator();

        @Override
        public boolean hasNext() {
          return this.inner.hasNext();
        }

        @Override
        public PersistentTuple next() {
          return Entity.this.store.get(this.inner.next());
        }

        public void finalize() {
          Entity.this.lock.unlockRead(stamp);
        }
      };
    }
  }
}
