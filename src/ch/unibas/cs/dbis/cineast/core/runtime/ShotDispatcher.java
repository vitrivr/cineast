package ch.unibas.cs.dbis.cineast.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LimitedQueue;
import ch.unibas.cs.dbis.cineast.core.data.providers.ShotProvider;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.features.extractor.ExtractorInitializer;
import ch.unibas.cs.dbis.cineast.core.util.DecodingError;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class ShotDispatcher implements Runnable {

	private static final int TASK_QUEUE_SIZE = 10;
	private static final int THREAD_COUNT = Config.numbetOfPoolThreads();
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private ArrayList<Extractor> extractors;
	private LinkedBlockingQueue<FrameContainer> shotQueue = new LinkedBlockingQueue<FrameContainer>(Config.shotQueueSize());
	private ExecutorService executor;
	private ShotProviderThread providerThread;
	private ExtractorInitializer initializer;
	
	public ShotDispatcher(ArrayList<Extractor> extractorList, ExtractorInitializer initializer, ShotProvider provider){
		this.extractors = extractorList;
		Collections.shuffle(this.extractors);
		LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
		this.executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 60, TimeUnit.SECONDS, taskQueue){

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				if(t != null){
					LOGGER.fatal("Decoding Error detected, shutting down");
					LOGGER.fatal(LogHelper.getStackTrace(t));
					this.shutdownNow();
					providerThread.interrupt();
				}
				super.afterExecute(r, null);
			}
			
		};
		
		this.providerThread = new ShotProviderThread(shotQueue, provider);
		this.initializer = initializer;
	}
	
	@Override
	public void run() {

		//init shot provider
		this.providerThread.start();
		LOGGER.debug("ShotSegmenterThread started");
		
		//init extractors
		for(Extractor e : extractors){
			this.initializer.initialize(e);
		}
		LOGGER.info("Features initialized");
		
		whileLoop:
		while(this.providerThread.isAlive() || !this.shotQueue.isEmpty()){
			try {
				FrameContainer s = this.shotQueue.poll(1, TimeUnit.MINUTES);
				if(s != null){
					LOGGER.info("start dispatching shot " + s.getId());
					for(Extractor f : extractors){
						try{
							this.executor.execute(new ExtractionTask(f, s));
							LOGGER.debug("submitted shot {} for feature {}", s, f);
						}catch(RejectedExecutionException e){
							this.providerThread.interrupt();
							this.shotQueue.clear();
							break whileLoop;
						}
					}
				}else{
					LOGGER.info("Timeout while waiting for shot to dispatch");
				}
			} catch (InterruptedException e) {
				LOGGER.warn("ShotDispatcher was interrupted: {}", LogHelper.getStackTrace(e));
			} catch (DecodingError e){
				LOGGER.fatal("Error while reading video: {}", LogHelper.getStackTrace(e));
			}
		}
		
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(15, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			LOGGER.warn("ShotDispatcher was interrupted: {}", LogHelper.getStackTrace(e));
		}
		
		LOGGER.debug("Closing Extractors");
		for(Extractor e : extractors){
			e.finish();
		}
		
	}

}
