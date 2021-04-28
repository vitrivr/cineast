package org.vitrivr.cineast.core.db.cottontaildb;


import io.grpc.StatusRuntimeException;
import java.util.List;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cottontail.client.BatchInsertClient;
import org.vitrivr.cottontail.client.TupleIterator;
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
        this.fqn = this.cottontail.fqn(name);
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
        //final long txId = this.cottontail.client.begin();
        BatchInsertClient client = this.cottontail.startBatchInsert();
        try {
            for (PersistentTuple t : tuples) {
                client.insert(getPersistentRepresentation(t));
            }
            client.complete();
            return true;
        } catch (StatusRuntimeException e) {
            client.abort();
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
