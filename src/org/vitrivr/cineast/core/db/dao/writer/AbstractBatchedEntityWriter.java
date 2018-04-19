package org.vitrivr.cineast.core.db.dao.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.01.17
 */
public abstract class AbstractBatchedEntityWriter<T> implements AutoCloseable {
    /** The {@link Queue} used to store {@link PersistentTuple}s until they are flushed to disk. */
    private final ArrayBlockingQueue<PersistentTuple> buffer;

    /** {@link PersistencyWriter} instance used to persist changes to the underlying persistence layer. */
    protected PersistencyWriter<?> writer;

    protected AbstractBatchedEntityWriter(PersistencyWriter<?> writer, int batchsize, boolean init) {
        this.buffer = new ArrayBlockingQueue<>(batchsize);
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
     * @param entity
     * @return
     */
    protected abstract PersistentTuple generateTuple(T entity);

    /**
     * Persists the provided entity by first converting it to a {@link PersistentTuple} and subsequently writing that tuple to the local buffer.
     * If the buffer is full, i.e. the batch size was reached, then buffer is flushed first.
     *
     * @param entity The entity that should be persisted.
     */
    public void write(T entity) {
        final PersistentTuple tuple = this.generateTuple(entity);
        if (this.buffer.remainingCapacity() == 0) {
            this.flush();
        }
        this.buffer.offer(tuple);
    }

    public void write(List<T> entity) {
        entity.forEach(this::write);
    }

    /**
     * Drains the content of the buffer and writes it to the underlying persistence layer using the local {@link PersistencyWriter} instance.
     */
    public final void flush() {
        final List<PersistentTuple> batch = new ArrayList<>(buffer.size());
        this.buffer.drainTo(batch);
        this.writer.persist(batch);
    }

    /**
     * Flushes the buffer and closes the local {@link PersistencyWriter}.
     */
    @Override
    public final void close() {
        if (this.writer != null) {
            if (this.buffer.size() > 0) {
                this.flush();
            }

            if (this.writer != null) {
                this.writer.close();
                this.writer = null;
            }
        }
    }

    /**
     * Closes the {@link AbstractBatchedEntityWriter} upon finalization.
     *
     * @throws Throwable If finalization goes wrong.
     */
    @Override
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
