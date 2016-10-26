package org.vitrivr.cineast.core.db;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.vitrivr.adam.grpc.AdamDefinitionGrpc;
import org.vitrivr.adam.grpc.AdamDefinitionGrpc.AdamDefinitionStub;
import org.vitrivr.adam.grpc.AdamGrpc.*;
import org.vitrivr.adam.grpc.AdamSearchGrpc;
import org.vitrivr.adam.grpc.AdamSearchGrpc.AdamSearchFutureStub;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.DatabaseConfig;

import java.util.concurrent.ExecutionException;

public class ADAMproWrapper { //TODO generate interrupted ackmessage

	private ManagedChannel channel;
	private AdamDefinitionStub definitionStub;
	private AdamSearchFutureStub searchStub;
	
	public ADAMproWrapper(){
		DatabaseConfig config = Config.getDatabaseConfig();
		this.channel = ManagedChannelBuilder.forAddress(config.getHost(), config.getPort()).usePlaintext(config.getPlaintext()).build();
		this.definitionStub = AdamDefinitionGrpc.newStub(channel);
		this.searchStub = AdamSearchGrpc.newFutureStub(channel);
	}
	
	public synchronized ListenableFuture<AckMessage> createEntity(CreateEntityMessage message){
		SettableFuture<AckMessage> future = SettableFuture.create();
		this.definitionStub.createEntity(message, new LastAckStreamObserver(future));
		return future;
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
		SettableFuture<AckMessage> future = SettableFuture.create();
		StreamObserver<InsertMessage> insertObserver = this.definitionStub.insert(new LastAckStreamObserver(future));
		insertObserver.onNext(message);
		insertObserver.onCompleted();
		return future;
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
		SettableFuture<ExistsMessage> future = SettableFuture.create();
		this.definitionStub.existsEntity(EntityNameMessage.getDefaultInstance().toBuilder().clear().setEntity(eName).build(), new LastObserver(future));
		try{
			return future.get().getExists();
		}catch(InterruptedException | ExecutionException e){
			e.printStackTrace();
			return false;
		}
	}

	public AckMessage dropEntityBlocking(EntityNameMessage message){
		SettableFuture<AckMessage> future = SettableFuture.create();
		this.definitionStub.dropEntity(message, new LastAckStreamObserver(future));
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
		//SettableFuture<QueryResultsMessage> future = SettableFuture.create();
		//synchronized (this.searchStub) {
			return this.searchStub.doQuery(message/*, new LastQueryResponseStreamObserver(future)*/);
		//}
		//return future;
	}

	public ListenableFuture<QueryResultsMessage> previewEntity(EntityNameMessage message){
		ListenableFuture<QueryResultsMessage> future = SettableFuture.create();
		synchronized (this.searchStub) {
			future = this.searchStub.preview(message);
		}
		return future;
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
