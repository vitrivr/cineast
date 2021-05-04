package org.vitrivr.cineast.core.db.cottontaildb;


import io.grpc.StatusRuntimeException;
import java.util.List;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cottontail.client.TupleIterator;
import org.vitrivr.cottontail.client.language.basics.Constants;
import org.vitrivr.cottontail.client.language.dml.BatchInsert;
import org.vitrivr.cottontail.client.language.dml.Insert;
import org.vitrivr.cottontail.client.language.dql.Query;
import org.vitrivr.cottontail.client.language.extensions.Literal;

public final class CottontailWriter extends AbstractPersistencyWriter<Insert> {

    /** Internal reference to the {@link CottontailWrapper} used by this {@link CottontailWriter}. */
    private final CottontailWrapper cottontail;

    /** The fully qualified name of the entity handled by this {@link CottontailWriter}. */
    private String fqn;

    public CottontailWriter(CottontailWrapper wrapper) {
        this.cottontail = wrapper;
    }

    @Override
    public boolean open(String name) {
        this.fqn = this.cottontail.fqnInput(name);
        return true;
    }

    @Override
    public boolean close() {
        this.cottontail.close();
        return true;
    }

    @Override
    public boolean exists(String key, String value) {
        final Query query = new Query(this.fqn).exists().where(new Literal(key, "=", value));
        final TupleIterator results = this.cottontail.client.query(query, null);
        final Boolean b = results.next().asBoolean("exists");
        if (b != null) {
            return b;
        } else {
            throw new IllegalArgumentException("Unexpected value in result set.");
        }
    }

    @Override
    public boolean persist(List<PersistentTuple> tuples) {
        final long txId = this.cottontail.client.begin();
        try {
            BatchInsert insert = new BatchInsert().into(this.fqn).columns(this.names);
            while (!tuples.isEmpty()) {
                final PersistentTuple tuple = tuples.remove(0);
                final Object[] values = tuple.getElements().stream().map(o -> {
                    if(o instanceof ReadableFloatVector){
                        return ReadableFloatVector.toArray((ReadableFloatVector)o);
                    } else {
                        return o;
                    }
                }).toArray();
                insert.append(values);
                if (insert.size() >= Constants.MAX_PAGE_SIZE_BYTES) {
                    this.cottontail.client.insert(insert, txId);
                    insert = new BatchInsert().into(this.fqn).columns(this.names);
                }
            }
            if (insert.getBuilder().getInsertsCount() > 0) {
                this.cottontail.client.insert(insert, txId);
            }
            this.cottontail.client.commit(txId);
            return true;
        }  catch (StatusRuntimeException e) {
            this.cottontail.client.rollback(txId);
            return false;
        }
    }

    @Override
    public Insert getPersistentRepresentation(PersistentTuple tuple) {
        final Insert insert = new Insert(this.fqn);
        int index = 0;
        for (Object o : tuple.getElements()) {
            if(o instanceof ReadableFloatVector){
                insert.value(this.names[index++], ReadableFloatVector.toArray((ReadableFloatVector)o));
            } else {
                insert.value(this.names[index++], o);
            }
        }
        return insert;
    }
}
