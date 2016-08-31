package org.vitrivr.cineast.core.db;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.adam.grpc.AdamGrpc.*;
import org.vitrivr.adam.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adam.grpc.AdamGrpc.InsertMessage.Builder;
import org.vitrivr.adam.grpc.AdamGrpc.InsertMessage.TupleInsertMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;



public class ADAMproWriter extends ProtobufTupleGenerator {
	
	private static final Logger LOGGER = LogManager.getLogger();
	private ADAMproWrapper adampro = new ADAMproWrapper();
	private String entityName;
	private Builder builder = InsertMessage.newBuilder();
	
	@Override
	public boolean open(String name) {
		this.entityName = name;
		return true;
	}

	@Override
	public boolean close() {
		this.adampro.close();
		return false;
	}

	@Override
	public boolean idExists(String id) {
		return exists("id", id);
	}

	@Override
	public boolean exists(String key, String value) { //TODO reduce the number of new objects created
		WhereMessage where = WhereMessage.newBuilder().setAttribute(key).setValue(value).build();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(AdamGrpc.FromMessage.newBuilder().setEntity(this.entityName).build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return responce.getResultsCount() > 0;
		
	}

	@Override
	public synchronized boolean persist(PersistentTuple<TupleInsertMessage> tuple) {
		this.builder.clear();
		this.builder.setEntity(this.entityName);
		ArrayList<TupleInsertMessage> tmp = new ArrayList<>(1);
		TupleInsertMessage tim = tuple.getPersistentRepresentation();
		tmp.add(tim);
		this.builder.addAllTuples(tmp);
		InsertMessage im = this.builder.build();
		AckMessage ack = this.adampro.insertOneBlocking(im);
		if(ack.getCode() != Code.OK){
			LOGGER.warn("{} during persist", ack.getMessage());
		}
		return ack.getCode() == Code.OK;
	}

	public synchronized boolean persist(List<PersistentTuple> tuples){
		this.builder.clear();
		this.builder.setEntity(this.entityName);
		ArrayList<TupleInsertMessage> tmp = new ArrayList(tuples.size());
		for(PersistentTuple<TupleInsertMessage> tuple : tuples){
			TupleInsertMessage tim = tuple.getPersistentRepresentation();
			tmp.add(tim);
		}
		this.builder.addAllTuples(tmp);
		InsertMessage im = this.builder.build();
		AckMessage ack = this.adampro.insertOneBlocking(im);
		if(ack.getCode() != Code.OK){
			LOGGER.warn("{} during persist", ack.getMessage());
		}
		return ack.getCode() == Code.OK;
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

}
