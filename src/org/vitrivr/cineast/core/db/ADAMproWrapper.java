package org.vitrivr.cineast.core.db;

import java.util.concurrent.ExecutionException;

import org.vitrivr.adampro.grpc.AdamDefinitionGrpc;
import org.vitrivr.adampro.grpc.AdamDefinitionGrpc.AdamDefinitionFutureStub;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamSearchGrpc;
import org.vitrivr.adampro.grpc.AdamSearchGrpc.AdamSearchFutureStub;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.DatabaseConfig;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ADAMproWrapper { //TODO generate interrupted ackmessage

	private ManagedChannel channel;
	private AdamDefinitionFutureStub definitionStub;
	private AdamSearchFutureStub searchStub;
	
	public ADAMproWrapper(){
		DatabaseConfig config = Config.getDatabaseConfig();
		this.channel = ManagedChannelBuilder.forAddress(config.getHost(), config.getPort()).usePlaintext(config.getPlaintext()).build();
		this.definitionStub = AdamDefinitionGrpc.newFutureStub(channel);
		this.searchStub = AdamSearchGrpc.newFutureStub(channel);
	}
	
	public synchronized ListenableFuture<AckMessage> createEntity(CreateEntityMessage message){
		return this.definitionStub.createEntity(message);
	}

	public AckMessage createEntityBlocking(CreateEntityMessage message){
		ListenableFuture<AckMessage> future = this.createEntity(message);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) { //TODO
			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized ListenableFuture<AckMessage> insertOne(InsertMessage message){
		return this.definitionStub.insert(message);
	}
	
	public AckMessage insertOneBlocking(InsertMessage message){
		ListenableFuture<AckMessage> future = this.insertOne(message);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) { //TODO
			e.printStackTrace();
			return null;
		}
	}

	public boolean existsEntity(String eName) {
	  ListenableFuture<ExistsMessage> future = this.definitionStub.existsEntity(EntityNameMessage.getDefaultInstance().toBuilder().clear().setEntity(eName).build());
		try{
			return future.get().getExists();
		}catch(InterruptedException | ExecutionException e){
			e.printStackTrace();
			return false;
		}
	}

	public AckMessage dropEntityBlocking(EntityNameMessage message){
		ListenableFuture<AckMessage> future = this.definitionStub.dropEntity(message);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ListenableFuture<QueryResultsMessage> booleanQuery(QueryMessage message){
		return standardQuery(message);
	}
	
	public ListenableFuture<QueryResultsMessage> standardQuery(QueryMessage message){
			synchronized (this.searchStub){
				return this.searchStub.doQuery(message);
			}
	}

	public ListenableFuture<QueryResultsMessage> previewEntity(PreviewMessage message){
		ListenableFuture<QueryResultsMessage> future;
		synchronized (this.searchStub) {
			future = this.searchStub.preview(message);
		}
		return future;
	}


	public PropertiesMessage getProperties(EntityPropertiesMessage message) {
		ListenableFuture<PropertiesMessage> future;
    synchronized (this.searchStub) {
			future = this.definitionStub.getEntityProperties(message);
		}
		try{
			return future.get();
		}catch(InterruptedException | ExecutionException e){
			e.printStackTrace();
			return null; //TODO
		}
	}
	
	public void close(){
		this.channel.shutdown();
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

    class LastObserver<T> implements StreamObserver<T>{

		private final SettableFuture<T> future;
		private T last = null;
		
		LastObserver(final SettableFuture<T> future){
			this.future = future;
		}
		
		@Override
		public void onCompleted() {
//			System.err.println("ADAMproWrapper.LastObserver.onCompleted(): " + this.last);
			future.set(this.last);
		}

		@Override
		public void onError(Throwable e) {
			e.printStackTrace(); //TODO
			future.setException(e);
		}

		@Override
		public void onNext(T t) {
//			System.out.println("ADAMproWrapper.LastObserver.onNext()");
			this.last = t;
		}
		
	}
	
	class LastAckStreamObserver extends LastObserver<AckMessage>{

		LastAckStreamObserver(SettableFuture<AckMessage> future) {
			super(future);
		}}
	
	class LastQueryResponseStreamObserver extends LastObserver<QueryResultsMessage>{

		LastQueryResponseStreamObserver(SettableFuture<QueryResultsMessage> future) {
			super(future);
		}}
	
}
