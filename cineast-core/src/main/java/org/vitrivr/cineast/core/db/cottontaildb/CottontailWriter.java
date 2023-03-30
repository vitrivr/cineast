package org.vitrivr.cineast.core.db.cottontaildb;

import io.grpc.StatusRuntimeException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cottontail.client.iterators.TupleIterator;
import org.vitrivr.cottontail.client.language.basics.Constants;
import org.vitrivr.cottontail.client.language.basics.predicate.Expression;
import org.vitrivr.cottontail.client.language.dml.BatchInsert;
import org.vitrivr.cottontail.client.language.dml.Insert;
import org.vitrivr.cottontail.client.language.dql.Query;

public final class CottontailWriter extends AbstractPersistencyWriter<Insert> {

  /**
   * Internal reference to the {@link CottontailWrapper} used by this {@link CottontailWriter}.
   */
  private final CottontailWrapper cottontail;
  /**
   * The batch size to use for INSERTS.
   */
  private final int batchSize;
  private final boolean useTransactions;
  /**
   * The fully qualified name of the entity handled by this {@link CottontailWriter}.
   */
  private String fqn;

  public CottontailWriter(CottontailWrapper wrapper, int batchSize, boolean useTransactions) {
    this.cottontail = wrapper;
    this.batchSize = batchSize;
    this.useTransactions = useTransactions;
  }

  @Override
  public boolean open(String name) {
    this.fqn = this.cottontail.fqnInput(name);
    return true;
  }

  @Override
  public void close() { /* No op */ }

  @Override
  public boolean exists(String key, String value) {
    final Query query = new Query(this.fqn).exists().where(new Expression(key, "=", value));
    final TupleIterator results = this.cottontail.client.query(query);
    final Boolean b = results.next().asBoolean("exists");
    if (b != null) {
      return b;
    } else {
      throw new IllegalArgumentException("Unexpected value in result set.");
    }
  }

  @Override
  public boolean persist(List<PersistentTuple> tuples) {
    if (this.fqn == null) {
      LOGGER.warn("fqn was null, not inserting {} tuples {}", tuples.size(), StringUtils.join(tuples, ", "));
    }
    long start = System.currentTimeMillis();
    int size = tuples.size();
    long txId = 0L;
    if (this.useTransactions) {
      txId = this.cottontail.client.begin();
    }
    try {
      BatchInsert insert = new BatchInsert().into(this.fqn).columns(this.names);
      if (useTransactions) {
        insert.txId(txId);
      }
      while (!tuples.isEmpty()) {
        final PersistentTuple tuple = tuples.remove(0);
        final Object[] values = tuple.getElements().stream().map(o -> {
          if (o instanceof ReadableFloatVector) {
            return ReadableFloatVector.toArray((ReadableFloatVector) o);
          } else {
            return o;
          }
        }).toArray();
        if (insert.serializedSize() >= Constants.MAX_PAGE_SIZE_BYTES - 10_000) { // cottontail sometimes acts up which is why we don't fully trust the max size
          LOGGER.trace("Inserting msg of size {} with {} elements into {}", insert.serializedSize(), insert.count(), this.fqn);
          this.cottontail.client.insert(insert);
          insert = new BatchInsert().into(this.fqn).columns(this.names);
          if (useTransactions) {
            insert.txId(txId);
          }
        }
        boolean append = insert.append(values);
        if (!append) {
          LOGGER.error("Value could not be appended to batch-insert");
        }
      }
      if (insert.count() > 0) {
        LOGGER.trace("Finalizing: Inserting msg of size {} with {} elements into {}", insert.serializedSize(), insert.count(), this.fqn);
        this.cottontail.client.insert(insert);
      }
      if (useTransactions) {
        LOGGER.trace("Committing");
        this.cottontail.client.commit(txId);
      }
      long stop = System.currentTimeMillis();
      LOGGER.trace("Completed insert of {} elements in {} ms", size, stop - start);
      return true;
    } catch (StatusRuntimeException e) {
      LOGGER.error(e);
      if (useTransactions) {
        this.cottontail.client.rollback(txId);
      }
      return false;
    }
  }

  @Override
  public Insert getPersistentRepresentation(PersistentTuple tuple) {
    final Insert insert = new Insert(this.fqn);
    int index = 0;
    for (Object o : tuple.getElements()) {
      if (o instanceof ReadableFloatVector) {
        insert.value(this.names[index++], ReadableFloatVector.toArray((ReadableFloatVector) o));
      } else {
        insert.value(this.names[index++], o);
      }
    }
    return insert;
  }

  @Override
  public int supportedBatchSize() {
    return this.batchSize;
  }
}
