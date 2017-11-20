package org.vitrivr.cineast.core.db.dao.writer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.01.17
 */
public abstract class AbstractBatchedEntityWriter<T> implements AutoCloseable {
  /** */
  private final ArrayDeque<PersistentTuple> buffer;

  /** */
  private final int batchsize;

  /** PersistencyWriter instance used to persist changes. */
  protected PersistencyWriter<?> writer;

  /**
   *
   * @param writer
   * @param batchsize
   */
  protected AbstractBatchedEntityWriter(PersistencyWriter<?> writer, int batchsize, boolean init) {
    this.buffer = new ArrayDeque<>(batchsize);
    this.batchsize = batchsize;
    this.writer = writer;
    if (init) {
      this.init();
    }
  }

  /**
   *
   */
  protected abstract void init();

  /**
   *
   * @param entity
   * @return
   */
  protected abstract PersistentTuple generateTuple(T entity);


  /**
   * Persists the provided entity descriptor in the database.
   *
   * @param entity Entity descriptor to persist
   */
  public void write(T entity) {
    PersistentTuple tuple = this.generateTuple(entity);
    if (this.batchsize == 1) {
      this.writeSingle(tuple);
    } else {
      this.writeBatched(tuple);
    }
  }

  /**
   *
   * @param entity
   */
  public void write(List<T> entity) {
    if (entity.size() < this.batchsize) {
      List<PersistentTuple> tuples = entity.stream().map(this::generateTuple)
          .collect(Collectors.toList());
      this.writer.persist(tuples);
    } else {
      entity.stream().map(this::generateTuple).forEach(this::writeBatched);
    }
  }

  /**
   *
   */
  public final void flush() {
    List<PersistentTuple> batch = new ArrayList<>(buffer.size());
    PersistentTuple t = null;
    while ((t = this.buffer.poll()) != null) {
      batch.add(t);
    }
    this.writer.persist(batch);
  }

  /**
   *
   * @param tuple
   */
  private void writeSingle(PersistentTuple tuple) {
    this.writer.persist(tuple);
  }

  /**
   *
   * @param tuple
   */
  private void writeBatched(PersistentTuple tuple) {
    this.buffer.offer(tuple);
    if (buffer.size() >= this.batchsize) {
      this.flush();
    }
  }

  /**
   * Flushes the buffer and closes the writer.
   */
  @Override
  public final void close() {
    if (this.buffer.size() > 0) {
      this.flush();
    }

    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
  }

  @Override
  public void finalize() throws Throwable {
    this.close();
    super.finalize();
  }
}
