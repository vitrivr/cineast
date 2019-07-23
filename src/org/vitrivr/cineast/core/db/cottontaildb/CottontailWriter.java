package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection.Operation;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Where;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.RelationalOperator;

public class CottontailWriter extends AbstractPersistencyWriter<Tuple> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean useGlobalWrapper = true;
    private static final CottontailWrapper GLOBAL_COTTONTAIL_WRAPPER =
            useGlobalWrapper ? new CottontailWrapper() : null;
    private CottontailWrapper cottontail =
            useGlobalWrapper ? GLOBAL_COTTONTAIL_WRAPPER : new CottontailWrapper();

    private Entity entity;


    @Override
    public boolean open(String name) {
        this.entity = CottontailMessageBuilder.entity(name);
        return true;
    }

    @Override
    public boolean close() {
        if (useGlobalWrapper) {
            LOGGER.debug("Not closing Cottontailwriter because global wrapper flag is set");
            return false;
        }
        LOGGER.debug("Closing Cottontailwriter");
        this.cottontail.close();
        return true;
    }


    @Override
    public boolean exists(String key, String value) {

        Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, key); //TODO replace with exists projection
        Where where = CottontailMessageBuilder.atomicWhere(key, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));

        List<QueryResponseMessage> result = cottontail
                .query(
                        CottontailMessageBuilder.queryMessage(
                                CottontailMessageBuilder.query(entity, projection, where, null), ""
                        )
                );

        if (result.isEmpty()) {
            return false;
        }

        return result.get(0).getResultsCount() > 0;

    }


    @Override
    public boolean persist(List<PersistentTuple> tuples) {
        InsertMessage im = InsertMessage.newBuilder().setEntity(this.entity).addAllTuple(tuples.stream().map(this::getPersistentRepresentation).collect(Collectors.toList())).build();
        return this.cottontail.insertBlocking(im).getSuccess();
    }

    @Override
    public Tuple getPersistentRepresentation(PersistentTuple tuple) {

        Tuple.Builder tupleBuilder = Tuple.newBuilder();

        HashMap<String, Data> tmpMap = new HashMap<>();
        int nameIndex = 0;

        for (Object o : tuple.getElements()) {
            tmpMap.put(names[nameIndex++], CottontailMessageBuilder.toData(o));
        }

        return tupleBuilder.putAllData(tmpMap).build();

    }
}
