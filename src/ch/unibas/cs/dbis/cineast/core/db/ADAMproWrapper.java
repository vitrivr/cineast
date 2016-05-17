package ch.unibas.cs.dbis.cineast.core.db;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.config.DatabaseConfig;
import ch.unibas.dmi.dbis.adam.http.AdamDefinitionGrpc;
import ch.unibas.dmi.dbis.adam.http.AdamDefinitionGrpc.AdamDefinitionStub;
import ch.unibas.dmi.dbis.adam.http.AdamSearchGrpc;
import ch.unibas.dmi.dbis.adam.http.AdamSearchGrpc.AdamSearchStub;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ADAMproWrapper { //TODO generate interrupted ackmessage

	private ManagedChannel channel;
	private AdamDefinitionStub definitionStub;
	private AdamSearchStub searchStub;
	
	public ADAMproWrapper(){
		DatabaseConfig config = Config.getDatabaseConfig();
		this.channel = ManagedChannelBuilder.forAddress(config.getHost(), config.getPort()).usePlaintext(config.getPplaintext()).build();
		this.definitionStub = AdamDefinitionGrpc.newStub(channel);
		this.searchStub = AdamSearchGrpc.newStub(channel);
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
	
	public ListenableFuture<QueryResponseInfoMessage> booleanQuery(SimpleBooleanQueryMessage message){
		SettableFuture<QueryResponseInfoMessage> future = SettableFuture.create();
		synchronized (this.searchStub) {
			this.searchStub.doBooleanQuery(message, new LastQueryResponseStreamObserver(future));
		}
		return future;
	}
	
	public ListenableFuture<QueryResponseInfoMessage> standardQuery(SimpleQueryMessage message){
		SettableFuture<QueryResponseInfoMessage> future = SettableFuture.create();
		synchronized (this.searchStub) {
			this.searchStub.doStandardQuery(message, new LastQueryResponseStreamObserver(future));
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
	
	class LastQueryResponseStreamObserver extends LastObserver<QueryResponseInfoMessage>{

		LastQueryResponseStreamObserver(SettableFuture<QueryResponseInfoMessage> future) {
			super(future);
		}}
	
}
