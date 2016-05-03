package ch.unibas.cs.dbis.cineast.core.db;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.config.DatabaseConfig;
import ch.unibas.dmi.dbis.adam.http.AdamDefinitionGrpc;
import ch.unibas.dmi.dbis.adam.http.AdamDefinitionGrpc.AdamDefinitionStub;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ADAMproWrapper { //TODO generate interrupted ackmessage

	private static ADAMproWrapper instance = null;
	private static final Object instanceLock = new Object();
	
	public static final ADAMproWrapper getInstance(){
		if(instance == null){
			synchronized (instanceLock) {
				if(instance == null){
					instance = new ADAMproWrapper();
				}
			}
		}
		return instance;
	}
	
	private ManagedChannel channel;
	private AdamDefinitionStub stub;
	
	private ADAMproWrapper(){
		DatabaseConfig config = Config.getDatabaseConfig();
		this.channel = ManagedChannelBuilder.forAddress(config.getHost(), config.getPort()).usePlaintext(config.getPplaintext()).build();
		this.stub = AdamDefinitionGrpc.newStub(channel);
	}
	
	public synchronized ListenableFuture<AckMessage> createEntity(CreateEntityMessage message){
		SettableFuture<AckMessage> future = SettableFuture.create();
		this.stub.createEntity(message, new LastAckStreamObserver(future));
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
		StreamObserver<InsertMessage> insertObserver = this.stub.insert(new LastAckStreamObserver(future));
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
	
	@Override
	protected void finalize() throws Throwable {
		this.channel.shutdown();
		super.finalize();
	}
	
	class LastAckStreamObserver implements StreamObserver<AckMessage>{

		private final SettableFuture<AckMessage> future;
		private AckMessage last = null;
		
		LastAckStreamObserver(final SettableFuture<AckMessage> future){
			this.future = future;
		}
		
		@Override
		public void onCompleted() {
			future.set(this.last);
		}

		@Override
		public void onError(Throwable e) {
			future.setException(e);
		}

		@Override
		public void onNext(AckMessage ack) {
			this.last = ack;
		}
		
	}
	
}
