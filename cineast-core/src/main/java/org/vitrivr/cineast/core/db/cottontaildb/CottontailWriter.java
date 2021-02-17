package org.vitrivr.cineast.core.db.cottontaildb;

import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.From;
import org.vitrivr.cottontail.grpc.CottontailGrpc.InsertMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.InsertMessage.InsertElement;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionOperation;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Scan;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Where;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.RelationalOperator;

public class CottontailWriter extends AbstractPersistencyWriter<InsertMessage.Builder> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final CottontailWrapper cottontail;

    public CottontailWriter(CottontailWrapper wrapper) {
        this.cottontail = wrapper;
    }

    private EntityName entity;


    @Override
    public boolean open(String name) {
        this.entity = CottontailMessageBuilder.entity(name);
        return true;
    }

    @Override
    public boolean close() {
        this.cottontail.close();
        return true;
    }


    @Override
    public boolean exists(String key, String value) {
        final Projection projection = CottontailMessageBuilder.projection(ProjectionOperation.EXISTS, key); //TODO replace with exists projection
        final Where where = CottontailMessageBuilder.atomicWhere(key, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));
        final List<QueryResponseMessage> result = cottontail.query(CottontailMessageBuilder.queryMessage(CottontailMessageBuilder.query(entity, projection, where, null, 1)));
        return result.get(0).getTuples(0).getData(0).getBooleanData();
    }


    @Override
    public boolean persist(List<PersistentTuple> tuples) {
        final List<InsertMessage> messages = tuples.stream()
            .map(t -> getPersistentRepresentation(t).setFrom(From.newBuilder().setScan(Scan.newBuilder().setEntity(this.entity))).build())
            .collect(Collectors.toList());
        return this.cottontail.insert(messages);
    }

    @Override
    public InsertMessage.Builder getPersistentRepresentation(PersistentTuple tuple) {
        final InsertMessage.Builder insertBuilder = InsertMessage.newBuilder();
        int index = 0;
        for (Object o : tuple.getElements()) {
            insertBuilder.addInserts(InsertElement.newBuilder().setColumn(ColumnName.newBuilder().setName(this.names[index++]).build()).setValue(CottontailMessageBuilder.toData(o)).build());
        }
        return insertBuilder;
    }
}
